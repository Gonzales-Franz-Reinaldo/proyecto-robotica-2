package com.example.driverdrowsinessdetectorapp.domain.model

/**
 * Modelo: Posición de la Cabeza
 * 
 * Representa la orientación 3D de la cabeza del conductor
 */
data class HeadPose(
    val pitch: Float,  // Inclinación vertical (arriba/abajo)
    val yaw: Float,    // Rotación horizontal (izquierda/derecha)
    val roll: Float    // Inclinación lateral
) {
    companion object {
        val NEUTRAL = HeadPose(
            pitch = 0f,
            yaw = 0f,
            roll = 0f
        )
    }
}