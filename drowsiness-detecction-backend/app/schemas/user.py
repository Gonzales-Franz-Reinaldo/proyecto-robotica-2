from pydantic import BaseModel, EmailStr, Field, validator
from typing import Optional
from datetime import datetime, date
from enum import Enum


class RolEnum(str, Enum):
    """Roles disponibles en el sistema"""
    ADMIN = "admin"
    CHOFER = "chofer"


class GeneroEnum(str, Enum):
    """Géneros disponibles"""
    MASCULINO = "masculino"
    FEMENINO = "femenino"
    OTRO = "otro"


class TipoChoferEnum(str, Enum):
    """Tipos de chofer"""
    INDIVIDUAL = "individual"
    EMPRESA = "empresa"


# SCHEMAS DE AUTENTICACIÓN

class LoginRequest(BaseModel):
    """Schema para solicitud de login con JSON"""
    username: str = Field(..., min_length=3, max_length=50, description="Nombre de usuario")
    password: str = Field(..., min_length=6, description="Contraseña")
    
    class Config:
        json_schema_extra = {
            "example": {
                "username": "admin",
                "password": "admin123"
            }
        }


# SCHEMAS BASE

class UserBase(BaseModel):
    """Schema base para Usuario"""
    usuario: str = Field(..., min_length=3, max_length=50)
    email: EmailStr
    nombre_completo: str = Field(..., min_length=3, max_length=200)
    telefono: Optional[str] = Field(None, max_length=20)


# SCHEMAS PARA CREACIÓN

class UserCreate(UserBase):
    """Schema para crear un nuevo usuario"""
    password: str = Field(..., min_length=6)
    rol: RolEnum
    dni_ci: Optional[str] = Field(None, max_length=20)
    
    # Campos opcionales para CHOFER
    genero: Optional[GeneroEnum] = None
    nacionalidad: Optional[str] = None
    fecha_nacimiento: Optional[date] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    tipo_chofer: Optional[TipoChoferEnum] = None
    id_empresa: Optional[int] = None
    numero_licencia: Optional[str] = None
    categoria_licencia: Optional[str] = None
    
    # Campo para envío de email (no se guarda en BD)
    enviar_email: Optional[bool] = False
    
    @validator('id_empresa')
    def validate_empresa_for_chofer(cls, v, values):
        """Validar que choferes de empresa tengan id_empresa"""
        if values.get('tipo_chofer') == TipoChoferEnum.EMPRESA and not v:
            raise ValueError('Chofer de empresa debe tener id_empresa')
        return v
    
    class Config:
        json_schema_extra = {
            "example": {
                "usuario": "jperez",
                "password": "chofer123",
                "email": "jperez@ejemplo.com",
                "nombre_completo": "Juan Pérez García",
                "telefono": "+591 70123456",
                "rol": "chofer",
                "dni_ci": "12345678",
                "genero": "masculino",
                "tipo_chofer": "empresa",
                "id_empresa": 1
            }
        }


class UserUpdate(BaseModel):
    """Schema para actualizar un usuario"""
    usuario: Optional[str] = Field(None, min_length=3, max_length=50)
    password: Optional[str] = Field(None, min_length=6)  # Opcional - solo actualizar si se proporciona
    email: Optional[EmailStr] = None
    nombre_completo: Optional[str] = Field(None, min_length=3, max_length=200)
    telefono: Optional[str] = None
    dni_ci: Optional[str] = Field(None, max_length=20)
    genero: Optional[GeneroEnum] = None
    nacionalidad: Optional[str] = None
    fecha_nacimiento: Optional[date] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    tipo_chofer: Optional[TipoChoferEnum] = None
    id_empresa: Optional[int] = None
    numero_licencia: Optional[str] = None
    categoria_licencia: Optional[str] = None
    activo: Optional[bool] = None


class PasswordChange(BaseModel):
    """Schema para cambio de contraseña"""
    current_password: str = Field(..., min_length=6)
    new_password: str = Field(..., min_length=6)
    
    class Config:
        json_schema_extra = {
            "example": {
                "current_password": "password_actual",
                "new_password": "nueva_password_123"
            }
        }


class PasswordReset(BaseModel):
    """Schema para reseteo de contraseña por admin"""
    new_password: str = Field(..., min_length=6)
    
    class Config:
        json_schema_extra = {
            "example": {
                "new_password": "nuevo_password_temporal"
            }
        }


# SCHEMAS PARA RESPUESTA

class UserResponse(UserBase):
    """Schema para respuesta de usuario"""
    id_usuario: int
    rol: RolEnum
    dni_ci: Optional[str] = None
    genero: Optional[GeneroEnum] = None
    nacionalidad: Optional[str] = None
    fecha_nacimiento: Optional[date] = None
    direccion: Optional[str] = None
    ciudad: Optional[str] = None
    tipo_chofer: Optional[TipoChoferEnum] = None
    id_empresa: Optional[int] = None
    nombre_empresa: Optional[str] = None  # Nombre de la empresa (JOIN)
    numero_licencia: Optional[str] = None
    categoria_licencia: Optional[str] = None
    activo: bool
    primer_inicio: bool
    fecha_registro: datetime
    ultima_sesion: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class UserListResponse(BaseModel):
    """Schema para listado de usuarios con paginación"""
    total: int
    page: int
    page_size: int
    users: list[UserResponse]