from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query, Body
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_admin_user, get_current_user
from app.schemas.viaje import (
    ViajeCreate,
    ViajeUpdate,
    ViajeResponse,
    ViajeListResponse,
    ViajeFilters,
    ChoferDisponible,
    ChoferesDisponiblesResponse,
    EstadoViajeEnum,
    CategoriaLicenciaEnum
)
from app.crud.viaje import viaje as viaje_crud
from app.crud.user import user as user_crud
from app.crud.empresa import empresa as empresa_crud
from app.models.user import Usuario
from app.services.email import email_service
from datetime import date, time as time_type
import logging

# Configurar logger
logger = logging.getLogger(__name__)

router = APIRouter()


@router.get(
    "/validar-disponibilidad",
    summary="Validar disponibilidad de chofer para fecha",
    description="**Solo Admin** puede validar si un chofer está disponible en una fecha específica (sin importar la hora)"
)
def validar_disponibilidad_chofer(
    *,
    db: Session = Depends(get_db),
    id_chofer: int = Query(..., description="ID del chofer a validar"),
    fecha_viaje_programada: str = Query(..., description="Fecha programada (YYYY-MM-DD)"),
    hora_viaje_programada: str = Query(None, description="Hora programada (opcional, no se usa en validación)"),
    id_viaje_excluir: Optional[int] = Query(None, description="ID del viaje a excluir (para modo edición)"),
    current_user: Usuario = Depends(get_current_admin_user)
):
    """
    Valida si un chofer está disponible en una FECHA específica (no valida hora).
    Un chofer solo puede tener un viaje por día.
    
    Retorna:
    - disponible: true si el chofer está disponible, false si ya tiene un viaje ese día
    - mensaje: Mensaje descriptivo del resultado
    - viaje_existente: (opcional) Datos del viaje que causa el conflicto
    """
    
    try:
        # Parsear fecha
        fecha_obj = date.fromisoformat(fecha_viaje_programada)
        
        # Hora dummy (no se usa en validación, solo por compatibilidad con el método)
        hora_obj = time_type(0, 0, 0)
        
    except (ValueError, IndexError) as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Formato de fecha inválido: {str(e)}"
        )
    
    # Validar disponibilidad usando el método del CRUD (solo valida fecha)
    disponible = viaje_crud.validar_chofer_disponible_en_fecha_hora(
        db=db,
        id_chofer=id_chofer,
        fecha_programada=fecha_obj,
        hora_programada=hora_obj,  # No se usa en validación
        id_viaje_excluir=id_viaje_excluir
    )
    
    if disponible:
        return {
            "disponible": True,
            "mensaje": "El chofer está disponible para esta fecha"
        }
    else:
        # Buscar el viaje existente para dar más detalles (solo por fecha)
        from app.models.viaje import Viaje
        query = db.query(Viaje).filter(
            Viaje.id_chofer == id_chofer,
            Viaje.fecha_viaje_programada == fecha_obj,
            Viaje.estado.in_(['pendiente', 'en_curso'])
        )
        
        # En edición, excluir el viaje actual
        if id_viaje_excluir:
            query = query.filter(Viaje.id_viaje != id_viaje_excluir)
        
        viaje_existente = query.first()
        
        fecha_formateada = fecha_obj.strftime('%d/%m/%Y')
        
        response = {
            "disponible": False,
            "mensaje": f"El chofer ya tiene un viaje asignado para el {fecha_formateada}"
        }
        
        if viaje_existente:
            response["viaje_existente"] = {
                "id_viaje": viaje_existente.id_viaje,
                "origen": viaje_existente.origen,
                "destino": viaje_existente.destino,
                "estado": viaje_existente.estado,
                "hora": viaje_existente.hora_viaje_programada.strftime('%H:%M') if viaje_existente.hora_viaje_programada else None
            }
        
        return response


