package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.features

import android.util.Log
import javax.inject.Inject

class DetectEyeRubUseCase @Inject constructor() {
    
    companion object {
        private const val TAG = "DetectEyeRub"
        
        // UMBRALES AJUSTADOS PARA MEJOR DETECCIÓN
        private const val EYE_RUB_DURATION_MS = 500L       // Reducido: 0.5 segundos
        private const val EYE_RUB_WINDOW_MS = 300_000L     // 5 minutos
        private const val COOLDOWN_MS = 2000L              //  Evitar conteo doble
    }
    
    // MANO IZQUIERDA
    private var leftHandStartTime: Long? = null
    private var leftHandLastRubTime: Long = 0L
    private val leftHandTimestamps = mutableListOf<Long>()
    private val leftHandDurations = mutableListOf<Long>()
    private var isLeftHandCurrentlyRubbing = false
    
    // MANO DERECHA
    private var rightHandStartTime: Long? = null
    private var rightHandLastRubTime: Long = 0L
    private val rightHandTimestamps = mutableListOf<Long>()
    private val rightHandDurations = mutableListOf<Long>()
    private var isRightHandCurrentlyRubbing = false
    
    operator fun invoke(handNearEyes: Map<String, Boolean>): Map<String, Triple<Boolean, Int, List<Long>>> {
        val currentTime = System.currentTimeMillis()
        
        // DETECTAR QUÉ MANOS ESTÁN CERCA DE LOS OJOS
        val isLeftHandNear = handNearEyes["MANO_IZQUIERDA_OJO_DERECHO"] == true || 
                             handNearEyes["MANO_IZQUIERDA_OJO_IZQUIERDO"] == true
        
        val isRightHandNear = handNearEyes["MANO_DERECHA_OJO_DERECHO"] == true || 
                              handNearEyes["MANO_DERECHA_OJO_IZQUIERDO"] == true
        
        // PROCESAR MANO IZQUIERDA
        val leftResult = processHand(
            isNear = isLeftHandNear,
            currentTime = currentTime,
            startTime = leftHandStartTime,
            setStartTime = { leftHandStartTime = it },
            lastRubTime = leftHandLastRubTime,
            setLastRubTime = { leftHandLastRubTime = it },
            timestamps = leftHandTimestamps,
            durations = leftHandDurations,
            isCurrentlyRubbing = isLeftHandCurrentlyRubbing,
            setCurrentlyRubbing = { isLeftHandCurrentlyRubbing = it },
            handLabel = "IZQUIERDA"
        )
        
        // PROCESAR MANO DERECHA
        val rightResult = processHand(
            isNear = isRightHandNear,
            currentTime = currentTime,
            startTime = rightHandStartTime,
            setStartTime = { rightHandStartTime = it },
            lastRubTime = rightHandLastRubTime,
            setLastRubTime = { rightHandLastRubTime = it },
            timestamps = rightHandTimestamps,
            durations = rightHandDurations,
            isCurrentlyRubbing = isRightHandCurrentlyRubbing,
            setCurrentlyRubbing = { isRightHandCurrentlyRubbing = it },
            handLabel = "DERECHA"
        )
        
        return mapOf(
            "MANO_IZQUIERDA" to leftResult,
            "MANO_DERECHA" to rightResult
        )
    }
    
    private fun processHand(
        isNear: Boolean,
        currentTime: Long,
        startTime: Long?,
        setStartTime: (Long?) -> Unit,
        lastRubTime: Long,
        setLastRubTime: (Long) -> Unit,
        timestamps: MutableList<Long>,
        durations: MutableList<Long>,
        isCurrentlyRubbing: Boolean,
        setCurrentlyRubbing: (Boolean) -> Unit,
        handLabel: String
    ): Triple<Boolean, Int, List<Long>> {
        
        // Limpiar timestamps fuera de ventana
        val cutoffTime = currentTime - EYE_RUB_WINDOW_MS
        timestamps.removeAll { it < cutoffTime }
        
        if (isNear) {
            // MANO CERCA DE LOS OJOS
            
            if (startTime == null) {
                // INICIO: Mano acaba de acercarse
                setStartTime(currentTime)
                Log.d(TAG, "👁️✋ Mano $handLabel CERCA de ojos")
            }
            
            val duration = currentTime - (startTime ?: currentTime)
            
            //  DETECTAR FROTAMIENTO MIENTRAS LA MANO ESTÁ CERCA
            if (duration >= EYE_RUB_DURATION_MS && !isCurrentlyRubbing) {
                // Verificar cooldown para evitar conteos múltiples
                if (currentTime - lastRubTime >= COOLDOWN_MS) {
                    setCurrentlyRubbing(true)
                    setLastRubTime(currentTime)
                    timestamps.add(currentTime)
                    durations.add(duration)
                    
                    val count = timestamps.size
                    
                    if (count > 3) {
                        Log.w(TAG, "⚠️ ALERTA: $count frotamientos con mano $handLabel en 5 minutos")
                    }
                    
                    return Triple(true, count, durations.toList())
                }
            }
            
            // Log de progreso mientras está cerca
            if (duration > 0 && duration % 200 < 50) {
                val progress = ((duration.toFloat() / EYE_RUB_DURATION_MS) * 100).toInt().coerceAtMost(100)
                if (progress < 100) {
                    Log.d(TAG, "⏱️ Mano $handLabel cerca: ${duration}ms ($progress%)")
                }
            }
            
            // Retornar estado actual
            return Triple(isCurrentlyRubbing, timestamps.size, durations.toList())
            
        } else {
            // MANO ALEJADA DE LOS OJOS
            
            if (startTime != null) {
                val duration = currentTime - startTime
                Log.d(TAG, "👋 Mano $handLabel ALEJADA (estuvo cerca ${duration}ms, detectó=${isCurrentlyRubbing})")
                
                // Reset para siguiente detección
                setStartTime(null)
                setCurrentlyRubbing(false)
            }
            
            return Triple(false, timestamps.size, durations.toList())
        }
    }
    
    fun reset() {
        leftHandStartTime = null
        leftHandLastRubTime = 0L
        leftHandTimestamps.clear()
        leftHandDurations.clear()
        isLeftHandCurrentlyRubbing = false
        
        rightHandStartTime = null
        rightHandLastRubTime = 0L
        rightHandTimestamps.clear()
        rightHandDurations.clear()
        isRightHandCurrentlyRubbing = false
        
        Log.d(TAG, "🔄 DetectEyeRub reseteado")
    }
}