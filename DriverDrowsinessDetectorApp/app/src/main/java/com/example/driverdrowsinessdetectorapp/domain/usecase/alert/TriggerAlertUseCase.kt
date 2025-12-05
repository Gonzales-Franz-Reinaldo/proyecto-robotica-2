package com.example.driverdrowsinessdetectorapp.domain.usecase.alert

import android.util.Log
import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.domain.model.MetricasSomnolencia
import com.example.driverdrowsinessdetectorapp.domain.repository.AlertRepository
import javax.inject.Inject

/**
 * Caso de uso para enviar alertas de somnolencia al backend.
 * El backend se encarga de activar la sirena Tuya y los LEDs del ESP32.
 */
class TriggerAlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository
) {
    companion object {
        private const val TAG = "TriggerAlertUseCase"
        
        // Cooldown para evitar spam de alertas (en milisegundos)
        private const val CRITICAL_COOLDOWN_MS = 10000L  // 10 segundos entre alertas críticas
        private const val NORMAL_COOLDOWN_MS = 5000L    // 5 segundos entre otras alertas
    }
    
    private var lastCriticalAlertTime = 0L
    private var lastAlertTime = 0L
    private var lastAlertLevel: AlertLevel? = null
    
    /**
     * Envía una alerta al backend si corresponde.
     * Implementa cooldown para evitar spam de alertas.
     * 
     * @param metrics Métricas de somnolencia actuales
     * @param userId ID del usuario
     * @param sessionId ID de la sesión
     * @return true si la alerta se envió, false si se omitió por cooldown
     */
    suspend operator fun invoke(
        metrics: MetricasSomnolencia,
        userId: Int? = null,
        sessionId: String? = null
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        val alertLevel = metrics.alertLevel
        val alertType = metrics.alertType  // Puede ser null
        
        // Verificar si debemos enviar la alerta
        if (!shouldSendAlert(alertLevel, currentTime)) {
            return false
        }
        
        Log.d(TAG, "🚨 Enviando alerta al backend: $alertLevel - $alertType")
        
        val result = alertRepository.triggerAlert(
            alertLevel = alertLevel,
            alertType = alertType,
            userId = userId,
            sessionId = sessionId,
            metrics = metrics
        )
        
        // Actualizar tiempos de última alerta
        if (result.isSuccess) {
            lastAlertTime = currentTime
            lastAlertLevel = alertLevel
            
            if (alertLevel == AlertLevel.CRITICAL) {
                lastCriticalAlertTime = currentTime
            }
            
            Log.d(TAG, "✅ Alerta enviada al backend exitosamente")
        } else {
            Log.e(TAG, "❌ Error al enviar alerta al backend")
        }
        
        return result.isSuccess && result.getOrDefault(false)
    }
    
    /**
     * Determina si se debe enviar una alerta basándose en:
     * - Cambio de nivel de alerta
     * - Cooldown entre alertas
     */
    private fun shouldSendAlert(alertLevel: AlertLevel, currentTime: Long): Boolean {
        // Si el nivel cambió, enviar inmediatamente
        if (alertLevel != lastAlertLevel) {
            Log.d(TAG, "📊 Nivel de alerta cambió: $lastAlertLevel -> $alertLevel")
            return true
        }
        
        // Si es NORMAL, no enviar constantemente
        if (alertLevel == AlertLevel.NORMAL) {
            // Solo enviar NORMAL si antes había una alerta activa
            val timeSinceLastAlert = currentTime - lastAlertTime
            return lastAlertLevel != AlertLevel.NORMAL && timeSinceLastAlert > NORMAL_COOLDOWN_MS
        }
        
        // Para alertas críticas, verificar cooldown
        if (alertLevel == AlertLevel.CRITICAL) {
            val timeSinceLastCritical = currentTime - lastCriticalAlertTime
            if (timeSinceLastCritical < CRITICAL_COOLDOWN_MS) {
                Log.d(TAG, "⏳ Cooldown crítico: ${(CRITICAL_COOLDOWN_MS - timeSinceLastCritical) / 1000}s restantes")
                return false
            }
            return true
        }
        
        // Para otras alertas, verificar cooldown normal
        val timeSinceLastAlert = currentTime - lastAlertTime
        if (timeSinceLastAlert < NORMAL_COOLDOWN_MS) {
            return false
        }
        
        return true
    }
    
    /**
     * Resetea el estado del caso de uso (útil al iniciar nueva sesión)
     */
    fun reset() {
        lastCriticalAlertTime = 0L
        lastAlertTime = 0L
        lastAlertLevel = null
        Log.d(TAG, "🔄 Estado reseteado")
    }
}