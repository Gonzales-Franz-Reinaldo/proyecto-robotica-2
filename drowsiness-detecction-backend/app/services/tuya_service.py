# ============================================
# SERVICIO DE CONTROL DE SIRENA TUYA
# Controla la sirena WiFi via protocolo Tuya local
# ============================================
import tinytuya
import asyncio
import logging
from typing import Optional

from app.core.config import settings

logger = logging.getLogger(__name__)


class TuyaService:
    """
    Servicio para controlar la sirena Tuya.
    Usa tinytuya para comunicación local (sin nube).
    """
    
    def __init__(self):
        self.device: Optional[tinytuya.Device] = None
        self._is_connected = False
        self._initialize_device()
    
    def _initialize_device(self):
        """Inicializa la conexión con el dispositivo Tuya"""
        if not settings.TUYA_ENABLED:
            logger.warning("Servicio Tuya deshabilitado")
            return
            
        if not all([settings.TUYA_DEVICE_ID, settings.TUYA_IP_ADDRESS, settings.TUYA_LOCAL_KEY]):
            logger.warning("Configuración Tuya incompleta")
            return
        
        try:
            self.device = tinytuya.Device(
                dev_id=settings.TUYA_DEVICE_ID,
                address=settings.TUYA_IP_ADDRESS,
                local_key=settings.TUYA_LOCAL_KEY,
                version=settings.TUYA_DEVICE_VERSION
            )
            self._is_connected = True
            logger.info(f"Dispositivo Tuya inicializado: {settings.TUYA_IP_ADDRESS}")
        except Exception as e:
            logger.error(f"Error inicializando dispositivo Tuya: {e}")
            self._is_connected = False
    
    def get_status(self) -> Optional[dict]:
        """Obtiene el estado actual del dispositivo"""
        if not self.device:
            return None
        
        try:
            status = self.device.status()
            logger.debug(f"Estado Tuya: {status}")
            return status
        except Exception as e:
            logger.error(f"Error obteniendo estado Tuya: {e}")
            return None
    
    def activate_siren(self, duration: int = None) -> bool:
        """
        Activa la sirena por un tiempo determinado.
        
        Args:
            duration: Duración en segundos (default: TUYA_SIREN_DURATION)
            
        Returns:
            True si se activó correctamente
        """
        if not self.device:
            logger.error("Dispositivo Tuya no disponible")
            return False
        
        duration = duration or settings.TUYA_SIREN_DURATION
        
        try:
            logger.info(f"🔔 Activando sirena por {duration} segundos...")
            self.device.set_value(settings.TUYA_SIREN_DPS, True)
            return True
        except Exception as e:
            logger.error(f"Error activando sirena: {e}")
            return False
    
    def deactivate_siren(self) -> bool:
        """Desactiva la sirena"""
        if not self.device:
            return False
        
        try:
            logger.info("🔕 Desactivando sirena...")
            self.device.set_value(settings.TUYA_SIREN_DPS, False)
            return True
        except Exception as e:
            logger.error(f"Error desactivando sirena: {e}")
            return False
    
    async def activate_siren_timed(self, duration: int = None) -> bool:
        """
        Activa la sirena por un tiempo y luego la apaga automáticamente.
        Versión asíncrona.
        
        Args:
            duration: Duración en segundos
        """
        duration = duration or settings.TUYA_SIREN_DURATION
        
        if self.activate_siren():
            await asyncio.sleep(duration)
            return self.deactivate_siren()
        return False
    
    @property
    def is_connected(self) -> bool:
        return self._is_connected


# Instancia global del servicio
tuya_service = TuyaService()