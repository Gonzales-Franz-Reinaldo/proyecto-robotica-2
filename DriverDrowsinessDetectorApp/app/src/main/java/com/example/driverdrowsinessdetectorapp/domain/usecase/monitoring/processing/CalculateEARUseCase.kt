package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Caso de Uso: Calcular Eye Aspect Ratio (EAR)
 * 
 * Equivalente a: drowsiness_processor/data_processing/eyes/eyes_processing.py
 * 
 * Fórmula EAR:
 * EAR = (|p2-p6| + |p3-p5|) / (2 * |p1-p4|)
 * 
 * Donde:
 * - p1, p4: Extremos del ojo (horizontal)
 * - p2, p6: Puntos superiores e inferiores (vertical izquierda)
 * - p3, p5: Puntos superiores e inferiores (vertical derecha)
 * 
 * Índices MediaPipe Face Mesh:
 * - Ojo izquierdo: [33, 160, 158, 133, 153, 144]
 * - Ojo derecho: [362, 385, 387, 263, 373, 380]
 */
class CalculateEARUseCase @Inject constructor() {
    
    companion object {
        // Índices de landmarks para ojo izquierdo (MediaPipe Face Mesh)
        private val LEFT_EYE_INDICES = intArrayOf(33, 160, 158, 133, 153, 144)
        
        // Índices de landmarks para ojo derecho
        private val RIGHT_EYE_INDICES = intArrayOf(362, 385, 387, 263, 373, 380)
    }
    
    /**
     * Calcular EAR promedio de ambos ojos
     * 
     * @param faceLandmarks Lista de 468 landmarks faciales
     * @return EAR promedio (0.0 = ojos cerrados, ~0.3 = ojos abiertos)
     */
    operator fun invoke(faceLandmarks: List<NormalizedLandmark>): Float {
        if (faceLandmarks.size < 468) {
            return 0f // Sin suficientes landmarks
        }
        
        // Calcular EAR para cada ojo
        val earLeft = calculateEyeAspectRatio(faceLandmarks, LEFT_EYE_INDICES)
        val earRight = calculateEyeAspectRatio(faceLandmarks, RIGHT_EYE_INDICES)
        
        // Retornar promedio
        return (earLeft + earRight) / 2f
    }
    
    /**
     * Calcular EAR para un ojo específico
     * 
     * @param landmarks Todos los landmarks faciales
     * @param indices Índices de los 6 puntos del ojo [p1, p2, p3, p4, p5, p6]
     * @return EAR del ojo
     */
    private fun calculateEyeAspectRatio(
        landmarks: List<NormalizedLandmark>,
        indices: IntArray
    ): Float {
        // Extraer puntos del ojo
        val p1 = landmarks[indices[0]]
        val p2 = landmarks[indices[1]]
        val p3 = landmarks[indices[2]]
        val p4 = landmarks[indices[3]]
        val p5 = landmarks[indices[4]]
        val p6 = landmarks[indices[5]]
        
        // Calcular distancias verticales
        val vertical1 = euclideanDistance(p2, p6)
        val vertical2 = euclideanDistance(p3, p5)
        
        // Calcular distancia horizontal
        val horizontal = euclideanDistance(p1, p4)
        
        // Evitar división por cero
        if (horizontal == 0f) return 0f
        
        // Fórmula EAR
        return (vertical1 + vertical2) / (2f * horizontal)
    }
    
    /**
     * Calcular distancia euclidiana 2D entre dos puntos
     * 
     * @param p1 Primer punto
     * @param p2 Segundo punto
     * @return Distancia euclidiana
     */
    private fun euclideanDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return sqrt(dx * dx + dy * dy)
    }
}