from typing import List
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_admin_user
from app.schemas.empresa import (
    EmpresaCreate,
    EmpresaUpdate,
    EmpresaResponse,
    EmpresaListResponse,
    EmpresaWithChoferes
)
from app.crud.empresa import empresa as empresa_crud
from app.models.user import Usuario

router = APIRouter()


@router.post(
    "/",
    response_model=EmpresaResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Crear nueva empresa",
    description="Crea una nueva empresa de transporte. **Solo Admin**"
)
def create_empresa(
    *,
    db: Session = Depends(get_db),
    empresa_in: EmpresaCreate,
    current_user: Usuario = Depends(get_current_admin_user)
):
    # Verificar si ya existe
    existing_empresa = empresa_crud.get_by_nombre(
        db, nombre_empresa=empresa_in.nombre_empresa
    )
    
    if existing_empresa:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Empresa con este nombre ya existe"
        )
    
    if empresa_in.ruc:
        existing_ruc = empresa_crud.get_by_ruc(db, ruc=empresa_in.ruc)
        if existing_ruc:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="RUC ya registrado"
            )
    
    empresa = empresa_crud.create(db, obj_in=empresa_in)
    return empresa


@router.get(
    "/",
    response_model=EmpresaListResponse,
    summary="Listar empresas",
    description="Obtiene lista paginada de empresas. **Solo Admin**"
)
def list_empresas(
    db: Session = Depends(get_db),
    skip: int = Query(0, ge=0, description="Registros a saltar"),
    limit: int = Query(100, ge=1, le=100, description="Límite de resultados"),
    activo: bool = Query(None, description="Filtrar por estado activo"),
    current_user: Usuario = Depends(get_current_admin_user)
):
    
    if activo is not None:
        if activo:
            empresas = empresa_crud.get_multi_active(db, skip=skip, limit=limit)
        else:
            empresas = [e for e in empresa_crud.get_multi(db, skip=skip, limit=limit) if not e.activo]
        total = len(empresas)
    else:
        empresas = empresa_crud.get_multi(db, skip=skip, limit=limit)
        total = db.query(empresa_crud.model).count()
    
    return {
        "total": total,
        "page": (skip // limit) + 1,
        "page_size": limit,
        "empresas": empresas
    }


@router.get(
    "/search",
    response_model=List[EmpresaResponse],
    summary="Buscar empresas",
    description="Busca empresas por nombre o RUC. **Solo Admin**"
)
def search_empresas(
    *,
    db: Session = Depends(get_db),
    q: str = Query(..., min_length=2, description="Término de búsqueda"),
    current_user: Usuario = Depends(get_current_admin_user)
):
    empresas = empresa_crud.search(db, query=q)
    return empresas


@router.get(
    "/{id_empresa}",
    response_model=EmpresaWithChoferes,
    summary="Obtener empresa por ID",
    description="Obtiene detalles de una empresa incluyendo conteo de choferes. **Solo Admin**"
)
def get_empresa(
    *,
    db: Session = Depends(get_db),
    id_empresa: int,
    current_user: Usuario = Depends(get_current_admin_user)
):

    empresa = empresa_crud.get_with_chofer_count(db, id_empresa=id_empresa)
    
    if not empresa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Empresa no encontrada"
        )
    
    return empresa


@router.put(
    "/{id_empresa}",
    response_model=EmpresaResponse,
    summary="Actualizar empresa",
    description="Actualiza información de una empresa. **Solo Admin**"
)
def update_empresa(
    *,
    db: Session = Depends(get_db),
    id_empresa: int,
    empresa_in: EmpresaUpdate,
    current_user: Usuario = Depends(get_current_admin_user)
):
    empresa = empresa_crud.get(db, id=id_empresa)
    
    if not empresa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Empresa no encontrada"
        )
    
    # Verificar nombre duplicado
    if empresa_in.nombre_empresa and empresa_in.nombre_empresa != empresa.nombre_empresa:
        existing = empresa_crud.get_by_nombre(db, nombre_empresa=empresa_in.nombre_empresa)
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Nombre de empresa ya existe"
            )
    
    empresa = empresa_crud.update(db, db_obj=empresa, obj_in=empresa_in)
    return empresa


@router.delete(
    "/{id_empresa}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Eliminar empresa",
    description="Elimina una empresa del sistema. **Solo Admin**"
)
def delete_empresa(
    *,
    db: Session = Depends(get_db),
    id_empresa: int,
    current_user: Usuario = Depends(get_current_admin_user)
):
    
    empresa = empresa_crud.get(db, id=id_empresa)
    
    if not empresa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Empresa no encontrada"
        )
    
    # Verificar si tiene choferes
    empresa_data = empresa_crud.get_with_chofer_count(db, id_empresa=id_empresa)
    if empresa_data and empresa_data.get("total_choferes", 0) > 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No se puede eliminar empresa con choferes asociados"
        )
    
    empresa_crud.remove(db, id=id_empresa)
    return None


@router.patch(
    "/{id_empresa}/activate",
    response_model=EmpresaResponse,
    summary="Activar empresa",
    description="Activa una empresa desactivada. **Solo Admin**"
)
def activate_empresa(
    *,
    db: Session = Depends(get_db),
    id_empresa: int,
    current_user: Usuario = Depends(get_current_admin_user)
):

    empresa = empresa_crud.get(db, id=id_empresa)
    
    if not empresa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Empresa no encontrada"
        )
    
    empresa = empresa_crud.activate(db, id_empresa=id_empresa)
    return empresa


@router.patch(
    "/{id_empresa}/deactivate",
    response_model=EmpresaResponse,
    summary="Desactivar empresa",
    description="Desactiva una empresa. **Solo Admin**"
)
def deactivate_empresa(
    *,
    db: Session = Depends(get_db),
    id_empresa: int,
    current_user: Usuario = Depends(get_current_admin_user)
):
    
    empresa = empresa_crud.get(db, id=id_empresa)
    
    if not empresa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Empresa no encontrada"
        )
    
    empresa = empresa_crud.deactivate(db, id_empresa=id_empresa)
    return empresa