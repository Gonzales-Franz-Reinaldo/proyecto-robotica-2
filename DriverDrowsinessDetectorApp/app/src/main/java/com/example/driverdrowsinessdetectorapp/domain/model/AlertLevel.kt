package com.example.driverdrowsinessdetectorapp.domain.model

/**
 * Niveles de alerta de somnolencia
 */
enum class AlertLevel {
    /**
     * Estado normal - Sin signos de somnolencia
     */
    NORMAL,
    
    /**
     * Advertencia - Signos leves de somnolencia detectados
     */
    MEDIUM,
    
    /**
     * Crítico - Somnolencia severa detectada, requiere acción inmediata
     */
    HIGH,
    
    /**
     * Crítico - Somnolencia severa detectada, requiere acción inmediata
     */
    CRITICAL
}