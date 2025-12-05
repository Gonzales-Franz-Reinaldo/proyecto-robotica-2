from typing import Optional, List, Dict, Any
from datetime import date, time
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import and_, or_

from app.crud.base import CRUDBase
from app.models.viaje import Viaje
from app.models.user import Usuario
from app.models.empresa import Empresa
from app.schemas.viaje import ViajeCreate, ViajeUpdate


class CRUDViaje(CRUDBase[Viaje, ViajeCreate, ViajeUpdate]):
    """
    CRUD específico para Viaje con métodos adicionales
    """
    
    def get(self, db: Session, id: int) -> Optional[Viaje]:
        """
        Obtener viaje por ID con información relacionada (JOIN)
        
        Args:
            db: Sesión de BD
            id: ID del viaje
            
        Returns:
            Instancia de Viaje con relaciones cargadas o None
        """
        return db.query(Viaje).options(
            joinedload(Viaje.chofer),
            joinedload(Viaje.empresa)
        ).filter(Viaje.id_viaje == id).first()
    
    def get_multi(
        self,
        db: Session,
        *,
        skip: int = 0,
        limit: int = 100
    ) -> List[Viaje]:
        """
        Obtener múltiples viajes con información relacionada (JOIN)
        
        Args:
            db: Sesión de BD
            skip: Registros a saltar
            limit: Límite de resultados
            
        Returns:
            Lista de viajes con relaciones cargadas
        """
        return db.query(Viaje).options(
            joinedload(Viaje.chofer),
            joinedload(Viaje.empresa)
        ).offset(skip).limit(limit).all()
    
    def get_multi_with_filters(
        self,
        db: Session,
        *,
        skip: int = 0,
        limit: int = 100,
        filters: Optional[Dict[str, Any]] = None
    ) -> tuple[List[Viaje], int]:
        """
        Obtener viajes con filtros opcionales y contar total
        
        Args:
            db: Sesión de BD
            skip: Registros a saltar
            limit: Límite de resultados
            filters: Diccionario con filtros opcionales
            
        Returns:
            Tupla (lista de viajes, total de registros)
        """
        query = db.query(Viaje).options(
            joinedload(Viaje.chofer),
            joinedload(Viaje.empresa)
        )
        
        # Aplicar filtros si existen
        if filters:
            if filters.get('id_chofer'):
                query = query.filter(Viaje.id_chofer == filters['id_chofer'])
            
            if filters.get('id_empresa'):
                query = query.filter(Viaje.id_empresa == filters['id_empresa'])
            
            if filters.get('estado'):
                query = query.filter(Viaje.estado == filters['estado'])
            
            if filters.get('origen'):
                query = query.filter(Viaje.origen.ilike(f"%{filters['origen']}%"))
            
            if filters.get('destino'):
                query = query.filter(Viaje.destino.ilike(f"%{filters['destino']}%"))
            
            if filters.get('fecha_viaje_programada'):
                query = query.filter(Viaje.fecha_viaje_programada == filters['fecha_viaje_programada'])
        
        # Contar total antes de paginar
        total = query.count()
        
        # Aplicar paginación y ordenar por fecha_asignacion descendente
        viajes = query.order_by(Viaje.fecha_asignacion.desc()).offset(skip).limit(limit).all()
        
        return viajes, total
    
    def validar_chofer_disponible_en_fecha_hora(
        self,
        db: Session,
        id_chofer: int,
        fecha_programada: date,
        hora_programada: time,
        id_viaje_excluir: Optional[int] = None
    ) -> bool:
        """
        Verifica que el chofer NO tenga otro viaje en la misma FECHA (sin importar la hora).
        Solo valida viajes pendientes o en curso.
        
        Args:
            db: Sesión de BD
            id_chofer: ID del chofer
            fecha_programada: Fecha del viaje
            hora_programada: Hora del viaje (no se usa en la validación, solo por compatibilidad)
            id_viaje_excluir: ID del viaje a excluir (en caso de edición)
            
        Returns:
            True si el chofer está disponible, False si ya tiene viaje
        """
        query = db.query(Viaje).filter(
            Viaje.id_chofer == id_chofer,
            Viaje.fecha_viaje_programada == fecha_programada,
            # NO validamos hora, solo fecha
            Viaje.estado.in_(['pendiente', 'en_curso'])
        )
        
        # En edición, excluir el viaje actual
        if id_viaje_excluir:
            query = query.filter(Viaje.id_viaje != id_viaje_excluir)
        
        viaje_existente = query.first()
        return viaje_existente is None  # True si NO existe, False si existe
    
    def get_by_chofer(
        self,
        db: Session,
        *,
        id_chofer: int,
        skip: int = 0,
        limit: int = 100
    ) -> List[Viaje]:
        """
        Obtener viajes de un chofer específico
        
        Args:
            db: Sesión de BD
            id_chofer: ID del chofer
            skip: Registros a saltar
            limit: Límite de resultados
            
        Returns:
            Lista de viajes del chofer
        """
        return db.query(Viaje).options(
            joinedload(Viaje.chofer),
            joinedload(Viaje.empresa)
        ).filter(
            Viaje.id_chofer == id_chofer
        ).order_by(
            Viaje.fecha_asignacion.desc()
        ).offset(skip).limit(limit).all()
    
    def get_by_empresa(
        self,
        db: Session,
        *,
        id_empresa: int,
        skip: int = 0,
        limit: int = 100
    ) -> List[Viaje]:
        """
        Obtener viajes de una empresa específica
        
        Args:
            db: Sesión de BD
            id_empresa: ID de la empresa
            skip: Registros a saltar
            limit: Límite de resultados
            
        Returns:
            Lista de viajes de la empresa
        """
        return db.query(Viaje).options(
            joinedload(Viaje.chofer),
            joinedload(Viaje.empresa)
        ).filter(
            Viaje.id_empresa == id_empresa
        ).order_by(
            Viaje.fecha_asignacion.desc()
        ).offset(skip).limit(limit).all()
    
    def get_by_estado(
        self,
        db: Session,
        *,
        estado: str,
        skip: int = 0,
        limit: int = 100
    ) -> List[Viaje]:
        """
        Obtener viajes por estado
        
        Args:
            db: Sesión de BD
            estado: Estado del viaje (pendiente, en_curso, completada, cancelada)
            skip: Registros a saltar
            limit: Límite de resultados
            
        Returns:
            Lista de viajes con el estado especificado
        """
        return db.query(Viaje).options(
            joinedload(Viaje.chofer),
            joinedload(Viaje.empresa)
        ).filter(
            Viaje.estado == estado
        ).order_by(
            Viaje.fecha_asignacion.desc()
        ).offset(skip).limit(limit).all()
    
    def create(self, db: Session, *, obj_in: ViajeCreate) -> Viaje:
        """
        Crear viaje
        
        Args:
            db: Sesión de BD
            obj_in: Schema con datos del viaje
            
        Returns:
            Instancia de Viaje creada con relaciones cargadas
            
        Raises:
            ValueError: Si el chofer ya tiene un viaje en la misma fecha y hora
        """
        # Validar disponibilidad del chofer en esa fecha y hora
        if not self.validar_chofer_disponible_en_fecha_hora(
            db, 
            obj_in.id_chofer, 
            obj_in.fecha_viaje_programada,
            obj_in.hora_viaje_programada
        ):
            raise ValueError(
                f"El chofer ya tiene un viaje asignado para el {obj_in.fecha_viaje_programada.strftime('%d/%m/%Y')}"
            )
        
        # Excluir el campo enviar_email ya que no se guarda en BD
        viaje_data = obj_in.dict(exclude={'enviar_email'})
        
        db_obj = Viaje(**viaje_data)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        
        # Cargar relaciones después de crear
        db_obj = self.get(db, id=db_obj.id_viaje)
        
        return db_obj
    
    def update(
        self,
        db: Session,
        *,
        db_obj: Viaje,
        obj_in: ViajeUpdate
    ) -> Viaje:
        """
        Actualizar viaje
        
        Args:
            db: Sesión de BD
            db_obj: Instancia actual del viaje
            obj_in: Schema con datos a actualizar
            
        Returns:
            Instancia de Viaje actualizada con relaciones cargadas
            
        Raises:
            ValueError: Si se cambia la fecha/hora y el chofer ya tiene otro viaje
        """
        # Obtener solo los campos que fueron proporcionados
        update_data = obj_in.dict(exclude_unset=True)
        
        # Si se cambia la fecha o hora programada, validar disponibilidad
        if 'fecha_viaje_programada' in update_data or 'hora_viaje_programada' in update_data:
            nueva_fecha = update_data.get('fecha_viaje_programada', db_obj.fecha_viaje_programada)
            nueva_hora = update_data.get('hora_viaje_programada', db_obj.hora_viaje_programada)
            
            if not self.validar_chofer_disponible_en_fecha_hora(
                db,
                db_obj.id_chofer,
                nueva_fecha,
                nueva_hora,
                id_viaje_excluir=db_obj.id_viaje
            ):
                raise ValueError(
                    f"El chofer ya tiene un viaje asignado para el {nueva_fecha.strftime('%d/%m/%Y')}"
                )
        
        # Actualizar los campos del objeto
        for field, value in update_data.items():
            setattr(db_obj, field, value)
        
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        
        # Recargar con relaciones
        db_obj = self.get(db, id=db_obj.id_viaje)
        
        return db_obj
    
    def delete(self, db: Session, *, id: int) -> Viaje:
        """
        Eliminar viaje (hard delete)
        
        Args:
            db: Sesión de BD
            id: ID del viaje a eliminar
            
        Returns:
            Instancia de Viaje eliminada
        """
        obj = self.get(db, id=id)
        if obj:
            db.delete(obj)
            db.commit()
        return obj
    
    def get_choferes_by_categoria(
        self,
        db: Session,
        *,
        categoria_licencia: str
    ) -> List[Usuario]:
        """
        Obtener choferes activos filtrados por categoría de licencia
        
        Args:
            db: Sesión de BD
            categoria_licencia: Categoría de licencia a filtrar
            
        Returns:
            Lista de choferes con la categoría especificada
        """
        return db.query(Usuario).options(
            joinedload(Usuario.empresa)
        ).filter(
            and_(
                Usuario.rol == "chofer",
                Usuario.activo == True,
                Usuario.categoria_licencia == categoria_licencia
            )
        ).order_by(Usuario.nombre_completo).all()
    
    def count_by_estado(self, db: Session, *, estado: str) -> int:
        """
        Contar viajes por estado
        
        Args:
            db: Sesión de BD
            estado: Estado del viaje
            
        Returns:
            Cantidad de viajes con el estado especificado
        """
        return db.query(Viaje).filter(Viaje.estado == estado).count()
    
    def get_viajes_activos_chofer(
        self,
        db: Session,
        *,
        id_chofer: int
    ) -> List[Viaje]:
        """
        Obtener viajes activos (pendientes o en curso) de un chofer
        
        Args:
            db: Sesión de BD
            id_chofer: ID del chofer
            
        Returns:
            Lista de viajes activos del chofer
        """
        return db.query(Viaje).filter(
            and_(
                Viaje.id_chofer == id_chofer,
                or_(
                    Viaje.estado == "pendiente",
                    Viaje.estado == "en_curso"
                )
            )
        ).order_by(Viaje.fecha_asignacion.desc()).all()


# Instancia global de CRUD para viajes
viaje = CRUDViaje(Viaje)