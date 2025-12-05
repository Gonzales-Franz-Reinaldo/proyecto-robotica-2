package com.example.driverdrowsinessdetectorapp.domain.usecase.metrics

import android.util.Log
import com.example.driverdrowsinessdetectorapp.data.local.entity.MetricsEntity
import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.domain.model.MetricasSomnolencia
import com.example.driverdrowsinessdetectorapp.domain.repository.MetricsRepository
import javax.inject.Inject

/**
 * Caso de Uso: Guardar Métricas en Room Database
 *
 * Equivalente a: reports/main.py → ReportesSomnolencia.principal()
 */
class SaveMetricsUseCase @Inject constructor(
    private val metricsRepository: MetricsRepository
) {
    companion object {
        private const val TAG = "SaveMetricsUseCase"
    }

    /**
     * Guardar métricas solo si hay alertas relevantes
     *
     * @param sessionId ID de la sesión activa
     * @param metrics Métricas calculadas
     */
    suspend operator fun invoke(sessionId: Long, metrics: MetricasSomnolencia): Result<Long> {
        return try {
            // Solo guardar si hay alerta (como en el backend Python)
            if (shouldSaveMetrics(metrics)) {
                val entity = MetricsEntity(
                    timestamp = metrics.timestamp,
                    sessionId = sessionId,
                    ear = metrics.ear,
                    mar = metrics.mar,
                    headPitch = metrics.headPose.pitch,
                    headYaw = metrics.headPose.yaw,
                    headRoll = metrics.headPose.roll,
                    isMicrosleep = metrics.isMicrosleep,
                    microsleepCount = metrics.microsleepCount,
                    microsleepDurations = metrics.microsleepDurations,
                    isYawning = metrics.isYawning,
                    yawnCount = metrics.yawnCount,
                    yawnDurations = metrics.yawnDurations,
                    isNodding = metrics.isNodding,
                    noddingCount = metrics.noddingCount,
                    noddingDurations = metrics.noddingDurations,
                    eyeRubFirstHandDetected = metrics.eyeRubFirstHand.first,
                    eyeRubFirstHandCount = metrics.eyeRubFirstHand.second,
                    eyeRubFirstHandDurations = metrics.eyeRubFirstHand.third,
                    eyeRubSecondHandDetected = metrics.eyeRubSecondHand.first,
                    eyeRubSecondHandCount = metrics.eyeRubSecondHand.second,
                    eyeRubSecondHandDurations = metrics.eyeRubSecondHand.third,
                    alertLevel = metrics.alertLevel.name,
                    alertType = metrics.alertType?.name
                )

                val id = metricsRepository.saveMetrics(entity)

                Log.d(TAG, "✅ Métricas guardadas: ID=$id, AlertLevel=${metrics.alertLevel}")

                Result.success(id)
            } else {
                Log.d(TAG, "⏭️ Métricas sin alertas - no guardadas")
                Result.success(-1L)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar métricas: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Determinar si las métricas deben guardarse
     *
     * Equivalente a la condición del backend Python:
     * if (reporte_frotamiento_ojos_primera_mano or ... or reporte_bostezo)
     */
    private fun shouldSaveMetrics(metrics: MetricasSomnolencia): Boolean {
        return metrics.alertLevel != AlertLevel.NORMAL ||
                metrics.isMicrosleep ||
                metrics.isYawning ||
                metrics.isNodding ||
                metrics.eyeRubFirstHand.first ||
                metrics.eyeRubSecondHand.first
    }
}