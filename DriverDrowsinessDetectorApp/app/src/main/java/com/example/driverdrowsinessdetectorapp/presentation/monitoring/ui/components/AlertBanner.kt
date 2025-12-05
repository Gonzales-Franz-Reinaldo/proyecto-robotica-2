package com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.domain.model.AlertType

@Composable
fun AlertBanner(
    alertLevel: AlertLevel,
    alertType: AlertType?,
    modifier: Modifier = Modifier
) {
    val isVisible = alertLevel != AlertLevel.NORMAL

    // Animación de parpadeo (solo CRITICAL)
    val infiniteTransition = rememberInfiniteTransition(label = "alert_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (alertLevel == AlertLevel.CRITICAL) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (alertLevel == AlertLevel.CRITICAL) 500 else 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (alertLevel == AlertLevel.CRITICAL) alpha else 1f)
                .background(
                    color = getAlertColor(alertLevel),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alerta",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = getAlertTitle(alertLevel),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    alertType?.let {
                        Text(
                            text = getDetailedAlertMessage(it, alertLevel),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                        
                        //  MENSAJE DE ACCIÓN
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getRecommendedAction(alertLevel, it),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun getAlertColor(alertLevel: AlertLevel): Color {
    return when (alertLevel) {
        AlertLevel.NORMAL -> Color.Transparent
        AlertLevel.MEDIUM -> Color(0xFFFFA726) // Naranja
        AlertLevel.HIGH -> Color(0xFFFF5722)   // Rojo-Naranja
        AlertLevel.CRITICAL -> Color(0xFFD32F2F) // Rojo intenso
    }
}

private fun getAlertTitle(alertLevel: AlertLevel): String {
    return when (alertLevel) {
        AlertLevel.NORMAL -> ""
        AlertLevel.MEDIUM -> "⚠️ Advertencia"
        AlertLevel.HIGH -> "🚨 Atención"
        AlertLevel.CRITICAL -> "🔴 PELIGRO"
    }
}

// ========== MENSAJES DETALLADOS (CORREGIDOS) ==========
private fun getDetailedAlertMessage(alertType: AlertType, alertLevel: AlertLevel): String {
    return when (alertType) {
        AlertType.MICROSLEEP -> when (alertLevel) {
            AlertLevel.CRITICAL -> "🔴 MICROSUEÑO: Sus ojos estuvieron cerrados más de 2 segundos"
            else -> "Microsueño detectado"
        }
        
        AlertType.HEAD_NODDING -> when (alertLevel) {
            AlertLevel.CRITICAL -> "🔴 CABECEO: Su cabeza se inclinó durante más de 3 segundos"
            else -> "Cabeceo detectado"
        }
        
        AlertType.YAWNING -> when (alertLevel) {
            AlertLevel.HIGH -> "⚠️ ADVERTENCIA: Ha bostezado más de 3 veces en los últimos 3 minutos"
            AlertLevel.MEDIUM -> "Ha bostezado varias veces - Señal de fatiga"
            else -> "Bostezo detectado"
        }
        
        AlertType.EXCESSIVE_BLINKING -> when (alertLevel) { 
            AlertLevel.MEDIUM -> "⚠️ ADVERTENCIA: Ha parpadeado más de 20 veces en el último minuto"
            else -> "Parpadeo excesivo detectado"
        }
        
        AlertType.EYE_RUB -> when (alertLevel) {
            AlertLevel.MEDIUM -> "⚠️ ADVERTENCIA: Se ha frotado los ojos más de 3 veces en los últimos 5 minutos"
            else -> "Frotamiento de ojos detectado"
        }
    }
}

// ========== ACCIONES RECOMENDADAS (CORREGIDAS) ==========
private fun getRecommendedAction(alertLevel: AlertLevel, alertType: AlertType?): String {
    return when (alertLevel) {
        AlertLevel.NORMAL -> ""
        
        AlertLevel.MEDIUM -> when (alertType) {
            AlertType.EXCESSIVE_BLINKING -> "💡 Parpadeo excesivo. Descanse la vista y manténgase hidratado"
            AlertType.EYE_RUB -> "💡 Señal de cansancio visual. Busque un lugar seguro para descansar"
            else -> "💡 Muestra signos de fatiga leve. Manténgase alerta"
        }
        
        AlertLevel.HIGH -> "⚠️ Fatiga acumulada. Busque el próximo área de descanso (máximo 15 minutos)"
        
        AlertLevel.CRITICAL -> "🛑 PELIGRO: Detenga el vehículo de forma segura INMEDIATAMENTE"
    }
}