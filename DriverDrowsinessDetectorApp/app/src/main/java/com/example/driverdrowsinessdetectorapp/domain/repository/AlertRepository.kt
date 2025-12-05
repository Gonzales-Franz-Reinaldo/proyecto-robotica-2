package com.example.driverdrowsinessdetectorapp.domain.repository

import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.domain.model.AlertType
import com.example.driverdrowsinessdetectorapp.domain.model.MetricasSomnolencia

/**
 * Repositorio para enviar alertas de somnolencia al backend.
 */
interface AlertRepository {
    
    /**
     * Envía una alerta al backend para activar dispositivos IoT.
     * 
     * @param alertLevel Nivel de la alerta (CRITICAL, HIGH, MEDIUM, NORMAL)
     * @param alertType Tipo de evento detectado (puede ser null)
     * @param userId ID del usuario (opcional)
     * @param sessionId ID de la sesión de monitoreo (opcional)
     * @param metrics Métricas de somnolencia (opcional)
     * @return true si la alerta se envió correctamente
     */
    suspend fun triggerAlert(
        alertLevel: AlertLevel,
        alertType: AlertType?,
        userId: Int? = null,
        sessionId: String? = null,
        metrics: MetricasSomnolencia? = null
    ): Result<Boolean>
}