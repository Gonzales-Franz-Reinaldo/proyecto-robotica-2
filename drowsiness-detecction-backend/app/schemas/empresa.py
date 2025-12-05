from pydantic import BaseModel, Field, EmailStr
from typing import Optional
from datetime import datetime


# SCHEMAS BASE

class EmpresaBase(BaseModel):
    """Schema base para Empresa"""
    nombre_empresa: str = Field(..., min_length=3, max_length=200)
    ruc: Optional[str] = Field(None, max_length=20)
    telefono: Optional[str] = Field(None, max_length=20)
    email: Optional[EmailStr] = None
    direccion: Optional[str] = None


# SCHEMAS PARA CREACIÓN

class EmpresaCreate(EmpresaBase):
    """Schema para crear una empresa"""
    pass


class EmpresaUpdate(BaseModel):
    """Schema para actualizar una empresa"""
    nombre_empresa: Optional[str] = Field(None, min_length=3, max_length=200)
    ruc: Optional[str] = Field(None, max_length=20)
    telefono: Optional[str] = Field(None, max_length=20)
    email: Optional[EmailStr] = None
    direccion: Optional[str] = None
    activo: Optional[bool] = None


# SCHEMAS PARA RESPUESTA

class EmpresaResponse(EmpresaBase):
    """Schema para respuesta de empresa"""
    id_empresa: int
    activo: bool
    fecha_registro: datetime
    
    class Config:
        from_attributes = True


class EmpresaListResponse(BaseModel):
    """Schema para listado de empresas con paginación"""
    total: int
    page: int
    page_size: int
    empresas: list[EmpresaResponse]


class EmpresaWithChoferes(EmpresaResponse):
    """Schema de empresa con conteo de choferes"""
    total_choferes: int