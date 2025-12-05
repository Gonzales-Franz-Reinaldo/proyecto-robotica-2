from app.db.base_class import Base
from app.models.user import Usuario
from app.models.empresa import Empresa
from app.models.token_blacklist import TokenBlacklist
from app.models.viaje import Viaje

# Exportar todos los modelos para que SQLAlchemy los registre
__all__ = ["Base", "Usuario", "Empresa", "TokenBlacklist", "Viaje"]