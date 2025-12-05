# ============================================
# SERVICIO DE COMUNICACIÓN CON ESP32
# Controla los LEDs del ESP32 via Serial USB
# ============================================
import serial
import asyncio
import logging
import time
from typing import Optional, Literal
from threading import Lock

from app.core.config import settings

logger = logging.getLogger(__name__)

LedColor = Literal["red", "yellow", "green", "off"]


class ESP32Service:
    """
    Servicio para controlar los LEDs del ESP32 via Serial USB.
    
    Protocolo de comandos:
    - "RED\n"    -> Enciende LED rojo
    - "YELLOW\n" -> Enciende LED amarillo
    - "GREEN\n"  -> Enciende LED verde
    - "OFF\n"    -> Apaga todos los LEDs
    - "STATUS\n" -> Obtiene estado actual
    """
    
    def __init__(self):
        self.serial_port: Optional[serial.Serial] = None
        self._is_connected = False
        self._lock = Lock()
        self._current_led = "green"
        self._last_error_time = 0
        self._reconnect_cooldown = 5  # segundos entre intentos de reconexión
        self._initialize_serial()
    
    def _initialize_serial(self):
        """Inicializa la conexión serial con el ESP32"""
        if not settings.ESP32_ENABLED:
            logger.warning("⚠️ Servicio ESP32 deshabilitado en configuración")
            return
        
        try:
            # Cerrar conexión anterior si existe
            if self.serial_port:
                try:
                    self.serial_port.close()
                except:
                    pass
                self.serial_port = None
            
            logger.info(f"🔌 Intentando conectar a ESP32 en {settings.ESP32_SERIAL_PORT}...")
            
            self.serial_port = serial.Serial(
                port=settings.ESP32_SERIAL_PORT,
                baudrate=settings.ESP32_BAUD_RATE,
                timeout=2,
                write_timeout=2
            )
            
            # Esperar a que el ESP32 se reinicie después de conectar
            logger.info("⏳ Esperando reinicio del ESP32 (3 segundos)...")
            time.sleep(3)
            
            # Limpiar buffer (puede tener mensajes de inicio)
            self.serial_port.reset_input_buffer()
            self.serial_port.reset_output_buffer()
            
            # Leer cualquier mensaje de inicio pendiente
            while self.serial_port.in_waiting:
                startup_msg = self.serial_port.readline().decode().strip()
                logger.debug(f"📥 Mensaje de inicio: {startup_msg}")
            
            # Verificar conexión con PING
            logger.info("🏓 Enviando PING al ESP32...")
            self.serial_port.write(b"PING\n")
            self.serial_port.flush()
            time.sleep(0.5)
            
            response = ""
            attempts = 0
            while attempts < 3:
                if self.serial_port.in_waiting:
                    response = self.serial_port.readline().decode().strip()
                    if response:
                        break
                time.sleep(0.3)
                attempts += 1
            
            if "OK" in response or "PONG" in response:
                self._is_connected = True
                logger.info(f"✅ ESP32 conectado correctamente. Respuesta: {response}")
                
                # Enviar estado inicial (verde)
                time.sleep(0.2)
                self._send_command("GREEN")
            elif "READY" in response:
                self._is_connected = True
                logger.info(f"✅ ESP32 listo. Respuesta: {response}")
                time.sleep(0.2)
                self._send_command("GREEN")
            else:
                logger.warning(f"⚠️ Respuesta inesperada del ESP32: '{response}' - Intentando de todos modos")
                self._is_connected = True  # Intentar de todos modos
                
        except serial.SerialException as e:
            logger.error(f"❌ Error de puerto serial ({settings.ESP32_SERIAL_PORT}): {e}")
            logger.info("💡 Sugerencias:")
            logger.info("   1. Verificar que Arduino IDE esté cerrado")
            logger.info("   2. Ejecutar: sudo chmod 666 /dev/ttyUSB0")
            logger.info("   3. Verificar conexión USB del ESP32")
            self._is_connected = False
        except Exception as e:
            logger.error(f"❌ Error inesperado con ESP32: {e}")
            self._is_connected = False
    
    def _try_reconnect(self) -> bool:
        """Intenta reconectar si hay error, respetando cooldown"""
        current_time = time.time()
        
        if current_time - self._last_error_time < self._reconnect_cooldown:
            return False
        
        self._last_error_time = current_time
        logger.info("🔄 Intentando reconectar con ESP32...")
        self._initialize_serial()
        return self._is_connected
    
    def _send_command(self, command: str) -> Optional[str]:
        """
        Envía un comando al ESP32 y espera respuesta.
        Thread-safe con reconexión automática.
        """
        if not settings.ESP32_ENABLED:
            return None
            
        if not self.serial_port or not self._is_connected:
            if not self._try_reconnect():
                logger.warning("⚠️ ESP32 no conectado - comando ignorado")
                return None
        
        with self._lock:
            try:
                # Verificar que el puerto esté abierto
                if not self.serial_port.is_open:
                    self.serial_port.open()
                
                # Limpiar buffer de entrada
                self.serial_port.reset_input_buffer()
                
                # Enviar comando
                cmd = f"{command}\n"
                self.serial_port.write(cmd.encode())
                self.serial_port.flush()
                
                logger.debug(f"📤 Comando enviado: {command}")
                
                # Esperar respuesta (timeout de 1 segundo)
                time.sleep(0.1)
                response = ""
                
                if self.serial_port.in_waiting:
                    response = self.serial_port.readline().decode().strip()
                
                if response:
                    logger.debug(f"📥 Respuesta ESP32: {response}")
                else:
                    # Intentar leer una vez más
                    time.sleep(0.2)
                    if self.serial_port.in_waiting:
                        response = self.serial_port.readline().decode().strip()
                
                return response
                
            except serial.SerialTimeoutException:
                logger.error(f"⏱️ Timeout escribiendo al ESP32")
                return None
            except serial.SerialException as e:
                logger.error(f"❌ Error de comunicación serial: {e}")
                self._is_connected = False
                # Intentar reconectar en la próxima llamada
                return None
            except OSError as e:
                # Error (5, 'Input/output error') - puerto desconectado
                logger.error(f"❌ Error I/O del puerto: {e}")
                self._is_connected = False
                try:
                    self.serial_port.close()
                except:
                    pass
                self.serial_port = None
                return None
            except Exception as e:
                logger.error(f"❌ Error enviando comando: {e}")
                return None
    
    def set_led(self, color: LedColor) -> bool:
        """
        Enciende el LED del color especificado.
        
        Args:
            color: "red", "yellow", "green", o "off"
            
        Returns:
            True si se ejecutó correctamente
        """
        command_map = {
            "red": "RED",
            "yellow": "YELLOW",
            "green": "GREEN",
            "off": "OFF"
        }
        
        command = command_map.get(color)
        if not command:
            logger.error(f"❌ Color inválido: {color}")
            return False
        
        response = self._send_command(command)
        
        if response and "OK" in response:
            self._current_led = color
            emoji = {"red": "🔴", "yellow": "🟡", "green": "🟢", "off": "⚫"}.get(color, "💡")
            logger.info(f"{emoji} LED {color.upper()} activado")
            return True
        
        logger.warning(f"⚠️ No se pudo activar LED {color}")
        return False
    
    async def set_led_async(self, color: LedColor) -> bool:
        """Versión asíncrona de set_led"""
        loop = asyncio.get_event_loop()
        return await loop.run_in_executor(None, self.set_led, color)
    
    def set_led_critical(self) -> bool:
        """Activa LED rojo para alertas críticas (microsueño, cabeceo)"""
        return self.set_led("red")
    
    def set_led_warning(self) -> bool:
        """Activa LED amarillo para alertas medias (bostezo, frotamiento)"""
        return self.set_led("yellow")
    
    def set_led_normal(self) -> bool:
        """Activa LED verde para estado normal"""
        return self.set_led("green")
    
    def turn_off_leds(self) -> bool:
        """Apaga todos los LEDs"""
        return self.set_led("off")
    
    def blink_emergency(self) -> bool:
        """Activa parpadeo de emergencia"""
        response = self._send_command("BLINK")
        return response is not None and "OK" in response
    
    def get_status(self) -> dict:
        """Obtiene el estado actual de los LEDs"""
        if not self._is_connected:
            return {"connected": False, "current_led": None, "raw_response": None}
        
        response = self._send_command("STATUS")
        return {
            "connected": self._is_connected,
            "current_led": self._current_led,
            "raw_response": response
        }
    
    def reconnect(self) -> bool:
        """Intenta reconectar con el ESP32"""
        logger.info("🔄 Reconexión manual solicitada...")
        
        if self.serial_port:
            try:
                self.serial_port.close()
            except:
                pass
        
        self.serial_port = None
        self._is_connected = False
        self._last_error_time = 0  # Reset cooldown
        
        self._initialize_serial()
        return self._is_connected
    
    @property
    def is_connected(self) -> bool:
        return self._is_connected
    
    @property
    def current_led(self) -> str:
        return self._current_led


# Instancia global del servicio
esp32_service = ESP32Service()