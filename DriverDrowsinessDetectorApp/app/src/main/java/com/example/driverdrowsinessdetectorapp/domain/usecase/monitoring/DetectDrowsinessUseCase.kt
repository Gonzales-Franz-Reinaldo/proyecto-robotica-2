package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring

import android.util.Log
import com.example.driverdrowsinessdetectorapp.domain.model.*
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.features.*
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing.*
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject

class DetectDrowsinessUseCase @Inject constructor(
    private val calculateEyeDistancesUseCase: CalculateEyeDistancesUseCase,
    private val calculateMouthDistancesUseCase: CalculateMouthDistancesUseCase,
    private val calculateMARUseCase: CalculateMARUseCase,
    private val detectHeadPositionUseCase: DetectHeadPositionUseCase,
    private val detectHandNearEyesUseCase: DetectHandNearEyesUseCase,
    private val detectBlinkUseCase: DetectBlinkUseCase,
    private val detectMicrosleepUseCase: DetectMicrosleepUseCase,
    private val detectYawnUseCase: DetectYawnUseCase,
    private val detectNoddingUseCase: DetectNoddingUseCase,
    private val detectEyeRubUseCase: DetectEyeRubUseCase
) {
    companion object {
        private const val TAG = "DetectDrowsinessUseCase"
        private const val BLINK_THRESHOLD = 20
        private const val YAWN_THRESHOLD = 3
        private const val EYE_RUB_THRESHOLD = 3
        
        // Umbral para considerar boca "muy abierta" (bostezo en progreso)
        private const val MOUTH_WIDE_OPEN_THRESHOLD = 0.5f
    }
    
    /**
     * Versión que acepta landmarks nullables para manejar sin rostro
     */
    operator fun invoke(
        faceLandmarks: List<NormalizedLandmark>?,
        handLandmarks: List<List<NormalizedLandmark>>?,
        handedness: List<List<Category>>?
    ): MetricasSomnolencia {
        try {
            // SIEMPRE detectar posición de cabeza
            val headPosition = detectHeadPositionUseCase(faceLandmarks)
            val (isNodding, noddingCount, noddingDurations) = detectNoddingUseCase(headPosition)
            
            // Si no hay rostro, solo retornar métricas de cabeceo
            if (faceLandmarks == null || faceLandmarks.size < 468) {
                Log.d(TAG, "⚠️ Sin rostro - Solo procesando cabeceo: isNodding=$isNodding")
                
                val alertLevel = if (isNodding) AlertLevel.CRITICAL else AlertLevel.NORMAL
                val alertType = if (isNodding) AlertType.HEAD_NODDING else null
                
                return MetricasSomnolencia(
                    timestamp = System.currentTimeMillis(),
                    ear = 0f,
                    mar = 0f,
                    headPose = HeadPose.NEUTRAL,
                    isBlinking = false,
                    blinkCount = 0,
                    isMicrosleep = false,
                    microsleepCount = 0,
                    microsleepDurations = emptyList(),
                    isYawning = false,
                    yawnCount = 0,
                    yawnDurations = emptyList(),
                    isNodding = isNodding,
                    noddingCount = noddingCount,
                    noddingDurations = noddingDurations,
                    eyeRubFirstHand = Triple(false, 0, emptyList()),
                    eyeRubSecondHand = Triple(false, 0, emptyList()),
                    alertLevel = alertLevel,
                    alertType = alertType
                )
            }
            
            // CÁLCULOS NORMALES
            val eyeDistances = calculateEyeDistancesUseCase(faceLandmarks)
            val mouthDistances = calculateMouthDistancesUseCase(faceLandmarks)
            
            val ear = if (eyeDistances.horizontalRightEye > 0 && eyeDistances.horizontalLeftEye > 0) {
                val earRight = eyeDistances.verticalRightEyelid / eyeDistances.horizontalRightEye
                val earLeft = eyeDistances.verticalLeftEyelid / eyeDistances.horizontalLeftEye
                (earRight + earLeft) / 2f
            } else 0f
            
            val mar = calculateMARUseCase(faceLandmarks)
            val handNearEyes = detectHandNearEyesUseCase(faceLandmarks, handLandmarks, handedness)
            
            // DETECTAR SI LA BOCA ESTÁ MUY ABIERTA (bostezo en progreso)
            val isMouthWideOpen = mouthDistances.distanciaLabios > mouthDistances.distanciaMenton ||
                                  mar > MOUTH_WIDE_OPEN_THRESHOLD
            
            // DETECTAR EVENTOS
            val (isBlinking, blinkCount, _) = detectBlinkUseCase(eyeDistances)
            
            // PASAR el estado de boca abierta a microsueño
            val (isMicrosleep, microsleepCount, microsleepDurations) = detectMicrosleepUseCase(
                eyeDistances = eyeDistances,
                isMouthWideOpen = isMouthWideOpen  
            )
            
            val (isYawning, yawnCount, yawnDurations) = detectYawnUseCase(mouthDistances)
            val eyeRubResults = detectEyeRubUseCase(handNearEyes)
            
            // 🆕 EXTRAER SI HAY FROTAMIENTO ACTIVO (evento individual)
            val isEyeRubLeftActive = eyeRubResults["MANO_IZQUIERDA"]?.first ?: false
            val isEyeRubRightActive = eyeRubResults["MANO_DERECHA"]?.first ?: false
            val isEyeRubActive = isEyeRubLeftActive || isEyeRubRightActive
            
            // DETERMINAR NIVEL DE ALERTA (🆕 LÓGICA MEJORADA)
            val (alertLevel, alertType) = determineAlertLevelAndType(
                isMicrosleep = isMicrosleep,
                isNodding = isNodding,
                isYawning = isYawning,  // 🆕 Evento individual de bostezo
                isEyeRubActive = isEyeRubActive,  // 🆕 Evento individual de frotamiento
                blinkCount = blinkCount,
                yawnCount = yawnCount,
                eyeRubFirstHandCount = eyeRubResults["MANO_IZQUIERDA"]?.second ?: 0,
                eyeRubSecondHandCount = eyeRubResults["MANO_DERECHA"]?.second ?: 0
            )
            
            return MetricasSomnolencia(
                timestamp = System.currentTimeMillis(),
                ear = ear,
                mar = mar,
                headPose = HeadPose(pitch = 0f, yaw = 0f, roll = 0f),
                isBlinking = isBlinking,
                blinkCount = blinkCount,
                isMicrosleep = isMicrosleep,
                microsleepCount = microsleepCount,
                microsleepDurations = microsleepDurations,
                isYawning = isYawning,
                yawnCount = yawnCount,
                yawnDurations = yawnDurations,
                isNodding = isNodding,
                noddingCount = noddingCount,
                noddingDurations = noddingDurations,
                eyeRubFirstHand = Triple(
                    eyeRubResults["MANO_IZQUIERDA"]?.first ?: false,
                    eyeRubResults["MANO_IZQUIERDA"]?.second ?: 0,
                    eyeRubResults["MANO_IZQUIERDA"]?.third ?: emptyList()
                ),
                eyeRubSecondHand = Triple(
                    eyeRubResults["MANO_DERECHA"]?.first ?: false,
                    eyeRubResults["MANO_DERECHA"]?.second ?: 0,
                    eyeRubResults["MANO_DERECHA"]?.third ?: emptyList()
                ),
                alertLevel = alertLevel,
                alertType = alertType
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            return MetricasSomnolencia.empty()
        }
    }
    
    /**
     * 🆕 LÓGICA MEJORADA: Determina nivel Y tipo de alerta
     * 
     * PRIORIDAD:
     * 1. CRITICAL: Microsueño, Cabeceo
     * 2. MEDIUM: Bostezo individual, Frotamiento individual (🆕 NUEVO)
     * 3. HIGH: Muchos bostezos acumulados
     * 4. MEDIUM: Parpadeo excesivo, muchos frotamientos acumulados
     * 5. NORMAL: Sin alertas
     */
    private fun determineAlertLevelAndType(
        isMicrosleep: Boolean,
        isNodding: Boolean,
        isYawning: Boolean,  // 🆕 Evento individual
        isEyeRubActive: Boolean,  // 🆕 Evento individual
        blinkCount: Int,
        yawnCount: Int,
        eyeRubFirstHandCount: Int,
        eyeRubSecondHandCount: Int
    ): Pair<AlertLevel, AlertType?> {
        
        // 1. CRÍTICO: Microsueño o Cabeceo
        if (isMicrosleep) {
            Log.w(TAG, "🔴 ALERTA CRÍTICA: MICROSUEÑO")
            return Pair(AlertLevel.CRITICAL, AlertType.MICROSLEEP)
        }
        
        if (isNodding) {
            Log.w(TAG, "🔴 ALERTA CRÍTICA: CABECEO")
            return Pair(AlertLevel.CRITICAL, AlertType.HEAD_NODDING)
        }
        
        // 2. 🆕 MEDIUM: Bostezo individual detectado (evento en progreso)
        if (isYawning) {
            Log.w(TAG, "🟡 ALERTA MEDIA: BOSTEZO DETECTADO")
            return Pair(AlertLevel.MEDIUM, AlertType.YAWNING)
        }
        
        // 3. 🆕 MEDIUM: Frotamiento de ojos individual (evento en progreso)
        if (isEyeRubActive) {
            Log.w(TAG, "🟡 ALERTA MEDIA: FROTAMIENTO DE OJOS DETECTADO")
            return Pair(AlertLevel.MEDIUM, AlertType.EYE_RUB)
        }
        
        // 4. HIGH: Muchos bostezos acumulados (umbral superado)
        if (yawnCount > YAWN_THRESHOLD) {
            Log.w(TAG, "🟠 ALERTA ALTA: $yawnCount bostezos (umbral: $YAWN_THRESHOLD)")
            return Pair(AlertLevel.HIGH, AlertType.YAWNING)
        }
        
        // 5. MEDIUM: Parpadeo excesivo
        if (blinkCount > BLINK_THRESHOLD) {
            Log.w(TAG, "🟡 ALERTA MEDIA: $blinkCount parpadeos (umbral: $BLINK_THRESHOLD)")
            return Pair(AlertLevel.MEDIUM, AlertType.EXCESSIVE_BLINKING)
        }
        
        // 6. MEDIUM: Muchos frotamientos acumulados
        if (eyeRubFirstHandCount > EYE_RUB_THRESHOLD || eyeRubSecondHandCount > EYE_RUB_THRESHOLD) {
            val totalRubs = eyeRubFirstHandCount + eyeRubSecondHandCount
            Log.w(TAG, "🟡 ALERTA MEDIA: $totalRubs frotamientos (umbral: $EYE_RUB_THRESHOLD)")
            return Pair(AlertLevel.MEDIUM, AlertType.EYE_RUB)
        }
        
        // 7. NORMAL: Sin alertas
        return Pair(AlertLevel.NORMAL, null)
    }
    
    fun reset() {
        detectBlinkUseCase.reset()
        detectMicrosleepUseCase.reset()
        detectYawnUseCase.reset()
        detectNoddingUseCase.reset()
        detectEyeRubUseCase.reset()
        detectHeadPositionUseCase.reset()
        Log.d(TAG, "🔄 Contadores reseteados")
    }
}