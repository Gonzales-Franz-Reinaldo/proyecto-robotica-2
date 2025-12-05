from pydantic import BaseModel, Field, validator
from typing import Optional, List
from datetime import datetime, date, time
from enum import Enum


class EstadoViajeEnum(str, Enum):
    """Estados disponibles para un viaje"""
    PENDIENTE = "pendiente"
    EN_CURSO = "en_curso"
    COMPLETADA = "completada"
    CANCELADA = "cancelada"


class CategoriaLicenciaEnum(str, Enum):
    """Categorías de licencia de conducir en Bolivia"""
    CATEGORIA_A = "Categoría A - Motocicletas"
    CATEGORIA_B = "Categoría B - Vehículos Livianos"
    CATEGORIA_C = "Categoría C - Vehículos Pesados"
    CATEGORIA_D = "Categoría D - Transporte Público"
    CATEGORIA_E = "Categoría E - Transporte Internacional"
    CATEGORIA_F = "Categoría F - Transporte de Carga Especial"


# SCHEMAS BASE

class ViajeBase(BaseModel):
    """Schema base para Viaje"""
    id_chofer: int = Field(..., gt=0, description="ID del chofer asignado")
    id_empresa: int = Field(..., gt=0, description="ID de la empresa")
    origen: str = Field(..., min_length=1, max_length=100, description="Departamento de origen")
    destino: str = Field(..., min_length=1, max_length=100, description="Departamento de destino")
    fecha_viaje_programada: date = Field(..., description="Fecha programada para el viaje")
    hora_viaje_programada: time = Field(..., description="Hora programada para el viaje")
    duracion_estimada: str = Field(..., min_length=1, max_length=50, description="Duración estimada del viaje")
    distancia_km: Optional[float] = Field(None, ge=0, description="Distancia en kilómetros")
    observaciones: Optional[str] = Field(None, description="Observaciones adicionales")
    
    @validator('destino')
    def validar_origen_destino_diferentes(cls, v, values):
        """Validar que el origen y destino sean diferentes"""
        if 'origen' in values and v == values['origen']:
            raise ValueError('El origen y destino deben ser diferentes')
        return v


# SCHEMAS PARA CREACIÓN

class ViajeCreate(ViajeBase):
    """Schema para crear un nuevo viaje"""
    # Campo para envío de email (no se guarda en BD, solo metadata)
    enviar_email: Optional[bool] = Field(False, description="Enviar notificación por email al chofer")
    
    class Config:
        json_schema_extra = {
            "example": {
                "id_chofer": 2,
                "id_empresa": 1,
                "origen": "La Paz",
                "destino": "Santa Cruz",
                "duracion_estimada": "12 horas 30 minutos",
                "distancia_km": 525.5,
                "observaciones": "Ruta principal por carretera",
                "enviar_email": True
            }
        }


# SCHEMAS PARA ACTUALIZACIÓN

class ViajeUpdate(BaseModel):
    """Schema para actualizar un viaje"""
    id_chofer: Optional[int] = Field(None, gt=0)
    id_empresa: Optional[int] = Field(None, gt=0)
    origen: Optional[str] = Field(None, min_length=1, max_length=100)
    destino: Optional[str] = Field(None, min_length=1, max_length=100)
    fecha_viaje_programada: Optional[date] = None
    hora_viaje_programada: Optional[time] = None
    duracion_estimada: Optional[str] = Field(None, min_length=1, max_length=50)
    distancia_km: Optional[float] = Field(None, ge=0)
    estado: Optional[EstadoViajeEnum] = None
    fecha_inicio: Optional[datetime] = None
    fecha_fin: Optional[datetime] = None
    observaciones: Optional[str] = None
    
    @validator('destino')
    def validar_origen_destino_diferentes(cls, v, values):
        """Validar que el origen y destino sean diferentes"""
        if 'origen' in values and values['origen'] and v and v == values['origen']:
            raise ValueError('El origen y destino deben ser diferentes')
        return v
    
    class Config:
        json_schema_extra = {
            "example": {
                "estado": "en_curso",
                "fecha_inicio": "2024-11-03T08:00:00",
                "observaciones": "Viaje iniciado sin problemas"
            }
        }


# SCHEMAS PARA RESPUESTA

