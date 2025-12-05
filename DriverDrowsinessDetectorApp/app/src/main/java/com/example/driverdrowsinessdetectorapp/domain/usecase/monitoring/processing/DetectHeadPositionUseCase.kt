package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing

import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

data class HeadPosition(
    val isHeadDown: Boolean,
    val position: String,
    val distanceNoseMouth: Float,
    val distanceForeheadNose: Float,
    val noFaceDetected: Boolean = false,
    val rollAngle: Float = 0f,
    val isHeadTilted: Boolean = false
)

class DetectHeadPositionUseCase @Inject constructor() {

    companion object {
        private const val TAG = "DetectHeadPosition"
        
        // Índices MediaPipe para PITCH (inclinación frontal)
        private const val NOSE_TIP = 1
        private const val NOSE_BASE = 0
        private const val NOSE_BOTTOM = 5
        
        // Índices MediaPipe para ROLL (inclinación lateral)
        private const val LEFT_EYE_OUTER = 33
        private const val RIGHT_EYE_OUTER = 263
        
        // UMBRALES AJUSTADOS PARA MEJOR DETECCIÓN
        
        // PITCH (cabeceo frontal recto)
        private const val HEAD_DOWN_THRESHOLD = 0.002f      // Umbral para cabeceo recto
        private const val HYSTERESIS_MARGIN = 0.005f
        
        // ROLL (inclinación lateral) ← MÁS IMPORTANTE
        private const val ROLL_MILD_THRESHOLD = 12f         // 12° = inclinación leve
        private const val ROLL_MODERATE_THRESHOLD = 18f     // 18° = inclinación moderada (cabeceo lateral)
        private const val ROLL_SEVERE_THRESHOLD = 25f       // 25° = inclinación severa (definitivamente cabeceo)
        
        // Umbral de PITCH más flexible cuando hay ROLL
        private const val PITCH_WITH_ROLL_THRESHOLD = -0.015f  // Mucho más permisivo si hay roll
    }
    
    private var wasHeadDown = false
    private var lastKnownHeadDown = false
    private var frameCount = 0

