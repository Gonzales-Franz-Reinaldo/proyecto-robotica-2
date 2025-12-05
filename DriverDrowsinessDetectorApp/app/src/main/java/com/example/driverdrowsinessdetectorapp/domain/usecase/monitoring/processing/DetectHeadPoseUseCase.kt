package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing

import com.example.driverdrowsinessdetectorapp.domain.model.HeadPose
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Caso de Uso: Detectar Posición de la Cabeza (HEAD_POSE)
 * 
 * Equivalente a: drowsiness_processor/data_processing/head/head_processing.py
 * 
 * Calcula los ángulos Pitch, Yaw, Roll usando landmarks faciales
 * 
 * - Pitch: Inclinación arriba/abajo (cabeceo)
 * - Yaw: Rotación izquierda/derecha
 * - Roll: Inclinación lateral
 */
class DetectHeadPoseUseCase @Inject constructor() {
    
    companion object {
        // Índices de landmarks clave para cálculo de HEAD_POSE
        private const val NOSE_TIP = 1        // Punta de la nariz
        private const val CHIN = 152          // Barbilla
        private const val LEFT_EYE = 33       // Ojo izquierdo
        private const val RIGHT_EYE = 263     // Ojo derecho
        private const val LEFT_MOUTH = 61     // Comisura izquierda
        private const val RIGHT_MOUTH = 291   // Comisura derecha
    }
    
    /**
     * Detectar orientación de la cabeza
     * 
     * @param faceLandmarks Lista de 468 landmarks faciales
     * @return HeadPose con ángulos pitch, yaw, roll
     */
    operator fun invoke(faceLandmarks: List<NormalizedLandmark>): HeadPose {
        if (faceLandmarks.size < 468) {
            return HeadPose.NEUTRAL
        }
        
        // Extraer landmarks clave
        val noseTip = faceLandmarks[NOSE_TIP]
        val chin = faceLandmarks[CHIN]
        val leftEye = faceLandmarks[LEFT_EYE]
        val rightEye = faceLandmarks[RIGHT_EYE]
        val leftMouth = faceLandmarks[LEFT_MOUTH]
        val rightMouth = faceLandmarks[RIGHT_MOUTH]
        
        // Calcular Pitch (inclinación vertical - cabeceo)
        val pitch = calculatePitch(noseTip, chin)
        
        // Calcular Yaw (rotación horizontal)
        val yaw = calculateYaw(leftEye, rightEye, noseTip)
        
        // Calcular Roll (inclinación lateral)
        val roll = calculateRoll(leftEye, rightEye)
        
        return HeadPose(
            pitch = pitch,
            yaw = yaw,
            roll = roll
        )
    }
    
    /**
     * Calcular Pitch (ángulo de inclinación vertical)
     * 
     * Pitch > 20° = Cabeza hacia abajo (cabeceo)
     * Pitch < -20° = Cabeza hacia arriba
     */
    private fun calculatePitch(noseTip: NormalizedLandmark, chin: NormalizedLandmark): Float {
        val dy = chin.y() - noseTip.y()
        val dz = (chin.z() ?: 0f) - (noseTip.z() ?: 0f)
        
        val angleRad = atan2(dy, dz)
        return Math.toDegrees(angleRad.toDouble()).toFloat()
    }
    
    /**
     * Calcular Yaw (ángulo de rotación horizontal)
     * 
     * Yaw > 30° = Cabeza girada a la derecha
     * Yaw < -30° = Cabeza girada a la izquierda
     */
    private fun calculateYaw(
        leftEye: NormalizedLandmark,
        rightEye: NormalizedLandmark,
        noseTip: NormalizedLandmark
    ): Float {
        // Centro entre ojos
        val eyeCenterX = (leftEye.x() + rightEye.x()) / 2f
        
        // Diferencia con nariz
        val dx = noseTip.x() - eyeCenterX
        val dz = (noseTip.z() ?: 0f) - ((leftEye.z() ?: 0f) + (rightEye.z() ?: 0f)) / 2f
        
        val angleRad = atan2(dx, dz)
        return Math.toDegrees(angleRad.toDouble()).toFloat()
    }
    
    /**
     * Calcular Roll (ángulo de inclinación lateral)
     * 
     * Roll > 15° = Cabeza inclinada a la derecha
     * Roll < -15° = Cabeza inclinada a la izquierda
     */
    private fun calculateRoll(leftEye: NormalizedLandmark, rightEye: NormalizedLandmark): Float {
        val dx = rightEye.x() - leftEye.x()
        val dy = rightEye.y() - leftEye.y()
        
        val angleRad = atan2(dy, dx)
        return Math.toDegrees(angleRad.toDouble()).toFloat()
    }
}