/*
 * ═══════════════════════════════════════════════════════════════════════════
 * ESP32 LED Controller - Sistema de Detección de Somnolencia
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Controla 3 LEDs indicadores según el nivel de alerta de somnolencia:
 * 
 * 🔴 LED ROJO (GPIO 25)    - Alerta CRÍTICA (microsueño, cabeceo)
 * 🟡 LED AMARILLO (GPIO 26) - Alerta MEDIA (bostezo, frotamiento ojos)
 * 🟢 LED VERDE (GPIO 27)    - Estado NORMAL (sin somnolencia)
 * 
 * Protocolo Serial (115200 baud):
 * - "RED\n"      → Enciende LED rojo (apaga otros)
 * - "YELLOW\n"   → Enciende LED amarillo (apaga otros)
 * - "GREEN\n"    → Enciende LED verde (apaga otros)
 * - "OFF\n"      → Apaga todos los LEDs
 * - "STATUS\n"   → Retorna estado actual: "OK:STATUS:<color>"
 * - "BLINK\n"    → Parpadeo de emergencia (LED rojo 3 veces)
 * - "TEST\n"     → Prueba todos los LEDs secuencialmente
 * 
 * Respuestas:
 * - "OK:<COMANDO>" - Comando ejecutado correctamente
 * - "ERROR:UNKNOWN_COMMAND" - Comando no reconocido
 * 
 * @author Sistema de Detección de Somnolencia
 * @version 2.0
 * @date 2024
 */

// ═══════════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN DE PINES
// ═══════════════════════════════════════════════════════════════════════════

#define LED_RED    25   // GPIO 25 - LED Rojo (Alerta Crítica)
#define LED_YELLOW 26   // GPIO 26 - LED Amarillo (Alerta Media)
#define LED_GREEN  27   // GPIO 27 - LED Verde (Estado Normal)

// LED integrado del ESP32 (opcional, para debug)
#define LED_BUILTIN 2

// ═══════════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN SERIAL
// ═══════════════════════════════════════════════════════════════════════════

#define SERIAL_BAUD_RATE 115200
#define COMMAND_TIMEOUT 100  // ms

// ═══════════════════════════════════════════════════════════════════════════
// VARIABLES GLOBALES
// ═══════════════════════════════════════════════════════════════════════════

String currentLed = "green";  // Estado inicial: verde (normal)
String inputBuffer = "";      // Buffer para comandos entrantes
bool commandComplete = false; // Flag de comando completo

// ═══════════════════════════════════════════════════════════════════════════
// SETUP - Inicialización
// ═══════════════════════════════════════════════════════════════════════════

void setup() {
    // Inicializar comunicación Serial
    Serial.begin(SERIAL_BAUD_RATE);
    while (!Serial) {
        ; // Esperar a que el puerto serial esté listo
    }
    
    // Reservar espacio para el buffer de comandos
    inputBuffer.reserve(50);
    
    // Configurar pines como salida
    pinMode(LED_RED, OUTPUT);
    pinMode(LED_YELLOW, OUTPUT);
    pinMode(LED_GREEN, OUTPUT);
    pinMode(LED_BUILTIN, OUTPUT);
    
    // Estado inicial: todos apagados
    turnOffAll();
    
    // Secuencia de inicio (test visual)
    startupSequence();
    
    // Estado inicial: LED verde (normal)
    setLed("GREEN");
    
    // Mensaje de inicio
    Serial.println("═══════════════════════════════════════════");
    Serial.println("  ESP32 LED Controller v2.0");
    Serial.println("  Sistema de Detección de Somnolencia");
    Serial.println("═══════════════════════════════════════════");
    Serial.println("OK:READY");
}

// ═══════════════════════════════════════════════════════════════════════════
// LOOP - Bucle Principal
// ═══════════════════════════════════════════════════════════════════════════

