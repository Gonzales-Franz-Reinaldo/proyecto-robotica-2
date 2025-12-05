package com.example.driverdrowsinessdetectorapp.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del backend al enviar una alerta.
 */
data class AlertTriggerResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("alert_level")
    val alertLevel: String,
    
    @SerializedName("alert_type")
    val alertType: String,
    
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("actions")
    val actions: AlertActions,
    
    @SerializedName("message")
    val message: String?
)

data class AlertActions(
    @SerializedName("siren")
    val siren: Boolean,
    
    @SerializedName("led")
    val led: Boolean?
)