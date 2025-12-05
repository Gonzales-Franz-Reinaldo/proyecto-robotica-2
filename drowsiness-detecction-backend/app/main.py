from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.openapi.utils import get_openapi

from app.core.config import settings
from app.core.middleware import setup_middlewares
from app.api.v1.routers import auth, users
from app.api.v1.routers import alerts  # 🆕 Agregar import

# Crear aplicación FastAPI
app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="""
    ## 🚗 Sistema de Detección de Somnolencia - API REST
    
    API completa para el sistema de monitoreo de somnolencia en conductores.
    
    ### 🔐 Autenticación
    
    Esta API usa **JWT Bearer Token** para autenticación.
    
    **Pasos para usar en Swagger:**
    
    1. **Login**: Endpoint `POST /api/v1/auth/login`
       ```json
       {
           "username": "admin",
           "password": "admin123"
       }
       ```
    
    2. **Obtener token**: Copiar el `access_token` de la respuesta
    
    3. **Autorizar**: Click en 🔓 **"Authorize"** (arriba a la derecha)
       - Pegar el token
       - Click "Authorize"

    
    ## 🔗 Enlaces
    
    * **Documentación Swagger**: [/docs](/docs)
    * **Documentación ReDoc**: [/redoc](/redoc)
    * **Health Check**: [/health](/health)
    """,
    docs_url="/docs",
    redoc_url="/redoc",
    contact={
        "name": "Equipo de Desarrollo",
        "email": "dev@sistema-somnolencia.com"
    },
    license_info={
        "name": "MIT",
    }
)

# Configurar esquema de seguridad OAuth2 en Swagger
def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema
    
    openapi_schema = get_openapi(
        title=settings.PROJECT_NAME,
        version=settings.VERSION,
        description=app.description,
        routes=app.routes,
    )
    
    # Agregar esquema de seguridad
    openapi_schema["components"]["securitySchemes"] = {
        "Bearer": {
            "type": "http",
            "scheme": "bearer",
            "bearerFormat": "JWT",
            "description": "Ingrese el token JWT obtenido del endpoint /auth/login"
        }
    }
    
    # Aplicar seguridad a todos los endpoints excepto login y refresh
    for path in openapi_schema["paths"]:
        for method in openapi_schema["paths"][path]:
            if path not in ["/api/v1/auth/login", "/api/v1/auth/refresh"]:
                openapi_schema["paths"][path][method]["security"] = [{"Bearer": []}]
    
    app.openapi_schema = openapi_schema
    return app.openapi_schema

app.openapi = custom_openapi

# Configurar middlewares
setup_middlewares(app)

# Incluir routers con tags para organización en Swagger
app.include_router(
    auth.router,
    prefix=f"{settings.API_V1_PREFIX}/auth",
    tags=["🔐 Autenticación"]
)

app.include_router(
    users.router,
    prefix=f"{settings.API_V1_PREFIX}/users",
    tags=["👥 Gestión de Choferes (Solo Admin)"]
)

app.include_router(auth.router, prefix="/api/v1")
app.include_router(users.router, prefix="/api/v1")
app.include_router(alerts.router, prefix="/api/v1")  # 🆕 Agregar router



@app.get("/", tags=["ℹ️ Info"])
def root():
    """
    **Endpoint raíz**
    
    Retorna información básica de la API
    """
    return {
        "message": "Sistema de Detección de Somnolencia - API",
        "version": settings.VERSION,
        "docs": "/docs",
        "redoc": "/redoc",
        "status": "🟢 Operativo",
        "roles": {
            "admin": "Gestión completa del sistema",
            "chofer": "Login, monitoreo personal y reportes"
        }
    }


@app.get("/health", tags=["ℹ️ Info"])
def health_check():
    """
    **Health Check**
    
    Endpoint para verificar que la API está funcionando
    """
    return {
        "status": "healthy",
        "environment": settings.ENVIRONMENT
    }


@app.get("/test-email-config", tags=["ℹ️ Info"])
def test_email_config():
    """
    **TEST - Verificar Configuración SMTP**
    
    Endpoint temporal para verificar que las variables SMTP se cargaron correctamente.
    ⚠️ ELIMINAR DESPUÉS DE VERIFICAR
    """
    return {
        "email_enabled": settings.EMAIL_ENABLED,
        "smtp_host": settings.SMTP_HOST,
        "smtp_port": settings.SMTP_PORT,
        "smtp_user_configured": bool(settings.SMTP_USER),
        "smtp_user_length": len(settings.SMTP_USER) if settings.SMTP_USER else 0,
        "smtp_password_configured": bool(settings.SMTP_PASSWORD),
        "smtp_password_length": len(settings.SMTP_PASSWORD) if settings.SMTP_PASSWORD else 0,
        "email_from_address": settings.EMAIL_FROM_ADDRESS,
        "smtp_user_preview": settings.SMTP_USER[:10] + "..." if settings.SMTP_USER and len(settings.SMTP_USER) > 10 else settings.SMTP_USER
    }


@app.get("/test-send-email", tags=["ℹ️ Info"])
def test_send_email(email: str = "cris.yosoy12@gmail.com"):
    """
    **TEST - Enviar Email de Prueba**
    
    Endpoint temporal para probar el envío de email.
    ⚠️ ELIMINAR DESPUÉS DE VERIFICAR
    
    Uso: /test-send-email?email=tu-email@gmail.com
    """
    from app.services.email import email_service
    
    try:
        result = email_service.enviar_credenciales_chofer(
            email=email,
            nombre_completo="Usuario de Prueba",
            usuario="usuario_test",
            contrasena="password_test_123"
        )
        
        return {
            "success": result,
            "message": "Email enviado correctamente" if result else "Error al enviar email",
            "email_sent_to": email
        }
    except Exception as e:
        return {
            "success": False,
            "message": f"Error: {str(e)}",
            "error_type": type(e).__name__
        }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG
    )