package com.example.driverdrowsinessdetectorapp.data.remote.api

import com.example.driverdrowsinessdetectorapp.data.remote.dto.request.AlertTriggerRequest
import com.example.driverdrowsinessdetectorapp.data.remote.dto.response.AlertTriggerResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API para enviar alertas de somnolencia al backend.
 * El backend se encarga de activar la sirena Tuya y los LEDs del ESP32.
 */
interface AlertApi {
    
    /**
     * Envía una alerta de somnolencia al backend.
     * 
     * - CRITICAL: Activa sirena (5 seg) + LED rojo
     * - HIGH/MEDIUM: Activa LED amarillo
     * - NORMAL: Activa LED verde
     */
    @POST("/api/v1/alerts/trigger")
    suspend fun triggerAlert(@Body request: AlertTriggerRequest): AlertTriggerResponse
}