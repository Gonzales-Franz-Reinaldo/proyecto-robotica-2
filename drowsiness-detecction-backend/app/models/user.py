from sqlalchemy import (
    Column, Integer, String, Boolean, DateTime, Date, Text, 
    ForeignKey, CheckConstraint
)
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from app.db.base_class import Base


class Usuario(Base):
    """
    Modelo de Usuario (Admin y Chofer)
    
    Roles:
        - admin: Administrador del sistema
        - chofer: Conductor que usa el sistema de monitoreo
    """
    
    __tablename__ = "usuarios"
    
    # Primary Key
    id_usuario = Column(Integer, primary_key=True, index=True)
    
    # CREDENCIALES (Compartidas por ambos roles)
    usuario = Column(String(50), unique=True, nullable=False, index=True)
    password_hash = Column(String(255), nullable=False)
    rol = Column(
        String(20),
        CheckConstraint("rol IN ('admin', 'chofer')"),
        nullable=False,
        index=True
    )
    
    # DATOS PERSONALES
    nombre_completo = Column(String(200), nullable=False)
    dni_ci = Column(String(20), unique=True, index=True)
    email = Column(String(100), unique=True, nullable=False, index=True)
    telefono = Column(String(20))
    
    # Datos adicionales para CHOFER (NULL para admin)
    genero = Column(
        String(20),
        CheckConstraint("genero IN ('masculino', 'femenino', 'otro')")
    )
    nacionalidad = Column(String(100))
    fecha_nacimiento = Column(Date)
    direccion = Column(Text)
    ciudad = Column(String(100))
    codigo_postal = Column(String(20))
    
    # INFORMACIÓN LABORAL (Solo para CHOFER)
    tipo_chofer = Column(
        String(20),
        CheckConstraint("tipo_chofer IN ('individual', 'empresa')")
    )
    id_empresa = Column(Integer, ForeignKey("empresas.id_empresa", ondelete="SET NULL"))
    numero_licencia = Column(String(50))
    categoria_licencia = Column(String(50))
    
    # ESTADO Y CONTROL
    activo = Column(Boolean, default=True, index=True, nullable=False)
    primer_inicio = Column(Boolean, default=True, nullable=False)
    
    # METADATOS
    fecha_registro = Column(DateTime(timezone=True), server_default=func.now())
    ultima_sesion = Column(DateTime(timezone=True))
    
    # RELACIONES
    empresa = relationship("Empresa", back_populates="choferes")
    
    def __repr__(self):
        return f"<Usuario {self.usuario} ({self.rol})>"
    
    @property
    def is_admin(self) -> bool:
        """Verificar si el usuario es administrador"""
        return self.rol == "admin"
    
    @property
    def is_chofer(self) -> bool:
        """Verificar si el usuario es chofer"""
        return self.rol == "chofer"
    
    @property
    def nombre_corto(self) -> str:
        """Obtener primer nombre"""
        return self.nombre_completo.split()[0] if self.nombre_completo else ""
    
    @property
    def nombre_empresa(self) -> str:
        """Obtener nombre de la empresa (si tiene)"""
        return self.empresa.nombre_empresa if self.empresa else "Individual"