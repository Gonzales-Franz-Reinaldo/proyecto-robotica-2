from sqlalchemy import Column, Integer, String, Boolean, DateTime, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from app.db.base_class import Base


class Empresa(Base):
    """
    Modelo de Empresa de Transporte
    
    Empresas que emplean choferes para transporte
    """
    
    __tablename__ = "empresas"
    
    # Primary Key
    id_empresa = Column(Integer, primary_key=True, index=True)
    
    # ============================================
    # INFORMACIÓN DE LA EMPRESA
    # ============================================
    nombre_empresa = Column(String(200), nullable=False, unique=True, index=True)
    ruc = Column(String(20), unique=True, index=True)
    telefono = Column(String(20))
    email = Column(String(100), index=True)
    direccion = Column(Text)
    
    # ============================================
    # ESTADO
    # ============================================
    activo = Column(Boolean, default=True, nullable=False, index=True)
    
    # ============================================
    # METADATOS
    # ============================================
    fecha_registro = Column(DateTime(timezone=True), server_default=func.now())
    
    # ============================================
    # RELACIONES
    # ============================================
    choferes = relationship("Usuario", back_populates="empresa")
    
    def __repr__(self):
        return f"<Empresa {self.nombre_empresa}>"
    
    @property
    def total_choferes(self) -> int:
        """Contar choferes de esta empresa"""
        return len(self.choferes) if self.choferes else 0