from typing import Optional, Dict
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from fastapi import HTTPException, status

from app.models.user import Usuario
from app.crud.user import user as user_crud
from app.core.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    verify_token_type
)
from app.core.config import settings


class AuthService:
    """
    Servicio de autenticación con control de intentos fallidos
    """
    
    def __init__(self):
        self.login_attempts: Dict[str, Dict] = {}
    
    def authenticate_user(
        self,
        db: Session,
        usuario: str,
        password: str
    ) -> Dict:
        """
        Autenticar usuario y generar tokens
        
        Args:
            db: Sesión de BD
            usuario: Nombre de usuario
            password: Contraseña en texto plano
            
        Returns:
            Dict con tokens y datos del usuario
            
        Raises:
            HTTPException: Si las credenciales son inválidas o cuenta bloqueada
        """
        
        # Verificar si la cuenta está bloqueada
        self._check_lockout(usuario)
        
        # Autenticar usuario
        user = user_crud.authenticate(db, usuario=usuario, password=password)
        
        if not user:
            self._increment_failed_attempts(usuario)
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=self._get_error_message(usuario),
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        # Verificar si el usuario está activo
        if not user_crud.is_active(user):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Usuario inactivo. Contacte al administrador.",
            )
        
        # Limpiar intentos fallidos
        self._clear_failed_attempts(usuario)
        
        # Actualizar última sesión
        user.ultima_sesion = datetime.utcnow()
        db.add(user)
        db.commit()
        
        # Generar tokens
        access_token = create_access_token(
            data={"sub": str(user.id_usuario), "rol": user.rol}
        )
        refresh_token = create_refresh_token(
            data={"sub": str(user.id_usuario), "rol": user.rol}
        )
        
        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer",
            "user": {
                "id_usuario": user.id_usuario,
                "usuario": user.usuario,
                "nombre_completo": user.nombre_completo,
                "email": user.email,
                "rol": user.rol,
                "primer_inicio": user.primer_inicio,
            }
        }
    
    def refresh_access_token(
        self,
        db: Session,
        refresh_token: str
    ) -> Dict:
        """
        Generar nuevo access token usando refresh token
        
        Args:
            db: Sesión de BD
            refresh_token: Refresh token válido
            
        Returns:
            Dict con nuevo access token
            
        Raises:
            HTTPException: Si el refresh token es inválido
        """
        
        # Decodificar y validar refresh token
        payload = decode_token(refresh_token)
        verify_token_type(payload, "refresh")
        
        user_id = int(payload.get("sub"))
        
        # Verificar que el usuario existe y está activo
        user = db.query(Usuario).filter(Usuario.id_usuario == user_id).first()
        
        if not user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Usuario no encontrado",
            )
        
        if not user_crud.is_active(user):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Usuario inactivo",
            )
        
        # Generar nuevo access token
        access_token = create_access_token(
            data={"sub": str(user.id_usuario), "rol": user.rol}
        )
        
        return {
            "access_token": access_token,
            "token_type": "bearer"
        }
    
    def _check_lockout(self, usuario: str):
        """
        Verificar si la cuenta está bloqueada por intentos fallidos
        
        Args:
            usuario: Nombre de usuario
            
        Raises:
            HTTPException: Si la cuenta está bloqueada
        """
        if usuario not in self.login_attempts:
            return
        
        attempt_data = self.login_attempts[usuario]
        
        if attempt_data["count"] >= settings.MAX_LOGIN_ATTEMPTS:
            lockout_time = attempt_data["locked_until"]
            
            if datetime.utcnow() < lockout_time:
                remaining = (lockout_time - datetime.utcnow()).seconds // 60
                raise HTTPException(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    detail=f"Cuenta bloqueada. Intente nuevamente en {remaining} minutos.",
                )
            else:
                # El bloqueo ha expirado, limpiar intentos
                self._clear_failed_attempts(usuario)
    
    def _increment_failed_attempts(self, usuario: str):
        """
        Incrementar contador de intentos fallidos
        
        Args:
            usuario: Nombre de usuario
        """
        if usuario not in self.login_attempts:
            self.login_attempts[usuario] = {
                "count": 0,
                "locked_until": None
            }
        
        self.login_attempts[usuario]["count"] += 1
        
        # Si alcanzó el máximo, bloquear la cuenta
        if self.login_attempts[usuario]["count"] >= settings.MAX_LOGIN_ATTEMPTS:
            self.login_attempts[usuario]["locked_until"] = (
                datetime.utcnow() + timedelta(minutes=settings.LOCKOUT_DURATION_MINUTES)
            )
    
    def _clear_failed_attempts(self, usuario: str):
        """
        Limpiar intentos fallidos después de login exitoso
        
        Args:
            usuario: Nombre de usuario
        """
        if usuario in self.login_attempts:
            del self.login_attempts[usuario]
    
    def _get_error_message(self, usuario: str) -> str:
        """
        Generar mensaje de error personalizado según intentos
        
        Args:
            usuario: Nombre de usuario
            
        Returns:
            Mensaje de error
        """
        if usuario not in self.login_attempts:
            return "Credenciales inválidas"
        
        attempts = self.login_attempts[usuario]["count"]
        remaining = settings.MAX_LOGIN_ATTEMPTS - attempts
        
        if remaining > 0:
            return f"Credenciales inválidas. Intentos restantes: {remaining}"
        else:
            return "Cuenta bloqueada por múltiples intentos fallidos"


# Instancia global del servicio
auth_service = AuthService()