@router.post(
    "/",
    response_model=ViajeResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Crear nuevo viaje",
    description="**Solo Admin** puede asignar nuevos viajes a choferes"
)
def create_viaje(
    *,
    db: Session = Depends(get_db),
    viaje_in: ViajeCreate = Body(
        ...,
        examples={
            "viaje_completo": {
                "summary": "Viaje con todos los datos",
                "description": "Viaje completo con distancia y observaciones",
                "value": {
                    "id_chofer": 2,
                    "id_empresa": 1,
                    "origen": "La Paz",
                    "destino": "Santa Cruz",
                    "duracion_estimada": "12 horas 30 minutos",
                    "distancia_km": 525.5,
                    "observaciones": "Ruta principal por carretera",
                    "enviar_email": True
                }
            },
            "viaje_simple": {
                "summary": "Viaje con datos mínimos",
                "description": "Viaje solo con datos requeridos",
                "value": {
                    "id_chofer": 3,
                    "id_empresa": 1,
                    "origen": "Cochabamba",
                    "destino": "Tarija",
                    "duracion_estimada": "8 horas",
                    "enviar_email": False
                }
            }
        }
    ),
    current_user: Usuario = Depends(get_current_admin_user)
):
    """
    Crear un nuevo viaje/ruta asignado a un chofer
    
    Validaciones:
    - Chofer debe existir y estar activo
    - Chofer debe tener rol 'chofer'
    - Empresa debe existir
    - Empresa del chofer debe coincidir con id_empresa del viaje
    - Origen y destino deben ser diferentes
    """
    
    # Validar que el chofer existe y está activo
    chofer = user_crud.get(db, id=viaje_in.id_chofer)
    if not chofer:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Chofer no encontrado"
        )
    
    if not chofer.is_chofer:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El usuario especificado no es un chofer"
        )
    
    if not chofer.activo:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El chofer no está activo"
        )
    
    # Validar que la empresa existe
    empresa = empresa_crud.get(db, id=viaje_in.id_empresa)
    if not empresa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Empresa no encontrada"
        )
    
    # Validar que la empresa del chofer coincide con la del viaje
    if chofer.id_empresa != viaje_in.id_empresa:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"El chofer pertenece a otra empresa (ID: {chofer.id_empresa})"
        )
    
    # Validar origen != destino (ya está en el schema pero reforzamos aquí)
    if viaje_in.origen == viaje_in.destino:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El origen y destino deben ser diferentes"
        )
    
    # Guardar flag de email antes de crear
    enviar_email = viaje_in.enviar_email
    
    # Crear viaje con validación de disponibilidad
    try:
        viaje = viaje_crud.create(db, obj_in=viaje_in)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    
    # Enviar email con detalles del viaje si el checkbox está marcado
    if enviar_email:
        # Validar que el chofer tenga un email válido
        if not chofer.email or not chofer.email.strip():
            logger.warning(f"⚠️ No se puede enviar email: el chofer {chofer.nombre_completo} no tiene un email registrado")
        else:
            try:
                # Formatear fecha y hora para el email
                fecha_formateada = viaje.fecha_viaje_programada.strftime('%d/%m/%Y')
                hora_formateada = viaje.hora_viaje_programada.strftime('%H:%M')
                
                email_enviado = email_service.enviar_asignacion_viaje(
                    email=chofer.email,
                    nombre_chofer=chofer.nombre_completo,
                    origen=viaje.origen,
                    destino=viaje.destino,
                    fecha_programada=fecha_formateada,
                    hora_programada=hora_formateada,
                    duracion_estimada=viaje.duracion_estimada,
                    distancia_km=viaje.distancia_km,
                    observaciones=viaje.observaciones,
                    nombre_empresa=empresa.nombre_empresa if empresa else None
                )
                if email_enviado:
                    logger.info(f"✉️ Detalles del viaje enviados a {chofer.email}")
                else:
                    logger.warning(f"⚠️ No se pudo enviar email a {chofer.email}")
            except Exception as e:
                # No fallar la creación del viaje si falla el email
                logger.error(f"⚠️ Error enviando viaje a {chofer.email}: {str(e)}")
    
    # Construir respuesta con datos relacionados
    response = ViajeResponse(
        id_viaje=viaje.id_viaje,
        id_chofer=viaje.id_chofer,
        id_empresa=viaje.id_empresa,
        origen=viaje.origen,
        destino=viaje.destino,
        duracion_estimada=viaje.duracion_estimada,
        distancia_km=viaje.distancia_km,
        estado=viaje.estado,
        fecha_asignacion=viaje.fecha_asignacion,
        fecha_viaje_programada=viaje.fecha_viaje_programada,
        hora_viaje_programada=viaje.hora_viaje_programada,
        fecha_inicio=viaje.fecha_inicio,
        fecha_fin=viaje.fecha_fin,
        observaciones=viaje.observaciones,
        nombre_chofer=viaje.chofer.nombre_completo if viaje.chofer else None,
        categoria_licencia=viaje.chofer.categoria_licencia if viaje.chofer else None,
        nombre_empresa=viaje.empresa.nombre_empresa if viaje.empresa else None
    )
    
    return response


