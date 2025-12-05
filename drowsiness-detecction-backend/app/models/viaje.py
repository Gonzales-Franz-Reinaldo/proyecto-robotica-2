from sqlalchemy import (
    Column, Integer, String, DECIMAL, Text, DateTime, Date, Time,
    ForeignKey, CheckConstraint
)
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from app.db.base_class import Base


class Viaje(Base):
    """
    Modelo de Viaje
    
    Representa la asignación de un viaje/ruta a un chofer.
    Incluye información de origen, destino, duración estimada y estado.
    """
    
    __tablename__ = "viajes"
    
    # Primary Key
    id_viaje = Column(Integer, primary_key=True, index=True)
    
    # RELACIONES (Solo choferes pueden ser asignados)
    id_chofer = Column(
        Integer, 
        ForeignKey("usuarios.id_usuario", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    id_empresa = Column(
        Integer,
        ForeignKey("empresas.id_empresa", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    
    # INFORMACIÓN DE LA RUTA
    origen = Column(String(100), nullable=False, index=True)
    destino = Column(String(100), nullable=False, index=True)
    duracion_estimada = Column(String(50), nullable=False)
    distancia_km = Column(DECIMAL(8, 2))
    
    # ESTADO DEL VIAJE
    estado = Column(
        String(20),
        CheckConstraint("estado IN ('pendiente', 'en_curso', 'completada', 'cancelada')"),
        default='pendiente',
        nullable=False,
        index=True
    )
    
    # FECHAS
    fecha_asignacion = Column(DateTime(timezone=True), server_default=func.now(), index=True)
    fecha_viaje_programada = Column(Date, nullable=False, index=True)
    hora_viaje_programada = Column(Time, nullable=False)
    fecha_inicio = Column(DateTime(timezone=True))
    fecha_fin = Column(DateTime(timezone=True))
    
    # NOTAS ADICIONALES
    observaciones = Column(Text)
    
    # RELACIONES
    chofer = relationship("Usuario", foreign_keys=[id_chofer], backref="viajes_asignados")
    empresa = relationship("Empresa", foreign_keys=[id_empresa], backref="viajes")
    
    # VALIDACIONES A NIVEL DE MODELO
    __table_args__ = (
        CheckConstraint('origen != destino', name='chk_viaje_origen_destino'),
    )
    
    def __repr__(self):
        return f"<Viaje {self.id_viaje}: {self.origen} -> {self.destino} ({self.estado})>"
    
    @property
    def es_pendiente(self) -> bool:
        """Verificar si el viaje está pendiente"""
        return self.estado == "pendiente"
    
    @property
    def es_en_curso(self) -> bool:
        """Verificar si el viaje está en curso"""
        return self.estado == "en_curso"
    
    @property
    def es_completada(self) -> bool:
        """Verificar si el viaje está completado"""
        return self.estado == "completada"
    
    @property
    def es_cancelada(self) -> bool:
        """Verificar si el viaje está cancelado"""
        return self.estado == "cancelada"
    
    @property
    def nombre_chofer(self) -> str:
        """Obtener nombre del chofer"""
        return self.chofer.nombre_completo if self.chofer else ""
    
    @property
    def nombre_empresa(self) -> str:
        """Obtener nombre de la empresa"""
        return self.empresa.nombre_empresa if self.empresa else ""
    
    @property
    def ruta_completa(self) -> str:
        """Obtener ruta completa como string"""
        return f"{self.origen} - {self.destino}"