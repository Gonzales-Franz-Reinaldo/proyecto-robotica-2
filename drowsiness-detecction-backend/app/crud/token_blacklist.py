from datetime import datetime
from typing import Optional
from sqlalchemy.orm import Session

from app.models.token_blacklist import TokenBlacklist


class CRUDTokenBlacklist:
    """CRUD para gestión de tokens invalidados"""
    
    def add_token(
        self,
        db: Session,
        *,
        token: str,
        id_usuario: int,
        fecha_expiracion: datetime
    ) -> TokenBlacklist:
        """
        Agregar token a la blacklist
        
        Args:
            db: Sesión de BD
            token: Token JWT a invalidar
            id_usuario: ID del usuario
            fecha_expiracion: Fecha de expiración del token
            
        Returns:
            Instancia de TokenBlacklist creada
        """
        db_obj = TokenBlacklist(
            token=token,
            id_usuario=id_usuario,
            fecha_expiracion=fecha_expiracion
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj
    
    def is_blacklisted(self, db: Session, *, token: str) -> bool:
        """
        Verificar si un token está en la blacklist
        
        Args:
            db: Sesión de BD
            token: Token JWT a verificar
            
        Returns:
            True si está en blacklist, False si no
        """
        result = db.query(TokenBlacklist).filter(
            TokenBlacklist.token == token
        ).first()
        
        return result is not None
    
    def clean_expired_tokens(self, db: Session) -> int:
        """
        Limpiar tokens expirados de la blacklist
        
        Args:
            db: Sesión de BD
            
        Returns:
            Número de tokens eliminados
        """
        count = db.query(TokenBlacklist).filter(
            TokenBlacklist.fecha_expiracion < datetime.utcnow()
        ).delete()
        
        db.commit()
        return count


# Instancia global
token_blacklist = CRUDTokenBlacklist()