class ViajeResponse(ViajeBase):
    """Schema de respuesta completo para un viaje"""
    id_viaje: int
    estado: EstadoViajeEnum
    fecha_asignacion: datetime
    fecha_viaje_programada: date
    hora_viaje_programada: time
    fecha_inicio: Optional[datetime] = None
    fecha_fin: Optional[datetime] = None
    
    # Información relacionada (joins)
    nombre_chofer: Optional[str] = None
    categoria_licencia: Optional[str] = None
    nombre_empresa: Optional[str] = None
    
    class Config:
        from_attributes = True
        json_schema_extra = {
            "example": {
                "id_viaje": 1,
                "id_chofer": 2,
                "id_empresa": 1,
                "origen": "La Paz",
                "destino": "Santa Cruz",
                "duracion_estimada": "12 horas 30 minutos",
                "distancia_km": 525.5,
                "estado": "pendiente",
                "fecha_asignacion": "2024-11-03T10:00:00",
                "fecha_inicio": None,
                "fecha_fin": None,
                "observaciones": "Ruta principal por carretera",
                "nombre_chofer": "Juan Pérez García",
                "categoria_licencia": "Categoría C - Vehículos Pesados",
                "nombre_empresa": "TransCorp SA"
            }
        }


# SCHEMAS PARA LISTADO CON PAGINACIÓN

class ViajeListResponse(BaseModel):
    """Schema de respuesta para listado paginado de viajes"""
    total: int = Field(..., description="Total de viajes")
    skip: int = Field(..., description="Número de registros omitidos")
    limit: int = Field(..., description="Número de registros por página")
    viajes: List[ViajeResponse] = Field(..., description="Lista de viajes")
    
    class Config:
        json_schema_extra = {
            "example": {
                "total": 25,
                "skip": 0,
                "limit": 10,
                "viajes": [
                    {
                        "id_viaje": 1,
                        "id_chofer": 2,
                        "id_empresa": 1,
                        "origen": "La Paz",
                        "destino": "Santa Cruz",
                        "duracion_estimada": "12 horas 30 minutos",
                        "distancia_km": 525.5,
                        "estado": "pendiente",
                        "fecha_asignacion": "2024-11-03T10:00:00",
                        "nombre_chofer": "Juan Pérez García",
                        "categoria_licencia": "Categoría C - Vehículos Pesados",
                        "nombre_empresa": "TransCorp SA"
                    }
                ]
            }
        }


# SCHEMA PARA FILTROS

class ViajeFilters(BaseModel):
    """Schema para filtros de búsqueda de viajes"""
    id_chofer: Optional[int] = None
    id_empresa: Optional[int] = None
    estado: Optional[EstadoViajeEnum] = None
    origen: Optional[str] = None
    destino: Optional[str] = None
    fecha_viaje_programada: Optional[date] = None
    skip: int = Field(0, ge=0)
    limit: int = Field(10, ge=1, le=100)
    
    class Config:
        json_schema_extra = {
            "example": {
                "id_empresa": 1,
                "estado": "pendiente",
                "skip": 0,
                "limit": 10
            }
        }


# SCHEMA PARA CHOFERES DISPONIBLES

class ChoferDisponible(BaseModel):
    """Schema para choferes disponibles por categoría de licencia"""
    id_usuario: int
    nombre_completo: str
    categoria_licencia: str
    id_empresa: Optional[int] = None
    nombre_empresa: Optional[str] = None
    
    class Config:
        from_attributes = True
        json_schema_extra = {
            "example": {
                "id_usuario": 2,
                "nombre_completo": "Juan Pérez García",
                "categoria_licencia": "Categoría C - Vehículos Pesados",
                "id_empresa": 1,
                "nombre_empresa": "TransCorp SA"
            }
        }


class ChoferesDisponiblesResponse(BaseModel):
    """Schema de respuesta para lista de choferes disponibles"""
    total: int
    choferes: List[ChoferDisponible]
    
    class Config:
        json_schema_extra = {
            "example": {
                "total": 5,
                "choferes": [
                    {
                        "id_usuario": 2,
                        "nombre_completo": "Juan Pérez García",
                        "categoria_licencia": "Categoría C - Vehículos Pesados",
                        "id_empresa": 1,
                        "nombre_empresa": "TransCorp SA"
                    }
                ]
            }
        }
