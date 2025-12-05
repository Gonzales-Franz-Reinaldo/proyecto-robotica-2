package com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui

import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.domain.model.HeadPose

sealed class MonitoringUiState {
    /**
     * Estado inactivo - Sin sesión iniciada
     */
    data object Idle : MonitoringUiState()
    
    /**
     * Estado de inicio - Inicializando cámara y permisos
     */
    data object Starting : MonitoringUiState()
    
    /**
     * Estado de carga
     */
    data object Loading : MonitoringUiState()
    
    /**
     * Sesión activa - Monitoreo en progreso
     */
    data class Active(
        val sessionId: Long,
        val duration: String,
        val currentEAR: Float,
        val currentMAR: Float,
        val headPose: HeadPose,
        val alertLevel: AlertLevel,
        val gpsEnabled: Boolean,
        val isProcessing: Boolean
    ) : MonitoringUiState()
    
    /**
     * Sesión pausada
     */
    data class Paused(
        val sessionId: Long,
        val duration: String
    ) : MonitoringUiState()
    
    /**
     * Error en la sesión
     */
    data class Error(val message: String) : MonitoringUiState()
}