package com.example.driverdrowsinessdetectorapp.domain.model

/**
 * Tipos de alerta de somnolencia
 */
enum class AlertType {
    /**
     * Microsueño - Ojos cerrados >2.5 segundos
     */
    MICROSLEEP,

    /**
     * Bostezo prolongado
     */
    YAWNING, // ← MANTENER SOLO ESTE

    /**
     * Cabeceo - Cabeza inclinada >1.5 segundos
     */
    HEAD_NODDING,

    /**
     * Frotamiento de ojos
     */
    EYE_RUB,

    /**
     * Parpadeo excesivo
     */
    EXCESSIVE_BLINKING
}