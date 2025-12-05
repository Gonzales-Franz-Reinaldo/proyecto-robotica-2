package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.features

import android.util.Log
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing.EyeDistances
import javax.inject.Inject

class DetectBlinkUseCase @Inject constructor() {

    companion object {
        private const val TAG = "DetectBlinkUseCase"
        private const val BLINK_WINDOW_MS = 60_000L  // ← 60 segundos
        private const val BLINK_COUNT_THRESHOLD = 20
        private const val EAR_THRESHOLD = 0.2f
    }

    private var wasEyesClosed = false
    private val blinkTimestamps = mutableListOf<Long>() 

    operator fun invoke(eyeDistances: EyeDistances): Triple<Boolean, Int, Boolean> {
        val currentTime = System.currentTimeMillis()

        // CALCULAR EAR
        val earRight = if (eyeDistances.horizontalRightEye > 0) {
            eyeDistances.verticalRightEyelid / eyeDistances.horizontalRightEye
        } else 0f
        
        val earLeft = if (eyeDistances.horizontalLeftEye > 0) {
            eyeDistances.verticalLeftEyelid / eyeDistances.horizontalLeftEye
        } else 0f
        
        val avgEar = (earRight + earLeft) / 2f
        val isEyesClosed = avgEar < EAR_THRESHOLD

        var isBlinking = false

        // Detectar parpadeo completo (cerrado → abierto)
        if (wasEyesClosed && !isEyesClosed) {
            blinkTimestamps.add(currentTime)
            isBlinking = true
            Log.d(TAG, "👁️ Parpadeo detectado (EAR: $avgEar)")
        }

        wasEyesClosed = isEyesClosed

        // LIMPIAR timestamps fuera de ventana (últimos 60 segundos)
        val cutoffTime = currentTime - BLINK_WINDOW_MS
        blinkTimestamps.removeAll { it < cutoffTime }

        val blinkCount = blinkTimestamps.size

        // LOG cuando excede umbral
        if (blinkCount > BLINK_COUNT_THRESHOLD) {
            Log.w(TAG, "⚠️ EXCEDE UMBRAL: $blinkCount parpadeos en 60s")
        }

        return Triple(isBlinking, blinkCount, isEyesClosed)
    }

    fun reset() {
        wasEyesClosed = false
        blinkTimestamps.clear()
    }
}