void loop() {
    // Leer datos del puerto serial
    while (Serial.available() > 0) {
        char inChar = (char)Serial.read();
        
        // Si es fin de línea, el comando está completo
        if (inChar == '\n' || inChar == '\r') {
            if (inputBuffer.length() > 0) {
                commandComplete = true;
            }
        } else {
            // Agregar carácter al buffer
            inputBuffer += inChar;
        }
    }
    
    // Procesar comando si está completo
    if (commandComplete) {
        inputBuffer.trim();
        inputBuffer.toUpperCase();
        
        if (inputBuffer.length() > 0) {
            processCommand(inputBuffer);
        }
        
        // Limpiar buffer y flag
        inputBuffer = "";
        commandComplete = false;
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// PROCESAMIENTO DE COMANDOS
// ═══════════════════════════════════════════════════════════════════════════

void processCommand(String cmd) {
    // Log del comando recibido (debug)
    // Serial.print("CMD: ");
    // Serial.println(cmd);
    
    if (cmd == "RED") {
        setLed("RED");
        Serial.println("OK:RED");
    }
    else if (cmd == "YELLOW") {
        setLed("YELLOW");
        Serial.println("OK:YELLOW");
    }
    else if (cmd == "GREEN") {
        setLed("GREEN");
        Serial.println("OK:GREEN");
    }
    else if (cmd == "OFF") {
        turnOffAll();
        currentLed = "off";
        Serial.println("OK:OFF");
    }
    else if (cmd == "STATUS") {
        Serial.print("OK:STATUS:");
        Serial.println(currentLed);
    }
    else if (cmd == "BLINK") {
        blinkEmergency();
        Serial.println("OK:BLINK");
    }
    else if (cmd == "TEST") {
        testSequence();
        Serial.println("OK:TEST");
    }
    else if (cmd == "PING") {
        Serial.println("OK:PONG");
    }
    else if (cmd == "VERSION") {
        Serial.println("OK:VERSION:2.0");
    }
    else {
        Serial.print("ERROR:UNKNOWN_COMMAND:");
        Serial.println(cmd);
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CONTROL DE LEDS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Enciende un LED específico y apaga los demás
 * @param color: "RED", "YELLOW", "GREEN"
 */
void setLed(String color) {
    // Apagar todos primero
    digitalWrite(LED_RED, LOW);
    digitalWrite(LED_YELLOW, LOW);
    digitalWrite(LED_GREEN, LOW);
    
    // Encender el LED correspondiente
    if (color == "RED") {
        digitalWrite(LED_RED, HIGH);
        currentLed = "red";
    }
    else if (color == "YELLOW") {
        digitalWrite(LED_YELLOW, HIGH);
        currentLed = "yellow";
    }
    else if (color == "GREEN") {
        digitalWrite(LED_GREEN, HIGH);
        currentLed = "green";
    }
    
    // Indicador en LED builtin
    digitalWrite(LED_BUILTIN, HIGH);
}

/**
 * Apaga todos los LEDs
 */
void turnOffAll() {
    digitalWrite(LED_RED, LOW);
    digitalWrite(LED_YELLOW, LOW);
    digitalWrite(LED_GREEN, LOW);
    digitalWrite(LED_BUILTIN, LOW);
    currentLed = "off";
}

// ═══════════════════════════════════════════════════════════════════════════
// SECUENCIAS ESPECIALES
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Secuencia de inicio - Test visual de todos los LEDs
 */
void startupSequence() {
    int delayTime = 300;
    
    // Rojo
    digitalWrite(LED_RED, HIGH);
    delay(delayTime);
    digitalWrite(LED_RED, LOW);
    
    // Amarillo
    digitalWrite(LED_YELLOW, HIGH);
    delay(delayTime);
    digitalWrite(LED_YELLOW, LOW);
    
    // Verde
    digitalWrite(LED_GREEN, HIGH);
    delay(delayTime);
    digitalWrite(LED_GREEN, LOW);
    
    // Todos juntos
    digitalWrite(LED_RED, HIGH);
    digitalWrite(LED_YELLOW, HIGH);
    digitalWrite(LED_GREEN, HIGH);
    delay(500);
    turnOffAll();
    
    delay(200);
}

/**
 * Parpadeo de emergencia - LED rojo intermitente
 */
void blinkEmergency() {
    String previousLed = currentLed;
    
    for (int i = 0; i < 5; i++) {
        digitalWrite(LED_RED, HIGH);
        digitalWrite(LED_YELLOW, LOW);
        digitalWrite(LED_GREEN, LOW);
        delay(200);
        
        digitalWrite(LED_RED, LOW);
        delay(200);
    }
    
    // -----------------------------------------------------------
    // SOLUCIÓN: Llama a toUpperCase() primero para modificar la variable,
    // y luego usa la variable modificada en setLed().
    // -----------------------------------------------------------
    
    previousLed.toUpperCase(); 
    // Ahora previousLed es "RED", "YELLOW", "GREEN" u "OFF"
    
    // Restaurar estado anterior
    setLed(previousLed); 
}

/**
 * Secuencia de prueba - Enciende cada LED por 1 segundo
 */
void testSequence() {
    // Rojo
    setLed("RED");
    delay(1000);
    
    // Amarillo
    setLed("YELLOW");
    delay(1000);
    
    // Verde
    setLed("GREEN");
    delay(1000);
    
    // Dejar en verde (estado normal)
}

// ═══════════════════════════════════════════════════════════════════════════
// FIN DEL PROGRAMA
// ═══════════════════════════════════════════════════════════════════════════