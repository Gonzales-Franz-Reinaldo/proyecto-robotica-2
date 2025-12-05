# ============================================
# ROUTER DE ALERTAS IoT
# Endpoints para control de sirena y LEDs
# ============================================
from fastapi import APIRouter, HTTPException, status, Depends, Header
from typing import Optional

from app.schemas.alert import (
    AlertTriggerRequest,
    AlertResponse,
    IoTStatusResponse,
    TestDevicesResponse,
    AlertLevel,
    AlertType
)
from app.services.alert_service import alert_service, AlertLevel as ServiceAlertLevel, AlertType as ServiceAlertType
from app.services.esp32_service import esp32_service
from app.services.tuya_service import tuya_service

router = APIRouter(prefix="/alerts", tags=["Alerts & IoT"])


# Función auxiliar para obtener usuario opcional
async def get_optional_user_id(authorization: Optional[str] = Header(None)) -> Optional[int]:
    """
    Intenta extraer el user_id del token si existe.
    No falla si no hay token - retorna None.
    """
    if not authorization:
        return None
    
    try:
        from app.core.security import decode_token
        token = authorization.replace("Bearer ", "")
        payload = decode_token(token)
        return int(payload.get("sub", 0))
    except:
        return None


@router.post(
    "/trigger",
    response_model=AlertResponse,
    summary="Disparar alerta de somnolencia",
    description="Procesa una alerta y activa los dispositivos IoT correspondientes. No requiere autenticación."
)
async def trigger_alert(
    request: AlertTriggerRequest,
    user_id: Optional[int] = Depends(get_optional_user_id)
):
    """
    Dispara una alerta de somnolencia.
    
    - **CRITICAL**: Activa sirena (5 seg) + LED rojo
    - **HIGH/MEDIUM**: Activa LED amarillo
    - **NORMAL**: Activa LED verde
    
    ⚠️ Este endpoint NO requiere autenticación para permitir
    que la app móvil envíe alertas rápidamente.
    """
    try:
        # Convertir enums del schema a enums del servicio
        service_level = ServiceAlertLevel(request.alert_level.value)
        service_type = ServiceAlertType(request.alert_type.value)
        
        # Usar user_id del request o del token
        effective_user_id = request.user_id or user_id
        
        result = await alert_service.process_alert(
            alert_level=service_level,
            alert_type=service_type,
            user_id=effective_user_id,
            metrics=request.metrics
        )
        
        return AlertResponse(
            success=True,
            alert_level=result["alert_level"],
            alert_type=result["alert_type"],
            timestamp=result["timestamp"],
            actions=result["actions"],
            message="Alerta procesada correctamente"
        )
        
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error procesando alerta: {str(e)}"
        )


@router.get(
    "/status",
    response_model=IoTStatusResponse,
    summary="Estado de dispositivos IoT",
    description="Obtiene el estado de la sirena Tuya y los LEDs del ESP32"
)
async def get_iot_status():
    """Retorna el estado actual de todos los dispositivos IoT"""
    return alert_service.get_iot_status()


@router.post(
    "/test",
    response_model=TestDevicesResponse,
    summary="Probar dispositivos IoT",
    description="Ejecuta una prueba de todos los dispositivos (sirena y LEDs)"
)
async def test_devices():
    """
    Prueba todos los dispositivos IoT:
    1. Enciende cada LED por 1 segundo
    2. Activa la sirena por 2 segundos
    """
    return await alert_service.test_devices()


@router.post(
    "/led/{color}",
    summary="Control manual de LED",
    description="Enciende manualmente un LED específico"
)
async def set_led(color: str):
    """
    Enciende un LED específico.
    
    Colores válidos: red, yellow, green, off
    """
    valid_colors = ["red", "yellow", "green", "off"]
    if color not in valid_colors:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Color inválido. Use: {valid_colors}"
        )
    
    success = esp32_service.set_led(color)
    return {
        "success": success,
        "color": color,
        "message": f"LED {color} {'activado' if success else 'error al activar'}"
    }


@router.post(
    "/siren/{action}",
    summary="Control manual de sirena",
    description="Activa o desactiva la sirena manualmente"
)
async def control_siren(action: str):
    """
    Controla la sirena manualmente.
    
    Acciones: on, off
    """
    if action == "on":
        success = tuya_service.activate_siren()
    elif action == "off":
        success = tuya_service.deactivate_siren()
    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Acción inválida. Use: on, off"
        )
    
    return {
        "success": success,
        "action": action,
        "message": f"Sirena {'activada' if action == 'on' else 'desactivada'}"
    }


@router.post(
    "/esp32/reconnect",
    summary="Reconectar ESP32",
    description="Intenta reconectar con el ESP32 si se perdió la conexión"
)
async def reconnect_esp32():
    """Intenta reconectar con el ESP32"""
    success = esp32_service.reconnect()
    return {
        "success": success,
        "message": "Reconexión exitosa" if success else "No se pudo reconectar"
    }