package com.example.driverdrowsinessdetectorapp.data.repository

import android.util.Log
import com.example.driverdrowsinessdetectorapp.data.remote.api.AlertApi
import com.example.driverdrowsinessdetectorapp.data.remote.dto.request.AlertTriggerRequest
import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.domain.model.AlertType
import com.example.driverdrowsinessdetectorapp.domain.model.MetricasSomnolencia
import com.example.driverdrowsinessdetectorapp.domain.repository.AlertRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val alertApi: AlertApi
) : AlertRepository {
    
    companion object {
        private const val TAG = "AlertRepository"
    }
    
    override suspend fun triggerAlert(
        alertLevel: AlertLevel,
        alertType: AlertType?,
        userId: Int?,
        sessionId: String?,
        metrics: MetricasSomnolencia?
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "📤 Enviando alerta: $alertLevel - $alertType")
            
            val request = AlertTriggerRequest(
                alertLevel = alertLevel.toApiString(),
                alertType = alertType?.toApiString() ?: "normal",
                userId = userId,
                sessionId = sessionId,
                metrics = metrics?.toMetricsMap()
            )
            
            val response = alertApi.triggerAlert(request)
            
            if (response.success) {
                Log.d(TAG, "✅ Alerta enviada correctamente: ${response.message}")
                Log.d(TAG, "   - Sirena: ${response.actions.siren}")
                Log.d(TAG, "   - LED: ${response.actions.led}")
                Result.success(true)
            } else {
                Log.w(TAG, "⚠️ Alerta enviada pero sin éxito: ${response.message}")
                Result.success(false)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando alerta: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Convierte AlertLevel a string para la API
     */
    private fun AlertLevel.toApiString(): String {
        return when (this) {
            AlertLevel.CRITICAL -> "critical"
            AlertLevel.HIGH -> "high"
            AlertLevel.MEDIUM -> "medium"
            AlertLevel.NORMAL -> "normal"
        }
    }
    
    /**
     * Convierte AlertType a string para la API
     */
    private fun AlertType.toApiString(): String {
        return when (this) {
            AlertType.MICROSLEEP -> "microsleep"
            AlertType.HEAD_NODDING -> "nodding"
            AlertType.YAWNING -> "yawn"
            AlertType.EYE_RUB -> "eye_rubbing"
            AlertType.EXCESSIVE_BLINKING -> "excessive_blinking"
        }
    }
    
    /**
     * Convierte MetricasSomnolencia a Map para la API
     */
    private fun MetricasSomnolencia.toMetricsMap(): Map<String, Any> {
        return mapOf(
            "ear" to ear,
            "mar" to mar,
            "blink_count" to blinkCount,
            "yawn_count" to yawnCount,
            "microsleep_count" to microsleepCount,
            "nodding_count" to noddingCount,
            "is_microsleep" to isMicrosleep,
            "is_nodding" to isNodding,
            "is_yawning" to isYawning
        )
    }
}