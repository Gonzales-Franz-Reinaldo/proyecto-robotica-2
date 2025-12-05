# ============================================
# SCHEMAS DE ALERTAS
# ============================================
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from datetime import datetime
from enum import Enum


class AlertLevel(str, Enum):
    """Niveles de alerta"""
    CRITICAL = "critical"
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"
    NORMAL = "normal"


class AlertType(str, Enum):
    """Tipos de eventos"""
    MICROSLEEP = "microsleep"
    NODDING = "nodding"
    YAWN = "yawn"
    EYE_RUBBING = "eye_rubbing"
    EXCESSIVE_BLINKING = "excessive_blinking"
    NORMAL = "normal"


class AlertTriggerRequest(BaseModel):
    """Request para disparar una alerta"""
    alert_level: AlertLevel = Field(..., description="Nivel de la alerta")
    alert_type: AlertType = Field(..., description="Tipo de evento detectado")
    user_id: Optional[int] = Field(None, description="ID del chofer")
    session_id: Optional[str] = Field(None, description="ID de la sesión de monitoreo")
    metrics: Optional[Dict[str, Any]] = Field(
        None, 
        description="Métricas de somnolencia (EAR, MAR, etc.)",
        example={
            "ear": 0.15,
            "mar": 0.6,
            "blink_count": 25,
            "yawn_count": 3
        }
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "alert_level": "critical",
                "alert_type": "microsleep",
                "user_id": 1,
                "session_id": "abc-123-def",
                "metrics": {
                    "ear": 0.15,
                    "mar": 0.3
                }
            }
        }


class AlertResponse(BaseModel):
    """Respuesta de una alerta procesada"""
    success: bool
    alert_level: str
    alert_type: str
    timestamp: datetime
    actions: Dict[str, Any]
    message: Optional[str] = None


class IoTStatusResponse(BaseModel):
    """Estado de los dispositivos IoT"""
    tuya_siren: Dict[str, Any]
    esp32_leds: Dict[str, Any]
    last_alert: Dict[str, Any]


class TestDevicesResponse(BaseModel):
    """Resultado de prueba de dispositivos"""
    siren_test: bool
    led_red_test: bool
    led_yellow_test: bool
    led_green_test: bool