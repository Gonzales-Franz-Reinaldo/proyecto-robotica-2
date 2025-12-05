package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Caso de Uso: Calcular Mouth Aspect Ratio (MAR)
 * 
 * Equivalente a: drowsiness_processor/data_processing/mouth/mouth_processing.py
 * 
 * Fórmula MAR:
 * MAR = (|p2-p8| + |p3-p7| + |p4-p6|) / (3 * |p1-p5|)
 * 
 * Índices MediaPipe Face Mesh para boca:
 * - p1 (izquierda): 61
 * - p2 (superior izquierda): 81
 * - p3 (superior centro): 13
 * - p4 (superior derecha): 311
 * - p5 (derecha): 291
 * - p6 (inferior derecha): 402
 * - p7 (inferior centro): 14
 * - p8 (inferior izquierda): 178
 */
class CalculateMARUseCase @Inject constructor() {
    
    companion object {
        // Índices de landmarks para boca (MediaPipe Face Mesh)
        private val MOUTH_INDICES = intArrayOf(
            61,  // p1 - Comisura izquierda
            81,  // p2 - Superior izquierda
            13,  // p3 - Superior centro
            311, // p4 - Superior derecha
            291, // p5 - Comisura derecha
            402, // p6 - Inferior derecha
            14,  // p7 - Inferior centro
            178  // p8 - Inferior izquierda
        )
    }
    
    /**
     * Calcular MAR (Mouth Aspect Ratio)
     * 
     * @param faceLandmarks Lista de 468 landmarks faciales
     * @return MAR (0.0 = boca cerrada, >0.6 = boca abierta/bostezo)
     */
    operator fun invoke(faceLandmarks: List<NormalizedLandmark>): Float {
        if (faceLandmarks.size < 468) {
            return 0f // Sin suficientes landmarks
        }
        
        // Extraer puntos de la boca
        val p1 = faceLandmarks[MOUTH_INDICES[0]]
        val p2 = faceLandmarks[MOUTH_INDICES[1]]
        val p3 = faceLandmarks[MOUTH_INDICES[2]]
        val p4 = faceLandmarks[MOUTH_INDICES[3]]
        val p5 = faceLandmarks[MOUTH_INDICES[4]]
        val p6 = faceLandmarks[MOUTH_INDICES[5]]
        val p7 = faceLandmarks[MOUTH_INDICES[6]]
        val p8 = faceLandmarks[MOUTH_INDICES[7]]
        
        // Calcular distancias verticales
        val vertical1 = euclideanDistance(p2, p8)
        val vertical2 = euclideanDistance(p3, p7)
        val vertical3 = euclideanDistance(p4, p6)
        
        // Calcular distancia horizontal
        val horizontal = euclideanDistance(p1, p5)
        
        // Evitar división por cero
        if (horizontal == 0f) return 0f
        
        // Fórmula MAR
        return (vertical1 + vertical2 + vertical3) / (3f * horizontal)
    }
    
    /**
     * Calcular distancia euclidiana 2D entre dos puntos
     */
    private fun euclideanDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return sqrt(dx * dx + dy * dy)
    }
}