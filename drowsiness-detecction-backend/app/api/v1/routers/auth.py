from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, status, Body
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_user, get_current_active_user, get_current_token
from app.schemas.token import Token, RefreshTokenRequest
from app.schemas.user import UserResponse, PasswordChange, LoginRequest
from app.services.auth_service import auth_service
from app.models.user import Usuario
from app.crud.user import user as user_crud
from app.crud.token_blacklist import token_blacklist
from app.core.security import decode_token

router = APIRouter()


@router.post(
    "/login",
    response_model=dict,
    status_code=status.HTTP_200_OK,
    summary="Iniciar sesión",
    description="Autentica un usuario y retorna tokens JWT",
    responses={
        200: {
            "description": "Login exitoso",
            "content": {
                "application/json": {
                    "example": {
                        "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "token_type": "bearer",
                        "user": {
                            "id_usuario": 1,
                            "usuario": "admin",
                            "nombre_completo": "Administrador del Sistema",
                            "email": "admin@sistema.com",
                            "rol": "admin",
                            "primer_inicio": False
                        }
                    }
                }
            }
        },
        401: {
            "description": "Credenciales inválidas",
            "content": {
                "application/json": {
                    "example": {"detail": "Credenciales inválidas. Intentos restantes: 2"}
                }
            }
        },
        403: {
            "description": "Usuario inactivo",
            "content": {
                "application/json": {
                    "example": {"detail": "Usuario inactivo. Contacte al administrador."}
                }
            }
        },
        429: {
            "description": "Cuenta bloqueada",
            "content": {
                "application/json": {
                    "example": {"detail": "Cuenta bloqueada. Intente nuevamente en 15 minutos."}
                }
            }
        }
    }
)
def login(
    *,
    db: Session = Depends(get_db),
    credentials: LoginRequest = Body(
        ...,
        examples={
            "admin": {
                "summary": "Login como Admin",
                "description": "Credenciales del administrador del sistema",
                "value": {
                    "username": "admin",
                    "password": "admin123"
                }
            },
            "chofer": {
                "summary": "Login como Chofer",
                "description": "Credenciales de un chofer de ejemplo",
                "value": {
                    "username": "jperez",
                    "password": "chofer123"
                }
            }
        }
    )
):
    return auth_service.authenticate_user(
        db=db,
        usuario=credentials.username,
        password=credentials.password
    )


@router.post(
    "/refresh",
    response_model=dict,
    summary="Renovar token de acceso",
    description="Genera un nuevo access token usando el refresh token",
    responses={
        200: {
            "description": "Token renovado exitosamente",
            "content": {
                "application/json": {
                    "example": {
                        "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "token_type": "bearer"
                    }
                }
            }
        },
        401: {
            "description": "Refresh token inválido o expirado"
        }
    }
)
def refresh_token(
    *,
    db: Session = Depends(get_db),
    refresh_request: RefreshTokenRequest = Body(
        ...,
        example={
            "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
    )
):
    return auth_service.refresh_access_token(
        db=db,
        refresh_token=refresh_request.refresh_token
    )


@router.get(
    "/me",
    response_model=UserResponse,
    summary="Obtener usuario actual",
    description="Retorna información del usuario autenticado. **Requiere Bearer Token**",
    responses={
        200: {
            "description": "Información del usuario",
            "content": {
                "application/json": {
                    "example": {
                        "id_usuario": 1,
                        "usuario": "admin",
                        "nombre_completo": "Administrador del Sistema",
                        "email": "admin@sistema.com",
                        "rol": "admin",
                        "activo": True,
                        "primer_inicio": False
                    }
                }
            }
        },
        401: {"description": "Token inválido o no proporcionado"}
    }
)
def get_current_user_info(
    current_user: Usuario = Depends(get_current_active_user)
):
    return current_user


@router.post(
    "/change-password",
    summary="Cambiar contraseña",
    description="Permite al usuario cambiar su propia contraseña. **Requiere Bearer Token**",
    responses={
        200: {
            "description": "Contraseña cambiada exitosamente",
            "content": {
                "application/json": {
                    "example": {"message": "Contraseña actualizada exitosamente"}
                }
            }
        },
        400: {"description": "Contraseña actual incorrecta"},
        401: {"description": "Token inválido o no proporcionado"}
    }
)
def change_password(
    *,
    db: Session = Depends(get_db),
    password_data: PasswordChange = Body(
        ...,
        example={
            "current_password": "admin123",
            "new_password": "nueva_password_segura"
        }
    ),
    current_user: Usuario = Depends(get_current_active_user)
):
    # Verificar contraseña actual
    if not user_crud.authenticate(
        db,
        usuario=current_user.usuario,
        password=password_data.current_password
    ):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Contraseña actual incorrecta"
        )
    
    # Actualizar contraseña
    user_crud.update_password(
        db,
        user=current_user,
        new_password=password_data.new_password
    )
    
    return {"message": "Contraseña actualizada exitosamente"}


@router.post(
    "/logout",
    summary="Cerrar sesión",
    description="Invalida el token actual agregándolo a la blacklist. **Requiere Bearer Token**",
    responses={
        200: {
            "description": "Sesión cerrada",
            "content": {
                "application/json": {
                    "example": {
                        "message": "Sesión cerrada exitosamente",
                        "user": "admin"
                    }
                }
            }
        }
    }
)
def logout(
    *,
    db: Session = Depends(get_db),
    token: str = Depends(get_current_token),
    current_user: Usuario = Depends(get_current_active_user)
):
   
    # Decodificar token para obtener fecha de expiración
    payload = decode_token(token)
    exp_timestamp = payload.get("exp")
    
    if exp_timestamp:
        fecha_expiracion = datetime.fromtimestamp(exp_timestamp)
    else:
        # Si no tiene exp, usar fecha por defecto (30 minutos)
        from datetime import timedelta
        fecha_expiracion = datetime.utcnow() + timedelta(minutes=30)
    
    # Agregar token a blacklist
    token_blacklist.add_token(
        db,
        token=token,
        id_usuario=current_user.id_usuario,
        fecha_expiracion=fecha_expiracion
    )
    
    return {
        "message": "Sesión cerrada exitosamente",
        "user": current_user.usuario
    }