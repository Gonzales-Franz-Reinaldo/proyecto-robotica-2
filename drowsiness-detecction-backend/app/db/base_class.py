from sqlalchemy.ext.declarative import as_declarative, declared_attr
from typing import Any


@as_declarative()
class Base:
    """
    Base class para todos los modelos SQLAlchemy
    Provee __tablename__ automático basado en el nombre de la clase
    """
    
    id: Any
    __name__: str
    
    @declared_attr
    def __tablename__(cls) -> str:
        """
        Genera el nombre de la tabla automáticamente
        Convierte CamelCase a snake_case
        Ejemplo: 
            - Usuario -> usuarios
            - Empresa -> empresas
        """
        # Convertir CamelCase a snake_case con pluralización
        import re
        name = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', cls.__name__)
        name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name).lower()
        
        # Agregar 's' para pluralizar (para tablas)
        if not name.endswith('s'):
            name += 's'
        
        return name