@router.get(
    "/",
    response_model=ViajeListResponse,
    summary="Listar viajes con filtros",
    description="**Solo Admin** puede listar viajes. Soporta filtros y paginación"
)
def list_viajes(
    *,
    db: Session = Depends(get_db),
    skip: int = Query(0, ge=0, description="Número de registros a omitir"),
    limit: int = Query(10, ge=1, le=100, description="Número de registros por página"),
    id_chofer: Optional[int] = Query(None, description="Filtrar por ID de chofer"),
    id_empresa: Optional[int] = Query(None, description="Filtrar por ID de empresa"),
    estado: Optional[EstadoViajeEnum] = Query(None, description="Filtrar por estado del viaje"),
    origen: Optional[str] = Query(None, description="Filtrar por origen"),
    destino: Optional[str] = Query(None, description="Filtrar por destino"),
    current_user: Usuario = Depends(get_current_admin_user)
):
    """
    Listar viajes con filtros opcionales y paginación
    
    Filtros disponibles:
    - id_chofer: Filtrar viajes de un chofer específico
    - id_empresa: Filtrar viajes de una empresa específica
    - estado: Filtrar por estado (pendiente, en_curso, completada, cancelada)
    - origen: Buscar por origen (búsqueda parcial)
    - destino: Buscar por destino (búsqueda parcial)
    """
    
    # Construir filtros
    filters = {}
    if id_chofer:
        filters['id_chofer'] = id_chofer
    if id_empresa:
        filters['id_empresa'] = id_empresa
    if estado:
        filters['estado'] = estado
    if origen:
        filters['origen'] = origen
    if destino:
        filters['destino'] = destino
    
    # Obtener viajes con filtros
    viajes, total = viaje_crud.get_multi_with_filters(
        db,
        skip=skip,
        limit=limit,
        filters=filters if filters else None
    )
    
    # Construir respuesta
    viajes_response = []
    for viaje in viajes:
        viajes_response.append(
            ViajeResponse(
                id_viaje=viaje.id_viaje,
                id_chofer=viaje.id_chofer,
                id_empresa=viaje.id_empresa,
                origen=viaje.origen,
                destino=viaje.destino,
                duracion_estimada=viaje.duracion_estimada,
                distancia_km=viaje.distancia_km,
                estado=viaje.estado,
                fecha_asignacion=viaje.fecha_asignacion,
                fecha_viaje_programada=viaje.fecha_viaje_programada,
                hora_viaje_programada=viaje.hora_viaje_programada,
                fecha_inicio=viaje.fecha_inicio,
                fecha_fin=viaje.fecha_fin,
                observaciones=viaje.observaciones,
                nombre_chofer=viaje.chofer.nombre_completo if viaje.chofer else None,
                categoria_licencia=viaje.chofer.categoria_licencia if viaje.chofer else None,
                nombre_empresa=viaje.empresa.nombre_empresa if viaje.empresa else None
            )
        )
    
    return ViajeListResponse(
        total=total,
        skip=skip,
        limit=limit,
        viajes=viajes_response
    )


@router.get(
    "/{id_viaje}",
    response_model=ViajeResponse,
    summary="Obtener viaje por ID",
    description="**Admin o Chofer** puede obtener detalles de un viaje específico"
)
def get_viaje(
    *,
    db: Session = Depends(get_db),
    id_viaje: int,
    current_user: Usuario = Depends(get_current_user)
):
    """
    Obtener un viaje específico por su ID
    
    - Admin puede ver cualquier viaje
    - Chofer solo puede ver sus propios viajes
    """
    
    viaje = viaje_crud.get(db, id=id_viaje)
    
    if not viaje:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Viaje no encontrado"
        )
    
    # Si es chofer, validar que sea su viaje
    if current_user.is_chofer and viaje.id_chofer != current_user.id_usuario:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tiene permiso para ver este viaje"
        )
    
    return ViajeResponse(
        id_viaje=viaje.id_viaje,
        id_chofer=viaje.id_chofer,
        id_empresa=viaje.id_empresa,
        origen=viaje.origen,
        destino=viaje.destino,
        duracion_estimada=viaje.duracion_estimada,
        distancia_km=viaje.distancia_km,
        estado=viaje.estado,
        fecha_asignacion=viaje.fecha_asignacion,
        fecha_viaje_programada=viaje.fecha_viaje_programada,
        hora_viaje_programada=viaje.hora_viaje_programada,
        fecha_inicio=viaje.fecha_inicio,
        fecha_fin=viaje.fecha_fin,
        observaciones=viaje.observaciones,
        nombre_chofer=viaje.chofer.nombre_completo if viaje.chofer else None,
        categoria_licencia=viaje.chofer.categoria_licencia if viaje.chofer else None,
        nombre_empresa=viaje.empresa.nombre_empresa if viaje.empresa else None
    )


