package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing

import android.util.Log
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.sqrt

data class HandDetectionResult(
    val faceLandmarks: List<NormalizedLandmark>,
    val handLandmarks: List<List<NormalizedLandmark>>?,
    val handedness: List<List<Category>>?
)

class DetectHandNearEyesUseCase @Inject constructor() {
    
    companion object {
        private const val TAG = "DetectHandNearEyes"
        
        // Índices de puntas de dedos
        private const val THUMB_TIP = 4
        private const val INDEX_FINGER_TIP = 8
        private const val MIDDLE_FINGER_TIP = 12
        private const val RING_FINGER_TIP = 16
        private const val PINKY_TIP = 20
        
        //  También incluir nudillos para mejor detección de palma
        private const val INDEX_MCP = 5
        private const val MIDDLE_MCP = 9
        private const val RING_MCP = 13
        private const val PINKY_MCP = 17
        private const val WRIST = 0
        
        // Índices de ojos
        private const val RIGHT_EYE_CENTER = 33
        private const val LEFT_EYE_CENTER = 263
        
        //  UMBRAL AUMENTADO para mejor detección
        private const val PROXIMITY_THRESHOLD_NORMALIZED = 0.12f  // 12% del ancho de imagen
        
        private var frameCount = 0
    }
    
    operator fun invoke(
        faceLandmarks: List<NormalizedLandmark>,
        handLandmarks: List<List<NormalizedLandmark>>?,
        handedness: List<List<Category>>?
    ): Map<String, Boolean> {
        frameCount++
        
        if (faceLandmarks.size < 478 || handLandmarks.isNullOrEmpty()) {
            return mapOf(
                "MANO_IZQUIERDA_OJO_DERECHO" to false,
                "MANO_IZQUIERDA_OJO_IZQUIERDO" to false,
                "MANO_DERECHA_OJO_DERECHO" to false,
                "MANO_DERECHA_OJO_IZQUIERDO" to false
            )
        }
        
        val rightEye = faceLandmarks[RIGHT_EYE_CENTER]
        val leftEye = faceLandmarks[LEFT_EYE_CENTER]
        
        val result = mutableMapOf<String, Boolean>()
        
        handLandmarks.forEachIndexed { index, hand ->
            // OBTENER LATERALIDAD (MediaPipe devuelve desde SU perspectiva)
            val mediaPipeLabel = handedness?.getOrNull(index)?.firstOrNull()?.categoryName() ?: "Unknown"
            
            // INVERTIR LATERALIDAD (cámara frontal = espejo)
            val userHandLabel = when (mediaPipeLabel) {
                "Left" -> "DERECHA"
                "Right" -> "IZQUIERDA"
                else -> "Unknown"
            }
            
            //  Verificar proximidad con TODOS los puntos relevantes de la mano
            val (isNearRightEye, minDistRight) = isHandNearEye(hand, rightEye)
            val (isNearLeftEye, minDistLeft) = isHandNearEye(hand, leftEye)
            
            when (userHandLabel) {
                "IZQUIERDA" -> {
                    result["MANO_IZQUIERDA_OJO_DERECHO"] = isNearRightEye
                    result["MANO_IZQUIERDA_OJO_IZQUIERDO"] = isNearLeftEye
                    if ((isNearRightEye || isNearLeftEye) && frameCount % 10 == 0) {
                        Log.d(TAG, "👁️✋ MANO IZQ cerca: Der=$isNearRightEye (${String.format("%.3f", minDistRight)}), Izq=$isNearLeftEye (${String.format("%.3f", minDistLeft)})")
                    }
                }
                "DERECHA" -> {
                    result["MANO_DERECHA_OJO_DERECHO"] = isNearRightEye
                    result["MANO_DERECHA_OJO_IZQUIERDO"] = isNearLeftEye
                    if ((isNearRightEye || isNearLeftEye) && frameCount % 10 == 0) {
                        Log.d(TAG, "👁️🤚 MANO DER cerca: Der=$isNearRightEye (${String.format("%.3f", minDistRight)}), Izq=$isNearLeftEye (${String.format("%.3f", minDistLeft)})")
                    }
                }
            }
        }
        
        result.putIfAbsent("MANO_IZQUIERDA_OJO_DERECHO", false)
        result.putIfAbsent("MANO_IZQUIERDA_OJO_IZQUIERDO", false)
        result.putIfAbsent("MANO_DERECHA_OJO_DERECHO", false)
        result.putIfAbsent("MANO_DERECHA_OJO_IZQUIERDO", false)
        
        return result
    }
    
    /**
     *  Verificar si CUALQUIER punto de la mano está cerca del ojo
     * Retorna (isNear, minDistance)
     */
    private fun isHandNearEye(hand: List<NormalizedLandmark>, eye: NormalizedLandmark): Pair<Boolean, Float> {
        if (hand.size < 21) return Pair(false, Float.MAX_VALUE)
        
        //  Incluir puntas de dedos Y nudillos para detectar palma completa
        val handPoints = listOf(
            hand[THUMB_TIP],
            hand[INDEX_FINGER_TIP],
            hand[MIDDLE_FINGER_TIP],
            hand[RING_FINGER_TIP],
            hand[PINKY_TIP],
            hand[INDEX_MCP],
            hand[MIDDLE_MCP],
            hand[RING_MCP],
            hand[PINKY_MCP],
            hand[WRIST]
        )
        
        val distances = handPoints.map { point ->
            euclideanDistance(point, eye)
        }
        
        val minDist = distances.minOrNull() ?: Float.MAX_VALUE
        val isNear = minDist < PROXIMITY_THRESHOLD_NORMALIZED
        
        return Pair(isNear, minDist)
    }
    
    private fun euclideanDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return sqrt(dx * dx + dy * dy)
    }
}