    operator fun invoke(faceLandmarks: List<NormalizedLandmark>?): HeadPosition {
        frameCount++
        
        //  SI NO HAY ROSTRO: Mantener último estado conocido
        if (faceLandmarks == null || faceLandmarks.size < 468) {
            if (frameCount % 10 == 0) {
                Log.w(TAG, "⚠️ Sin rostro - Manteniendo estado: ${if(lastKnownHeadDown) "ABAJO" else "ARRIBA"}")
            }
            return HeadPosition(
                isHeadDown = lastKnownHeadDown,
                position = if (lastKnownHeadDown) "cabeza abajo (sin rostro)" else "sin rostro",
                distanceNoseMouth = 0f,
                distanceForeheadNose = 0f,
                noFaceDetected = true
            )
        }

        // 1 CALCULAR PITCH (inclinación frontal)
        val noseTip = faceLandmarks[NOSE_TIP]
        val noseBase = faceLandmarks[NOSE_BASE]
        val noseBottom = faceLandmarks[NOSE_BOTTOM]

        val distanceNoseMouth = euclideanDistance(noseTip, noseBase)
        val distanceForeheadNose = euclideanDistance(noseTip, noseBottom)
        
        val pitchDifference = distanceForeheadNose - distanceNoseMouth

        // 2 CALCULAR ROLL (inclinación lateral)
        val leftEyeOuter = faceLandmarks[LEFT_EYE_OUTER]
        val rightEyeOuter = faceLandmarks[RIGHT_EYE_OUTER]
        
        val rollAngle = calculateRollAngle(leftEyeOuter, rightEyeOuter)
        val absRoll = abs(rollAngle)
        
        // 3 DETECTAR CABECEO (LÓGICA MEJORADA)
        
        val isHeadDown: Boolean
        val position: String
        val direction = if (rollAngle > 0) "derecha" else "izquierda"

        when {
            // CASO 1: ROLL SEVERO (>25°) → SIEMPRE es cabeceo lateral
            absRoll >= ROLL_SEVERE_THRESHOLD -> {
                isHeadDown = true
                position = "cabeceo lateral severo ($direction, roll=${rollAngle.toInt()}°)"
                Log.w(TAG, "🙇↘️ ROLL SEVERO: ${absRoll.toInt()}° → Cabeceo lateral detectado")
            }
            
            // CASO 2: ROLL MODERADO (18-25°) → Cabeceo si pitch no es muy negativo
            absRoll >= ROLL_MODERATE_THRESHOLD -> {
                // Con roll moderado, ser más flexible con el pitch
                isHeadDown = pitchDifference > PITCH_WITH_ROLL_THRESHOLD
                position = if (isHeadDown) {
                    "cabeceo lateral ($direction, roll=${rollAngle.toInt()}°)"
                } else {
                    "inclinado $direction (${rollAngle.toInt()}°)"
                }
                
                if (isHeadDown) {
                    Log.w(TAG, "🙇↗️ ROLL MODERADO: ${absRoll.toInt()}° + pitch=${"%.4f".format(pitchDifference)} → Cabeceo")
                }
            }
            
            // CASO 3: ROLL LEVE (12-18°) → Combinar con pitch
            absRoll >= ROLL_MILD_THRESHOLD -> {
                // Con roll leve, requerir algo de pitch
                val adjustedPitchThreshold = if (wasHeadDown) {
                    -HYSTERESIS_MARGIN
                } else {
                    -0.005f  // Ligeramente flexible
                }
                
                isHeadDown = pitchDifference > adjustedPitchThreshold
                position = if (isHeadDown) {
                    "cabeza abajo inclinada ($direction)"
                } else {
                    "levemente inclinado $direction"
                }
            }
            
            // CASO 4: SIN ROLL SIGNIFICATIVO → Solo pitch (cabeceo recto)
            else -> {
                isHeadDown = if (wasHeadDown) {
                    pitchDifference > -HYSTERESIS_MARGIN
                } else {
                    pitchDifference > HEAD_DOWN_THRESHOLD
                }
                position = if (isHeadDown) "cabeza abajo (recto)" else "cabeza arriba"
            }
        }
        
        // 4 ACTUALIZAR ESTADOS Y LOGS
        val stateChanged = isHeadDown != wasHeadDown
        wasHeadDown = isHeadDown
        lastKnownHeadDown = isHeadDown

        // Log detallado cuando cambia estado
        if (stateChanged) {
            if (isHeadDown) {
                Log.w(TAG, "🙇⬇️ ═══ CABEZA INCLINADA ═══")
                Log.w(TAG, "    Pitch: ${"%.4f".format(pitchDifference)}")
                Log.w(TAG, "    Roll: ${rollAngle.toInt()}°")
                Log.w(TAG, "    Posición: $position")
            } else {
                Log.d(TAG, "⬆️ Cabeza ARRIBA (pitch=${"%.4f".format(pitchDifference)}, roll=${rollAngle.toInt()}°)")
            }
        }
        
        // Log periódico de valores (cada 30 frames)
        if (frameCount % 30 == 0) {
            Log.d(TAG, "📊 Pitch=${"%.4f".format(pitchDifference)}, Roll=${rollAngle.toInt()}°, Down=$isHeadDown, Pos=$position")
        }

        return HeadPosition(
            isHeadDown = isHeadDown,
            position = position,
            distanceNoseMouth = distanceNoseMouth,
            distanceForeheadNose = distanceForeheadNose,
            noFaceDetected = false,
            rollAngle = rollAngle,
            isHeadTilted = absRoll > ROLL_MILD_THRESHOLD
        )
    }

    /**
     * Calcular ángulo ROLL (inclinación lateral de la cabeza)
     */
    private fun calculateRollAngle(leftEye: NormalizedLandmark, rightEye: NormalizedLandmark): Float {
        val dx = rightEye.x() - leftEye.x()
        val dy = rightEye.y() - leftEye.y()
        
        val angleRad = atan2(dy.toDouble(), dx.toDouble())
        return Math.toDegrees(angleRad).toFloat()
    }

    private fun euclideanDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return sqrt(dx * dx + dy * dy)
    }
    
    fun reset() {
        wasHeadDown = false
        lastKnownHeadDown = false
        frameCount = 0
        Log.d(TAG, "🔄 DetectHeadPosition reseteado")
    }
}