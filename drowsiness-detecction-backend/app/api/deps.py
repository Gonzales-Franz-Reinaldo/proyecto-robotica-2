from typing import Generator, Optional
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.models.user import Usuario
from app.core.security import decode_token, verify_token_type
from app.crud.user import user as user_crud
from app.crud.token_blacklist import token_blacklist

# OAuth2 scheme para extraer el token del header Authorization
oauth2_scheme = OAuth2PasswordBearer(
    tokenUrl="/api/v1/auth/login",
    scheme_name="JWT"
)


def get_current_user(
    db: Session = Depends(get_db),
    token: str = Depends(oauth2_scheme)
) -> Usuario:
    """
    Dependency para obtener el usuario actual desde el JWT token
    
    Args:
        db: Sesión de BD
        token: JWT token del header Authorization
        
    Returns:
        Instancia del Usuario autenticado
        
    Raises:
        HTTPException: Si el token es inválido o el usuario no existe
    """
    
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="No se pudo validar las credenciales",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    # Verificar si el token está en blacklist
    if token_blacklist.is_blacklisted(db, token=token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalidado. Por favor, inicie sesión nuevamente.",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    try:
        # Decodificar token
        payload = decode_token(token)
        verify_token_type(payload, "access")
        
        user_id: str = payload.get("sub")
        
        if user_id is None:
            raise credentials_exception
        
    except JWTError:
        raise credentials_exception
    
    # Obtener usuario de la BD
    user = db.query(Usuario).filter(Usuario.id_usuario == int(user_id)).first()
    
    if user is None:
        raise credentials_exception
    
    # Verificar que el usuario esté activo
    if not user_crud.is_active(user):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Usuario inactivo"
        )
    
    return user


def get_current_active_user(
    current_user: Usuario = Depends(get_current_user)
) -> Usuario:
    """
    Dependency para verificar que el usuario esté activo
    
    Args:
        current_user: Usuario actual
        
    Returns:
        Usuario activo
        
    Raises:
        HTTPException: Si el usuario está inactivo
    """
    if not current_user.activo:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Usuario inactivo"
        )
    return current_user


def get_current_admin_user(
    current_user: Usuario = Depends(get_current_user)
) -> Usuario:
    """
    Dependency para verificar que el usuario sea administrador
    
    Args:
        current_user: Usuario actual
        
    Returns:
        Usuario con rol admin
        
    Raises:
        HTTPException: Si el usuario no es admin
    """
    if not current_user.is_admin:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tiene permisos de administrador"
        )
    return current_user


def get_current_chofer_user(
    current_user: Usuario = Depends(get_current_user)
) -> Usuario:
    """
    Dependency para verificar que el usuario sea chofer
    
    Args:
        current_user: Usuario actual
        
    Returns:
        Usuario con rol chofer
        
    Raises:
        HTTPException: Si el usuario no es chofer
    """
    if not current_user.is_chofer:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tiene permisos de chofer"
        )
    return current_user


def get_current_token(token: str = Depends(oauth2_scheme)) -> str:
    """
    Dependency para obtener el token actual
    
    Args:
        token: JWT token del header Authorization
        
    Returns:
        Token JWT
    """
    return token