package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing

import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Caso de Uso: Calcular Distancias de Párpados
 *
 * Equivalente EXACTO a: eyes_processing.py - ProcesamientoOjos
 *
 * Python usa: puntos_ojos['distancias'] = [159, 145, 385, 374, 33, 133, 362, 263]
 */
data class EyeDistances(
    val verticalRightEyelid: Float,  // Distancia VERTICA ojo derecho
    val verticalLeftEyelid: Float,   // Distancia VERTICA ojo izquierdo
    val horizontalRightEye: Float,   // Distancia HORIZONTAL ojo derecho
    val horizontalLeftEye: Float     // Distancia HORIZONTAL ojo izquierdo
)

class CalculateEyeDistancesUseCase @Inject constructor() {

    companion object {
        private const val TAG = "CalculateEyeDistances"
        
        //  ÍNDICES CORRECTOS SEGÚN MEDIAPIPE FACE MESH
        // Ojo derecho (RIGHT EYE)
        private const val RIGHT_EYE_TOP = 159        // Superior centro
        private const val RIGHT_EYE_BOTTOM = 145     // Inferior centro
        private const val RIGHT_EYE_LEFT = 33        // Extremo izquierdo
        private const val RIGHT_EYE_RIGHT = 133      // Extremo derecho
        
        // Ojo izquierdo (LEFT EYE)
        private const val LEFT_EYE_TOP = 386         // Superior centro
        private const val LEFT_EYE_BOTTOM = 374      // Inferior centro
        private const val LEFT_EYE_LEFT = 362        // Extremo izquierdo
        private const val LEFT_EYE_RIGHT = 263       // Extremo derecho
    }

    operator fun invoke(faceLandmarks: List<NormalizedLandmark>): EyeDistances {
        //  VERIFICACIÓN CORRECTA
        if (faceLandmarks.size < 468) {
            Log.w(TAG, "⚠️ Landmarks insuficientes: ${faceLandmarks.size}")
            return EyeDistances(0f, 0f, 0f, 0f)
        }

        try {
            // OJO DERECHO
            val rightTop = faceLandmarks[RIGHT_EYE_TOP]
            val rightBottom = faceLandmarks[RIGHT_EYE_BOTTOM]
            val rightLeft = faceLandmarks[RIGHT_EYE_LEFT]
            val rightRight = faceLandmarks[RIGHT_EYE_RIGHT]
            
            // OJO IZQUIERDO
            val leftTop = faceLandmarks[LEFT_EYE_TOP]
            val leftBottom = faceLandmarks[LEFT_EYE_BOTTOM]
            val leftLeft = faceLandmarks[LEFT_EYE_LEFT]
            val leftRight = faceLandmarks[LEFT_EYE_RIGHT]

            //  CALCULAR DISTANCIAS VERTICALES (para detectar cierre)
            val verticalRight = euclideanDistance(rightTop, rightBottom)
            val verticalLeft = euclideanDistance(leftTop, leftBottom)
            
            //  CALCULAR DISTANCIAS HORIZONTALES (para normalizar)
            val horizontalRight = euclideanDistance(rightLeft, rightRight)
            val horizontalLeft = euclideanDistance(leftLeft, leftRight)

            Log.d(TAG, "📏 Vertical Right=$verticalRight, Horizontal Right=$horizontalRight")
            Log.d(TAG, "📏 Vertical Left=$verticalLeft, Horizontal Left=$horizontalLeft")

            return EyeDistances(
                verticalRightEyelid = verticalRight,
                verticalLeftEyelid = verticalLeft,
                horizontalRightEye = horizontalRight,
                horizontalLeftEye = horizontalLeft
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            return EyeDistances(0f, 0f, 0f, 0f)
        }
    }

    private fun euclideanDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return sqrt(dx * dx + dy * dy)
    }
}