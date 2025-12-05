package com.example.driverdrowsinessdetectorapp.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Request para enviar una alerta de somnolencia al backend.
 */
data class AlertTriggerRequest(
    @SerializedName("alert_level")
    val alertLevel: String,  // "critical", "high", "medium", "low", "normal"
    
    @SerializedName("alert_type")
    val alertType: String,   // "microsleep", "nodding", "yawn", "eye_rubbing", "excessive_blinking", "normal"
    
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("session_id")
    val sessionId: String? = null,
    
    @SerializedName("metrics")
    val metrics: Map<String, Any>? = null
)