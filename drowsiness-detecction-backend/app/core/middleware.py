from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from starlette.middleware.base import BaseHTTPMiddleware
import time
import logging

from app.core.config import settings

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class LoggingMiddleware(BaseHTTPMiddleware):
    """
    Middleware para logging de requests
    """
    
    async def dispatch(self, request: Request, call_next):
        start_time = time.time()
        
        # Log request
        logger.info(f"{request.method} {request.url.path}")
        
        # Procesar request
        response = await call_next(request)
        
        # Calcular tiempo de procesamiento
        process_time = time.time() - start_time
        
        # Log response
        logger.info(
            f"{request.method} {request.url.path} - "
            f"Status: {response.status_code} - "
            f"Time: {process_time:.2f}s"
        )
        
        # Agregar header con tiempo de procesamiento
        response.headers["X-Process-Time"] = str(process_time)
        
        return response


class RateLimitMiddleware(BaseHTTPMiddleware):
    """
    Middleware básico de rate limiting
    """
    
    def __init__(self, app, max_requests: int = 100, window_seconds: int = 60):
        super().__init__(app)
        self.max_requests = max_requests
        self.window_seconds = window_seconds
        self.requests = {}
    
    async def dispatch(self, request: Request, call_next):
        # Obtener IP del cliente
        client_ip = request.client.host
        current_time = time.time()
        
        # Limpiar requests antiguos
        self.requests = {
            ip: times
            for ip, times in self.requests.items()
            if any(t > current_time - self.window_seconds for t in times)
        }
        
        # Verificar rate limit
        if client_ip in self.requests:
            # Filtrar requests dentro de la ventana de tiempo
            self.requests[client_ip] = [
                t for t in self.requests[client_ip]
                if t > current_time - self.window_seconds
            ]
            
            if len(self.requests[client_ip]) >= self.max_requests:
                return JSONResponse(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    content={
                        "detail": "Demasiadas solicitudes. Intente más tarde."
                    }
                )
        else:
            self.requests[client_ip] = []
        
        # Registrar request
        self.requests[client_ip].append(current_time)
        
        # Procesar request
        response = await call_next(request)
        return response


def setup_middlewares(app):
    """
    Configurar todos los middlewares de la aplicación
    
    Args:
        app: Instancia de FastAPI
    """
    
    # CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.BACKEND_CORS_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    
    # Logging
    app.add_middleware(LoggingMiddleware)
    
    # Rate Limiting (solo en producción)
    if not settings.DEBUG:
        app.add_middleware(RateLimitMiddleware)
    
    logger.info("Middlewares configurados")