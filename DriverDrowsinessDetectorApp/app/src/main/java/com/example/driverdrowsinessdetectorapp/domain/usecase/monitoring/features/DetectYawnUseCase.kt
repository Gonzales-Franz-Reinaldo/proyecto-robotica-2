package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.features

import android.util.Log
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing.MouthDistances
import javax.inject.Inject

class DetectYawnUseCase @Inject constructor() {
    
    companion object {
        private const val TAG = "DetectYawnUseCase"
        private const val YAWN_DURATION_MS = 2500L 
        private const val YAWN_WINDOW_MS = 180_000L  // ← 180 segundos (3 minutos)
    }
    
    private var mouthOpenStartTime: Long? = null
    private var isCurrentlyYawning = false
    private var isMouthOpen = false
    private val yawnTimestamps = mutableListOf<Long>() 
    private val yawnDurations = mutableListOf<Long>()
    
    operator fun invoke(mouthDistances: MouthDistances): Triple<Boolean, Int, List<Long>> {
        val currentTime = System.currentTimeMillis()
        
        if (mouthDistances.distanciaLabios > mouthDistances.distanciaMenton) {
            if (!isMouthOpen) {
                mouthOpenStartTime = currentTime
                isCurrentlyYawning = false
                isMouthOpen = true
                Log.d(TAG, "👄 Boca abierta")
            }
        } else {
            if (isMouthOpen) {
                val duration = currentTime - (mouthOpenStartTime ?: currentTime)
                isMouthOpen = false
                mouthOpenStartTime = null
                
                // BOSTEZO = BOCA ABIERTA > 2.5 SEGUNDOS
                if (duration > YAWN_DURATION_MS && !isCurrentlyYawning) {
                    isCurrentlyYawning = true
                    yawnTimestamps.add(currentTime) 
                    yawnDurations.add(duration)
                    Log.d(TAG, "🚨 BOSTEZO DETECTADO: ${duration}ms")
                    
                    // LIMPIAR timestamps fuera de ventana (últimos 3 minutos)
                    val cutoffTime = currentTime - YAWN_WINDOW_MS
                    yawnTimestamps.removeAll { it < cutoffTime }
                    
                    val yawnCount = yawnTimestamps.size
                    
                    // LOG cuando excede umbral
                    if (yawnCount > 3) {
                        Log.w(TAG, "⚠️ EXCEDE UMBRAL: $yawnCount bostezos en 3 minutos")
                    }
                    
                    return Triple(true, yawnCount, yawnDurations)
                } else {
                    isCurrentlyYawning = false
                }
            }
        }
        
        // LIMPIAR timestamps fuera de ventana (incluso cuando no hay bostezo)
        val cutoffTime = currentTime - YAWN_WINDOW_MS
        yawnTimestamps.removeAll { it < cutoffTime }
        
        return Triple(false, yawnTimestamps.size, yawnDurations)
    }
    
    fun reset() {
        mouthOpenStartTime = null
        yawnTimestamps.clear()
        yawnDurations.clear()
        isCurrentlyYawning = false
        isMouthOpen = false
    }
}