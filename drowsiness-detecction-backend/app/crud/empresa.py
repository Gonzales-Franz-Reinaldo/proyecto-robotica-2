from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import func

from app.crud.base import CRUDBase
from app.models.empresa import Empresa
from app.models.user import Usuario
from app.schemas.empresa import EmpresaCreate, EmpresaUpdate


class CRUDEmpresa(CRUDBase[Empresa, EmpresaCreate, EmpresaUpdate]):
    """
    CRUD específico para Empresa con métodos adicionales
    """
    
    def get_by_nombre(self, db: Session, *, nombre_empresa: str) -> Optional[Empresa]:
        """Obtener empresa por nombre"""
        return db.query(Empresa).filter(
            Empresa.nombre_empresa == nombre_empresa
        ).first()
    
    def get_by_ruc(self, db: Session, *, ruc: str) -> Optional[Empresa]:
        """Obtener empresa por RUC"""
        return db.query(Empresa).filter(Empresa.ruc == ruc).first()
    
    def get_multi_active(
        self,
        db: Session,
        *,
        skip: int = 0,
        limit: int = 100
    ) -> List[Empresa]:
        """Obtener empresas activas"""
        return db.query(Empresa).filter(
            Empresa.activo == True
        ).offset(skip).limit(limit).all()
    
    def search(
        self,
        db: Session,
        *,
        query: str,
        skip: int = 0,
        limit: int = 100
    ) -> List[Empresa]:
        """Buscar empresas por nombre o RUC"""
        return db.query(Empresa).filter(
            (Empresa.nombre_empresa.ilike(f"%{query}%")) |
            (Empresa.ruc.ilike(f"%{query}%"))
        ).offset(skip).limit(limit).all()
    
    def get_with_chofer_count(
        self,
        db: Session,
        *,
        id_empresa: int
    ) -> Optional[dict]:
        """
        Obtener empresa con conteo de choferes usando la relación
        """
        empresa = db.query(Empresa).filter(Empresa.id_empresa == id_empresa).first()
        
        if not empresa:
            return None
        
        # Usar la relación SQLAlchemy para contar choferes
        total_choferes = len([c for c in empresa.choferes if c.activo])
        
        return {
            "id_empresa": empresa.id_empresa,
            "nombre_empresa": empresa.nombre_empresa,
            "ruc": empresa.ruc,
            "telefono": empresa.telefono,
            "email": empresa.email,
            "direccion": empresa.direccion,
            "activo": empresa.activo,
            "fecha_registro": empresa.fecha_registro,
            "total_choferes": total_choferes
        }
    
    def activate(self, db: Session, *, id_empresa: int) -> Empresa:
        """Activar empresa"""
        empresa = self.get(db, id=id_empresa)
        empresa.activo = True
        db.add(empresa)
        db.commit()
        db.refresh(empresa)
        return empresa
    
    def deactivate(self, db: Session, *, id_empresa: int) -> Empresa:
        """Desactivar empresa"""
        empresa = self.get(db, id=id_empresa)
        empresa.activo = False
        db.add(empresa)
        db.commit()
        db.refresh(empresa)
        return empresa


# Instancia global de CRUD para empresas
empresa = CRUDEmpresa(Empresa)