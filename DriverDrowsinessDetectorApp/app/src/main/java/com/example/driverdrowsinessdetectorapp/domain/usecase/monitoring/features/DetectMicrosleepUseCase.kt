package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.features

import android.util.Log
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing.EyeDistances
import javax.inject.Inject

class DetectMicrosleepUseCase @Inject constructor() {
    
    companion object {
        private const val TAG = "DetectMicrosleepUseCase"
        private const val MICROSLEEP_DURATION_MS = 2500L
        private const val EAR_THRESHOLD = 0.2f
        
        // Umbral para considerar que los ojos están "suficientemente cerrados"
        // Durante bostezo el EAR baja pero no tanto como en microsueño real
        private const val EAR_STRICT_THRESHOLD = 0.15f  // Más estricto para evitar falsos positivos
    }
    
    private var eyesClosedStartTime: Long? = null
    private var microsleepCount = 0
    private val microsleepDurations = mutableListOf<Long>()
    private var isCurrentlyInMicrosleep = false
    
    /**
     *  Recibe también si hay bostezo activo
     */
    operator fun invoke(
        eyeDistances: EyeDistances,
        isMouthWideOpen: Boolean = false  
    ): Triple<Boolean, Int, List<Long>> {
        val currentTime = System.currentTimeMillis()

        // CALCULAR EAR
        val earRight = if (eyeDistances.horizontalRightEye > 0) {
            eyeDistances.verticalRightEyelid / eyeDistances.horizontalRightEye
        } else 0f
        
        val earLeft = if (eyeDistances.horizontalLeftEye > 0) {
            eyeDistances.verticalLeftEyelid / eyeDistances.horizontalLeftEye
        } else 0f
        
        val avgEar = (earRight + earLeft) / 2f
        
        // Diferentes umbrales según contexto
        
        val isEyesClosed: Boolean
        val reason: String
        
        if (isMouthWideOpen) {
            // DURANTE BOSTEZO: Usar umbral MÁS ESTRICTO
            // Solo considerar microsueño si los ojos están MUY cerrados
            isEyesClosed = avgEar < EAR_STRICT_THRESHOLD
            reason = "bostezo activo, umbral estricto=${EAR_STRICT_THRESHOLD}"
            
            if (avgEar < EAR_THRESHOLD && avgEar >= EAR_STRICT_THRESHOLD) {
                Log.d(TAG, "👁️🥱 Ojos entrecerrados por BOSTEZO (EAR=${"%.3f".format(avgEar)}) - IGNORANDO")
            }
        } else {
            //  NORMAL: Usar umbral estándar
            isEyesClosed = avgEar < EAR_THRESHOLD
            reason = "normal, umbral=${EAR_THRESHOLD}"
        }

        // Log periódico
        if (System.currentTimeMillis() % 1000 < 50) {
            Log.d(TAG, "EAR: ${"%.3f".format(avgEar)}, Closed=$isEyesClosed ($reason), MouthOpen=$isMouthWideOpen")
        }

        if (isEyesClosed) {
            if (eyesClosedStartTime == null) {
                eyesClosedStartTime = currentTime
                isCurrentlyInMicrosleep = false
                Log.d(TAG, "👁️ Ojos cerrados (EAR=${"%.3f".format(avgEar)}, bostezo=$isMouthWideOpen)")
            }

            val duration = currentTime - (eyesClosedStartTime ?: currentTime)

            if (duration >= MICROSLEEP_DURATION_MS && !isCurrentlyInMicrosleep) {
                isCurrentlyInMicrosleep = true
                microsleepCount++
                microsleepDurations.add(duration)
                
                return Triple(true, microsleepCount, microsleepDurations)
            }
            
            // Log de progreso
            if (duration > 0 && duration % 500 < 50) {
                val progress = ((duration.toFloat() / MICROSLEEP_DURATION_MS) * 100).toInt().coerceAtMost(100)
                Log.d(TAG, "⏱️ Ojos cerrados: ${duration}ms / ${MICROSLEEP_DURATION_MS}ms ($progress%)")
            }

            return Triple(isCurrentlyInMicrosleep, microsleepCount, microsleepDurations)
        } else {
            // Ojos abiertos - reset
            if (eyesClosedStartTime != null) {
                val duration = currentTime - (eyesClosedStartTime ?: currentTime)
                Log.d(TAG, "👁️ Ojos abiertos (duración cerrados: ${duration}ms)")
            }
            eyesClosedStartTime = null
            isCurrentlyInMicrosleep = false
        }
        
        return Triple(false, microsleepCount, microsleepDurations)
    }
    
    fun reset() {
        eyesClosedStartTime = null
        microsleepCount = 0
        microsleepDurations.clear()
        isCurrentlyInMicrosleep = false
        Log.d(TAG, "🔄 DetectMicrosleep reseteado")
    }
}