@router.put(
    "/{id_viaje}",
    response_model=ViajeResponse,
    summary="Actualizar viaje",
    description="**Solo Admin** puede actualizar viajes"
)
def update_viaje(
    *,
    db: Session = Depends(get_db),
    id_viaje: int,
    viaje_in: ViajeUpdate,
    current_user: Usuario = Depends(get_current_admin_user)
):
    """
    Actualizar un viaje existente
    
    Puede actualizar:
    - Datos del viaje (chofer, empresa, origen, destino, duración, distancia)
    - Estado del viaje
    - Fechas (inicio, fin)
    - Observaciones
    """
    
    viaje = viaje_crud.get(db, id=id_viaje)
    
    if not viaje:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Viaje no encontrado"
        )
    
    # Si se cambia el chofer, validar que existe y está activo
    if viaje_in.id_chofer:
        chofer = user_crud.get(db, id=viaje_in.id_chofer)
        if not chofer:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Chofer no encontrado"
            )
        if not chofer.is_chofer:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El usuario especificado no es un chofer"
            )
        if not chofer.activo:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El chofer no está activo"
            )
    
    # Si se cambia la empresa, validar que existe
    if viaje_in.id_empresa:
        empresa = empresa_crud.get(db, id=viaje_in.id_empresa)
        if not empresa:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Empresa no encontrada"
            )
    
    # Actualizar viaje con validación de disponibilidad
    try:
        viaje_updated = viaje_crud.update(db, db_obj=viaje, obj_in=viaje_in)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    
    return ViajeResponse(
        id_viaje=viaje_updated.id_viaje,
        id_chofer=viaje_updated.id_chofer,
        id_empresa=viaje_updated.id_empresa,
        origen=viaje_updated.origen,
        destino=viaje_updated.destino,
        duracion_estimada=viaje_updated.duracion_estimada,
        distancia_km=viaje_updated.distancia_km,
        estado=viaje_updated.estado,
        fecha_asignacion=viaje_updated.fecha_asignacion,
        fecha_viaje_programada=viaje_updated.fecha_viaje_programada,
        hora_viaje_programada=viaje_updated.hora_viaje_programada,
        fecha_inicio=viaje_updated.fecha_inicio,
        fecha_fin=viaje_updated.fecha_fin,
        observaciones=viaje_updated.observaciones,
        nombre_chofer=viaje_updated.chofer.nombre_completo if viaje_updated.chofer else None,
        categoria_licencia=viaje_updated.chofer.categoria_licencia if viaje_updated.chofer else None,
        nombre_empresa=viaje_updated.empresa.nombre_empresa if viaje_updated.empresa else None
    )


@router.delete(
    "/{id_viaje}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Eliminar viaje",
    description="**Solo Admin** puede eliminar viajes"
)
def delete_viaje(
    *,
    db: Session = Depends(get_db),
    id_viaje: int,
    current_user: Usuario = Depends(get_current_admin_user)
):
    """
    Eliminar un viaje permanentemente
    
    ADVERTENCIA: Esta operación no se puede deshacer
    """
    
    viaje = viaje_crud.get(db, id=id_viaje)
    
    if not viaje:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Viaje no encontrado"
        )
    
    viaje_crud.delete(db, id=id_viaje)
    
    return None


@router.get(
    "/chofer/{id_chofer}",
    response_model=ViajeListResponse,
    summary="Obtener viajes de un chofer",
    description="**Admin o el mismo Chofer** puede ver los viajes asignados a un chofer"
)
def get_viajes_by_chofer(
    *,
    db: Session = Depends(get_db),
    id_chofer: int,
    skip: int = Query(0, ge=0),
    limit: int = Query(10, ge=1, le=100),
    current_user: Usuario = Depends(get_current_user)
):
    """
    Obtener todos los viajes asignados a un chofer específico
    """
    
    # Si es chofer, validar que sea él mismo
    if current_user.is_chofer and id_chofer != current_user.id_usuario:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tiene permiso para ver viajes de otro chofer"
        )
    
    # Validar que el chofer existe
    chofer = user_crud.get(db, id=id_chofer)
    if not chofer or not chofer.is_chofer:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Chofer no encontrado"
        )
    
    viajes = viaje_crud.get_by_chofer(db, id_chofer=id_chofer, skip=skip, limit=limit)
    
    # Contar total
    total = len(viaje_crud.get_by_chofer(db, id_chofer=id_chofer, skip=0, limit=999999))
    
    viajes_response = []
    for viaje in viajes:
        viajes_response.append(
            ViajeResponse(
                id_viaje=viaje.id_viaje,
                id_chofer=viaje.id_chofer,
                id_empresa=viaje.id_empresa,
                origen=viaje.origen,
                destino=viaje.destino,
                duracion_estimada=viaje.duracion_estimada,
                distancia_km=viaje.distancia_km,
                estado=viaje.estado,
                fecha_asignacion=viaje.fecha_asignacion,
                fecha_viaje_programada=viaje.fecha_viaje_programada,
                hora_viaje_programada=viaje.hora_viaje_programada,
                fecha_inicio=viaje.fecha_inicio,
                fecha_fin=viaje.fecha_fin,
                observaciones=viaje.observaciones,
                nombre_chofer=viaje.chofer.nombre_completo if viaje.chofer else None,
                categoria_licencia=viaje.chofer.categoria_licencia if viaje.chofer else None,
                nombre_empresa=viaje.empresa.nombre_empresa if viaje.empresa else None
            )
        )
    
    return ViajeListResponse(
        total=total,
        skip=skip,
        limit=limit,
        viajes=viajes_response
    )


