package com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.presentation.monitoring.MonitoringViewModel
import com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui.components.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MonitoringScreen(
    onNavigateBack: () -> Unit,
    viewModel: MonitoringViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMetrics by viewModel.currentMetrics.collectAsState()

    // Permisos necesarios
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    // Solicitar permisos al entrar
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        } else {
            // Iniciar viaje automáticamente cuando hay permisos
            viewModel.startTrip()
        }
    }

    // Mostrar diálogo si no hay permisos
    if (!permissionsState.allPermissionsGranted) {
        PermissionRequestDialog(
            onRequestPermissions = {
                permissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = onNavigateBack
        )
        return
    }

    // UI Principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // =========================================
        // CÁMARA
        // =========================================
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onFrameCaptured = { bitmap ->
                viewModel.processFrame(bitmap)
            }
        )

        // =========================================
        // UI OVERLAY
        // =========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ========== HEADER ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón volver
                IconButton(
                    onClick = {
                        viewModel.stopTrip()
                        onNavigateBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                // Título
                Text(
                    text = "Monitoreo en Vivo",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                // GPS Indicator
                GPSIndicator(isActive = true)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ========== SUBTÍTULO ==========
            Text(
                text = "Procesamiento en tiempo real con IA",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========== TIMER ==========
            if (uiState is MonitoringUiState.Active) {
                TimerDisplay(
                    formattedTime = (uiState as MonitoringUiState.Active).duration
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== STATUS INDICATOR ==========
            currentMetrics?.let { metrics ->
                StatusIndicator(
                    alertLevel = metrics.alertLevel
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ========== ALERT BANNER ==========
            currentMetrics?.let { metrics ->
                if (metrics.alertLevel != AlertLevel.NORMAL) {
                    AlertBanner(
                        alertLevel = metrics.alertLevel,
                        alertType = metrics.alertType
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ========== METRICS DISPLAY ==========
            if (uiState is MonitoringUiState.Active) {
                currentMetrics?.let { metrics ->
                    MetricsDisplay(metrics = metrics)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== CONTROLES ==========
            SessionControls(
                isPaused = uiState is MonitoringUiState.Paused,
                onPauseResume = {
                    when (uiState) {
                        is MonitoringUiState.Active -> viewModel.pauseTrip()
                        is MonitoringUiState.Paused -> viewModel.resumeTrip()
                        else -> {}
                    }
                },
                onStop = {
                    viewModel.stopTrip()
                    onNavigateBack()
                }
            )
        }

        // ========== LOADING OVERLAY ==========
        if (uiState is MonitoringUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}