from functools import wraps
from typing import List
from fastapi import HTTPException, status

from app.models.user import Usuario


def require_roles(allowed_roles: List[str]):
    """
    Decorador para verificar que el usuario tenga uno de los roles permitidos
    
    Args:
        allowed_roles: Lista de roles permitidos
        
    Raises:
        HTTPException: Si el usuario no tiene el rol requerido
        
    Example:
        @router.get("/admin-only")
        @require_roles(["admin"])
        def admin_only_route(current_user: Usuario = Depends(get_current_user)):
            ...
    """
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # Obtener current_user de kwargs
            current_user = kwargs.get("current_user")
            
            if not current_user:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="No autenticado"
                )
            
            if current_user.rol not in allowed_roles:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail=f"Requiere rol: {', '.join(allowed_roles)}"
                )
            
            return func(*args, **kwargs)
        
        return wrapper
    return decorator


def require_admin(func):
    """
    Decorador para verificar que el usuario sea administrador
    
    Raises:
        HTTPException: Si el usuario no es admin
        
    Example:
        @router.delete("/users/{id}")
        @require_admin
        def delete_user(id: int, current_user: Usuario = Depends(get_current_user)):
            ...
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        current_user = kwargs.get("current_user")
        
        if not current_user or not current_user.is_admin:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Requiere permisos de administrador"
            )
        
        return func(*args, **kwargs)
    
    return wrapper


def require_active_user(func):
    """
    Decorador para verificar que el usuario esté activo
    
    Raises:
        HTTPException: Si el usuario está inactivo
        
    Example:
        @router.get("/profile")
        @require_active_user
        def get_profile(current_user: Usuario = Depends(get_current_user)):
            ...
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        current_user = kwargs.get("current_user")
        
        if not current_user or not current_user.activo:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Usuario inactivo"
            )
        
        return func(*args, **kwargs)
    
    return wrapper


class PermissionChecker:
    """
    Clase para verificaciones de permisos más complejas
    """
    
    @staticmethod
    def can_manage_user(current_user: Usuario, target_user: Usuario) -> bool:
        """
        Verificar si el usuario actual puede gestionar al usuario objetivo
        
        Args:
            current_user: Usuario que intenta la acción
            target_user: Usuario objetivo
            
        Returns:
            True si tiene permisos, False si no
        """
        # Admin puede gestionar a todos
        if current_user.is_admin:
            return True
        
        # Un chofer solo puede gestionar su propio perfil
        if current_user.id_usuario == target_user.id_usuario:
            return True
        
        return False
    
    @staticmethod
    def can_view_empresa(current_user: Usuario, id_empresa: int) -> bool:
        """
        Verificar si el usuario puede ver datos de una empresa
        
        Args:
            current_user: Usuario actual
            id_empresa: ID de la empresa
            
        Returns:
            True si tiene permisos, False si no
        """
        # Admin puede ver todas las empresas
        if current_user.is_admin:
            return True
        
        # Chofer solo puede ver su propia empresa
        if current_user.id_empresa == id_empresa:
            return True
        
        return False