@router.get(
    "/empresa/{id_empresa}",
    response_model=ViajeListResponse,
    summary="Obtener viajes de una empresa",
    description="**Solo Admin** puede ver los viajes de una empresa"
)
def get_viajes_by_empresa(
    *,
    db: Session = Depends(get_db),
    id_empresa: int,
    skip: int = Query(0, ge=0),
    limit: int = Query(10, ge=1, le=100),
    current_user: Usuario = Depends(get_current_admin_user)
):
    """
    Obtener todos los viajes asignados a choferes de una empresa específica
    """
    
    # Validar que la empresa existe
    empresa = empresa_crud.get(db, id=id_empresa)
    if not empresa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Empresa no encontrada"
        )
    
    viajes = viaje_crud.get_by_empresa(db, id_empresa=id_empresa, skip=skip, limit=limit)
    
    # Contar total
    total = len(viaje_crud.get_by_empresa(db, id_empresa=id_empresa, skip=0, limit=999999))
    
    viajes_response = []
    for viaje in viajes:
        viajes_response.append(
            ViajeResponse(
                id_viaje=viaje.id_viaje,
                id_chofer=viaje.id_chofer,
                id_empresa=viaje.id_empresa,
                origen=viaje.origen,
                destino=viaje.destino,
                duracion_estimada=viaje.duracion_estimada,
                distancia_km=viaje.distancia_km,
                estado=viaje.estado,
                fecha_asignacion=viaje.fecha_asignacion,
                fecha_viaje_programada=viaje.fecha_viaje_programada,
                hora_viaje_programada=viaje.hora_viaje_programada,
                fecha_inicio=viaje.fecha_inicio,
                fecha_fin=viaje.fecha_fin,
                observaciones=viaje.observaciones,
                nombre_chofer=viaje.chofer.nombre_completo if viaje.chofer else None,
                categoria_licencia=viaje.chofer.categoria_licencia if viaje.chofer else None,
                nombre_empresa=viaje.empresa.nombre_empresa if viaje.empresa else None
            )
        )
    
    return ViajeListResponse(
        total=total,
        skip=skip,
        limit=limit,
        viajes=viajes_response
    )


@router.get(
    "/choferes-disponibles/por-categoria",
    response_model=ChoferesDisponiblesResponse,
    summary="Obtener choferes disponibles por categoría de licencia",
    description="**Solo Admin** puede obtener lista de choferes activos filtrados por categoría"
)
def get_choferes_disponibles(
    *,
    db: Session = Depends(get_db),
    categoria_licencia: CategoriaLicenciaEnum = Query(..., description="Categoría de licencia requerida"),
    current_user: Usuario = Depends(get_current_admin_user)
):
    """
    Obtener choferes activos filtrados por categoría de licencia
    
    Útil para el formulario de asignación de viajes, donde se necesita
    seleccionar un chofer con una categoría de licencia específica
    """
    
    choferes = viaje_crud.get_choferes_by_categoria(
        db,
        categoria_licencia=categoria_licencia.value
    )
    
    choferes_response = []
    for chofer in choferes:
        choferes_response.append(
            ChoferDisponible(
                id_usuario=chofer.id_usuario,
                nombre_completo=chofer.nombre_completo,
                categoria_licencia=chofer.categoria_licencia,
                id_empresa=chofer.id_empresa,
                nombre_empresa=chofer.empresa.nombre_empresa if chofer.empresa else None
            )
        )
    
    return ChoferesDisponiblesResponse(
        total=len(choferes_response),
        choferes=choferes_response
    )
