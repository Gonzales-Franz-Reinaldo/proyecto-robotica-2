from pydantic_settings import BaseSettings
from typing import Optional
from functools import lru_cache


class Settings(BaseSettings):
    """
    Configuración global de la aplicación
    Carga variables desde .env
    """
    
    # Información del Proyecto
    PROJECT_NAME: str = "Sistema de Detección de Somnolencia - API"
    VERSION: str = "1.0.0"
    API_V1_PREFIX: str = "/api/v1"
    
    # Base de Datos PostgreSQL
    DB_HOST: str
    DB_PORT: int = 5432
    DB_USER: str
    DB_PASSWORD: str
    DB_NAME: str
    
    @property
    def DATABASE_URL(self) -> str:
        return f"postgresql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
    
    # Seguridad JWT
    SECRET_KEY: str  # openssl rand -hex 32
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30  # 30 minutos
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7     # 7 días
    
    # CORS
    BACKEND_CORS_ORIGINS: list = [
        "http://localhost:3000",
        "http://localhost:5173",  # Vite default
        "http://localhost:5174",
    ]
    
    # Rate Limiting
    MAX_LOGIN_ATTEMPTS: int = 3
    LOCKOUT_DURATION_MINUTES: int = 15
    
    # Sesión
    SESSION_DURATION_MINUTES: int = 15
    
    # Configuración de Email SMTP
    EMAIL_ENABLED: bool = True
    SMTP_HOST: str = "smtp.gmail.com"
    SMTP_PORT: int = 587
    SMTP_USER: str = ""  # Se configurará en .env
    SMTP_PASSWORD: str = ""  # App Password de Gmail
    EMAIL_FROM_NAME: str = "Sistema Detección Somnolencia"
    EMAIL_FROM_ADDRESS: str = ""  # Se configurará en .env
    
    # Configuración de la aplicación
    DEBUG: bool = True
    ENVIRONMENT: str = "development"
    
    class Config:
        env_file = ".env"
        case_sensitive = True


@lru_cache()
def get_settings() -> Settings:
    """
    Singleton para configuración
    Se cachea para no leer .env múltiples veces
    """
    return Settings()


settings = get_settings()