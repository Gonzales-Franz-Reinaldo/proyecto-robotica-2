# ============================================
# SERVICIO DE ALERTAS - ORQUESTADOR
# Coordina la sirena Tuya y los LEDs del ESP32
# ============================================
import asyncio
import logging
from typing import Optional
from datetime import datetime
from enum import Enum

from app.services.tuya_service import tuya_service
from app.services.esp32_service import esp32_service

logger = logging.getLogger(__name__)


class AlertLevel(str, Enum):
    """Niveles de alerta de somnolencia"""
    CRITICAL = "critical"   # Microsueño, Cabeceo
    HIGH = "high"           # Bostezo prolongado
    MEDIUM = "medium"       # Frotamiento de ojos, parpadeo excesivo
    LOW = "low"             # Señales leves
    NORMAL = "normal"       # Sin somnolencia


class AlertType(str, Enum):
    """Tipos de eventos de somnolencia"""
    MICROSLEEP = "microsleep"
    NODDING = "nodding"
    YAWN = "yawn"
    EYE_RUBBING = "eye_rubbing"
    EXCESSIVE_BLINKING = "excessive_blinking"
    NORMAL = "normal"


class AlertService:
    """
    Servicio orquestador de alertas.
    Coordina la activación de sirena y LEDs según el nivel de alerta.
    """
    
    def __init__(self):
        self._last_alert_level = AlertLevel.NORMAL
        self._last_alert_time: Optional[datetime] = None
        self._siren_active = False
    
    async def process_alert(
        self,
        alert_level: AlertLevel,
        alert_type: AlertType,
        user_id: Optional[int] = None,
        metrics: Optional[dict] = None
    ) -> dict:
        """
        Procesa una alerta de somnolencia y activa los dispositivos IoT.
        
        Args:
            alert_level: Nivel de la alerta
            alert_type: Tipo de evento detectado
            user_id: ID del usuario/chofer
            metrics: Métricas de somnolencia (EAR, MAR, etc.)
            
        Returns:
            Diccionario con el resultado de las acciones
        """
        logger.info(f"📊 Procesando alerta: {alert_level.value} - {alert_type.value}")
        
        result = {
            "alert_level": alert_level.value,
            "alert_type": alert_type.value,
            "timestamp": datetime.now().isoformat(),
            "actions": {
                "siren": False,
                "led": None
            }
        }
        
        # Determinar acciones según nivel de alerta
        if alert_level == AlertLevel.CRITICAL:
            # CRÍTICO: Sirena + LED Rojo
            result["actions"]["led"] = await self._set_led_async("red")
            result["actions"]["siren"] = await self._activate_siren_async()
            
        elif alert_level in [AlertLevel.HIGH, AlertLevel.MEDIUM]:
            # MEDIO/ALTO: LED Amarillo (sin sirena)
            result["actions"]["led"] = await self._set_led_async("yellow")
            
        elif alert_level == AlertLevel.LOW:
            # BAJO: LED Amarillo intermitente o solo advertencia
            result["actions"]["led"] = await self._set_led_async("yellow")
            
        else:
            # NORMAL: LED Verde
            result["actions"]["led"] = await self._set_led_async("green")
        
        # Actualizar estado
        self._last_alert_level = alert_level
        self._last_alert_time = datetime.now()
        
        logger.info(f"✅ Alerta procesada: {result}")
        return result
    
    async def _set_led_async(self, color: str) -> bool:
        """Activa el LED de forma asíncrona"""
        try:
            return await esp32_service.set_led_async(color)
        except Exception as e:
            logger.error(f"Error activando LED {color}: {e}")
            return False
    
    async def _activate_siren_async(self) -> bool:
        """Activa la sirena de forma asíncrona"""
        if self._siren_active:
            logger.warning("Sirena ya está activa")
            return True
        
        try:
            self._siren_active = True
            # Ejecutar sirena temporizada en background
            asyncio.create_task(self._siren_timed_task())
            return True
        except Exception as e:
            logger.error(f"Error activando sirena: {e}")
            self._siren_active = False
            return False
    
    async def _siren_timed_task(self):
        """Tarea para activar sirena por tiempo limitado"""
        try:
            await tuya_service.activate_siren_timed()
        finally:
            self._siren_active = False
    
    def get_iot_status(self) -> dict:
        """Obtiene el estado de todos los dispositivos IoT"""
        return {
            "tuya_siren": {
                "connected": tuya_service.is_connected,
                "active": self._siren_active
            },
            "esp32_leds": {
                "connected": esp32_service.is_connected,
                "current_led": esp32_service.current_led
            },
            "last_alert": {
                "level": self._last_alert_level.value if self._last_alert_level else None,
                "time": self._last_alert_time.isoformat() if self._last_alert_time else None
            }
        }
    
    async def test_devices(self) -> dict:
        """Prueba todos los dispositivos IoT"""
        results = {
            "siren_test": False,
            "led_red_test": False,
            "led_yellow_test": False,
            "led_green_test": False
        }
        
        logger.info("🧪 Iniciando prueba de dispositivos IoT...")
        
        # Probar LEDs
        for color in ["red", "yellow", "green"]:
            success = await self._set_led_async(color)
            results[f"led_{color}_test"] = success
            await asyncio.sleep(1)
        
        # Volver a verde
        await self._set_led_async("green")
        
        # Probar sirena (2 segundos)
        logger.info("🔔 Probando sirena (2 segundos)...")
        if tuya_service.activate_siren():
            await asyncio.sleep(2)
            tuya_service.deactivate_siren()
            results["siren_test"] = True
        
        logger.info(f"🧪 Prueba completada: {results}")
        return results


# Instancia global del servicio
alert_service = AlertService()