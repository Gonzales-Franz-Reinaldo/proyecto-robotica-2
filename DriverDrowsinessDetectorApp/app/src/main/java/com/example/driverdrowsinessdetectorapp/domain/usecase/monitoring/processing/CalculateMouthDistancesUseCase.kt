package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing

import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * : Calcular distancias de BOCA según Python
 *
 * Python usa: boca['distancias'] = [0, 61, 81, 13, 311, 17, 402, 14, 178, 84, 405, 314, 181]
 */
data class MouthDistances(
    val distanciaLabios: Float,  // Distancia vertical labios (superior-inferior)
    val distanciaMenton: Float   // Distancia horizontal mentón
)

class CalculateMouthDistancesUseCase @Inject constructor() {

    companion object {
        private const val TAG = "CalculateMouthDistances"

        //  ÍNDICES SEGÚN PYTHON
        // boca['distancias'] = [0, 61, 81, 13, 311, 17, 402, 14, 178, 84, 405, 314, 181]
        private const val P0 = 0    // Centro boca
        private const val P61 = 61  // Labio superior izquierdo
        private const val P81 = 81  // Labio superior
        private const val P13 = 13  // Labio superior centro
        private const val P311 = 311 // Labio superior derecho
        private const val P17 = 17  // Comisura derecha
        private const val P402 = 402 // Labio inferior derecho
        private const val P14 = 14  // Labio inferior centro
        private const val P178 = 178 // Labio inferior izquierdo
        private const val P84 = 84  // Mentón izquierdo
        private const val P405 = 405 // Mentón derecho
        private const val P314 = 314 // Mentón
        private const val P181 = 181 // Labio inferior
    }

    operator fun invoke(faceLandmarks: List<NormalizedLandmark>): MouthDistances {
        if (faceLandmarks.size < 468) {
            return MouthDistances(0f, 0f)
        }

        // Extraer puntos
        val p13 = faceLandmarks[P13]   // Labio superior centro
        val p14 = faceLandmarks[P14]   // Labio inferior centro
        val p84 = faceLandmarks[P84]   // Mentón izquierdo
        val p314 = faceLandmarks[P314] // Mentón derecho

        //  CALCULAR DISTANCIAS SEGÚN PYTHON
        // distancia_labios = distancia(labio_superior, labio_inferior)
        val distanciaLabios = euclideanDistance(p13, p14)

        // distancia_menton = distancia(menton_izq, menton_der)
        val distanciaMenton = euclideanDistance(p84, p314)

        Log.d(TAG, "📏 Labios=$distanciaLabios, Mentón=$distanciaMenton")

        return MouthDistances(distanciaLabios, distanciaMenton)
    }

    private fun euclideanDistance(p1: NormalizedLandmark, p2: NormalizedLandmark): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return sqrt(dx * dx + dy * dy)
    }
}