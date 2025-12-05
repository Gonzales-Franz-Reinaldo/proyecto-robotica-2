from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import or_

from app.crud.base import CRUDBase
from app.models.user import Usuario
from app.schemas.user import UserCreate, UserUpdate
from app.core.security import get_password_hash, verify_password


class CRUDUser(CRUDBase[Usuario, UserCreate, UserUpdate]):
    """
    CRUD específico para Usuario con métodos adicionales
    """
    
    def get_by_usuario(self, db: Session, *, usuario: str) -> Optional[Usuario]:
        """
        Obtener usuario por nombre de usuario
        
        Args:
            db: Sesión de BD
            usuario: Nombre de usuario
            
        Returns:
            Instancia de Usuario o None
        """
        return db.query(Usuario).filter(Usuario.usuario == usuario).first()
    
    def get_by_email(self, db: Session, *, email: str) -> Optional[Usuario]:
        """
        Obtener usuario por email
        
        Args:
            db: Sesión de BD
            email: Email del usuario
            
        Returns:
            Instancia de Usuario o None
        """
        return db.query(Usuario).filter(Usuario.email == email).first()
    
    def get_by_dni(self, db: Session, *, dni_ci: str) -> Optional[Usuario]:
        """
        Obtener usuario por DNI/CI
        
        Args:
            db: Sesión de BD
            dni_ci: DNI o Cédula de Identidad
            
        Returns:
            Instancia de Usuario o None
        """
        return db.query(Usuario).filter(Usuario.dni_ci == dni_ci).first()
    
    def create(self, db: Session, *, obj_in: UserCreate) -> Usuario:
        """
        Crear usuario con password hasheado
        
        Args:
            db: Sesión de BD
            obj_in: Schema con datos del usuario
            
        Returns:
            Instancia de Usuario creada
        """
        db_obj = Usuario(
            usuario=obj_in.usuario,
            password_hash=get_password_hash(obj_in.password),
            email=obj_in.email,
            nombre_completo=obj_in.nombre_completo,
            telefono=obj_in.telefono,
            rol=obj_in.rol,
            dni_ci=obj_in.dni_ci,
            genero=obj_in.genero,
            nacionalidad=obj_in.nacionalidad,
            fecha_nacimiento=obj_in.fecha_nacimiento,
            direccion=obj_in.direccion,
            ciudad=obj_in.ciudad,
            tipo_chofer=obj_in.tipo_chofer,
            id_empresa=obj_in.id_empresa,
            numero_licencia=obj_in.numero_licencia,
            categoria_licencia=obj_in.categoria_licencia,
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj
    
    def update(self, db: Session, *, db_obj: Usuario, obj_in: UserUpdate) -> Usuario:
        """
        Actualizar usuario (con manejo especial de password)
        
        Args:
            db: Sesión de BD
            db_obj: Instancia actual del usuario
            obj_in: Schema con datos a actualizar
            
        Returns:
            Instancia de Usuario actualizada
        """
        # Obtener solo los campos que fueron proporcionados
        update_data = obj_in.dict(exclude_unset=True)
        
        # Manejar password de forma especial
        if "password" in update_data:
            if update_data["password"]:  # Solo si no es None o vacío
                # Hashear la nueva contraseña y guardarla en password_hash
                update_data["password_hash"] = get_password_hash(update_data["password"])
            # Eliminar 'password' del dict (usamos 'password_hash')
            del update_data["password"]
        
        # Actualizar los campos del objeto
        for field, value in update_data.items():
            setattr(db_obj, field, value)
        
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj
    
    def authenticate(
        self, 
        db: Session, 
        *, 
        usuario: str, 
        password: str
    ) -> Optional[Usuario]:
        """
        Autenticar usuario verificando credenciales
        
        Args:
            db: Sesión de BD
            usuario: Nombre de usuario
            password: Contraseña en texto plano
            
        Returns:
            Instancia de Usuario si las credenciales son correctas, None si no
        """
        user = self.get_by_usuario(db, usuario=usuario)
        
        if not user:
            return None
        
        if not verify_password(password, user.password_hash):
            return None
        
        return user
    
    def is_active(self, user: Usuario) -> bool:
        """Verificar si el usuario está activo"""
        return user.activo
    
    def is_admin(self, user: Usuario) -> bool:
        """Verificar si el usuario es administrador"""
        return user.rol == "admin"
    
    def update_password(
        self, 
        db: Session, 
        *, 
        user: Usuario, 
        new_password: str
    ) -> Usuario:
        """
        Actualizar contraseña de usuario
        
        Args:
            db: Sesión de BD
            user: Instancia del usuario
            new_password: Nueva contraseña en texto plano
            
        Returns:
            Instancia de Usuario actualizada
        """
        user.password_hash = get_password_hash(new_password)
        user.primer_inicio = False
        db.add(user)
        db.commit()
        db.refresh(user)
        return user
    
    def search(
        self,
        db: Session,
        *,
        query: str,
        skip: int = 0,
        limit: int = 100
    ) -> List[Usuario]:
        """
        Buscar usuarios por nombre, usuario o email
        
        Args:
            db: Sesión de BD
            query: Término de búsqueda
            skip: Registros a saltar
            limit: Límite de resultados
            
        Returns:
            Lista de usuarios que coinciden con la búsqueda
        """
        return db.query(Usuario).filter(
            or_(
                Usuario.nombre_completo.ilike(f"%{query}%"),
                Usuario.usuario.ilike(f"%{query}%"),
                Usuario.email.ilike(f"%{query}%")
            )
        ).offset(skip).limit(limit).all()
    
    def get_by_rol(
        self,
        db: Session,
        *,
        rol: str,
        skip: int = 0,
        limit: int = 100
    ) -> List[Usuario]:
        """
        Obtener usuarios por rol
        
        Args:
            db: Sesión de BD
            rol: Rol a filtrar ('admin' o 'chofer')
            skip: Registros a saltar
            limit: Límite de resultados
            
        Returns:
            Lista de usuarios con el rol especificado
        """
        return db.query(Usuario).filter(
            Usuario.rol == rol
        ).offset(skip).limit(limit).all()


# Instancia global de CRUD para usuarios
user = CRUDUser(Usuario)