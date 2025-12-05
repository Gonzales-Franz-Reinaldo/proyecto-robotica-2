package com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel

@Composable
fun StatusIndicator(
    alertLevel: AlertLevel,
    modifier: Modifier = Modifier
) {
    // Animación de pulsación
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador circular
        Box(
            modifier = Modifier
                .size(16.dp)
                .scale(if (alertLevel != AlertLevel.NORMAL) scale else 1f)
                .background(
                    color = getStatusColor(alertLevel),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Texto de estado
        Text(
            text = getStatusText(alertLevel),
            style = MaterialTheme.typography.bodyMedium,
            color = getStatusColor(alertLevel),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

private fun getStatusColor(alertLevel: AlertLevel): Color {
    return when (alertLevel) {
        AlertLevel.NORMAL -> Color(0xFF4CAF50)   // Verde
        AlertLevel.MEDIUM -> Color(0xFFFFA726)   // Naranja
        AlertLevel.HIGH -> Color(0xFFFF5722)     // Rojo-Naranja
        AlertLevel.CRITICAL -> Color(0xFFD32F2F) // Rojo
    }
}

private fun getStatusText(alertLevel: AlertLevel): String {
    return when (alertLevel) {
        AlertLevel.NORMAL -> "Normal"
        AlertLevel.MEDIUM -> "Advertencia"
        AlertLevel.HIGH -> "Alerta Alta"
        AlertLevel.CRITICAL -> "CRÍTICO"
    }
}