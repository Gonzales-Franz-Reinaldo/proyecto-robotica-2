package com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driverdrowsinessdetectorapp.domain.model.AlertLevel
import com.example.driverdrowsinessdetectorapp.domain.model.MetricasSomnolencia

@Composable
fun MetricsDisplay(
    metrics: MetricasSomnolencia,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.75f) 
        ),
        shape = RoundedCornerShape(12.dp) 
    ) {
        Column(
            modifier = Modifier.padding(12.dp) 
        ) {
            // ========== TÍTULO ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Análisis de Puntos",
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Badge de estado
                StatusBadge(alertLevel = metrics.alertLevel)
            }

            Spacer(modifier = Modifier.height(8.dp)) 

            // ========== MÉTRICAS PRINCIPALES (EAR/MAR) ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompactMetricItem(
                    label = "EAR",
                    value = String.format("%.2f", metrics.ear),
                    isGood = metrics.ear > 0.2f
                )

                CompactMetricItem(
                    label = "MAR",
                    value = String.format("%.2f", metrics.mar),
                    isGood = metrics.mar < 0.6f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 0.5.dp)
            
            Spacer(modifier = Modifier.height(8.dp))

            // ========== 5 CONTADORES DE DETECCIÓN ==========
            
            // FILA 1: Microsueño + Cabeceo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactCounterItem(
                    icon = "😴",
                    label = "Microsueños",
                    count = metrics.microsleepCount,
                    color = Color(0xFFE63946),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                CompactCounterItem(
                    icon = "🙇",
                    label = "Cabeceos",
                    count = metrics.noddingCount,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // FILA 2: Parpadeo + Bostezo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactCounterItem(
                    icon = "👁️",
                    label = "Parpadeos",
                    count = metrics.blinkCount,
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                CompactCounterItem(
                    icon = "🥱",
                    label = "Bostezos",
                    count = metrics.yawnCount,
                    color = Color(0xFFFFA726),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // FILA 3: Frotamiento Izquierda + Derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactCounterItem(
                    icon = "👈",
                    label = "Frote Izq",
                    count = metrics.eyeRubFirstHandCount,
                    color = Color(0xFF95E1D3),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                CompactCounterItem(
                    icon = "👉",
                    label = "Frote Der",
                    count = metrics.eyeRubSecondHandCount,
                    color = Color(0xFF95E1D3),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ========== MÉTRICA PRINCIPAL COMPACTA (EAR/MAR) ==========
@Composable
private fun CompactMetricItem(
    label: String,
    value: String,
    isGood: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp, 
            color = Color.White.copy(alpha = 0.6f)
        )
        
        Text(
            text = value,
            fontSize = 16.sp, 
            fontWeight = FontWeight.Bold,
            color = if (isGood) Color(0xFF4CAF50) else Color(0xFFFF5252)
        )
    }
}

// ========== CONTADOR COMPACTO ==========
@Composable
private fun CompactCounterItem(
    icon: String,
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = if (count > 0) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 6.dp, horizontal = 8.dp), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 18.sp 
        )
        
        Text(
            text = count.toString(),
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold,
            color = if (count > 0) color else Color.Gray
        )
        
        Text(
            text = label,
            fontSize = 9.sp, 
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

// ========== BADGE DE ESTADO ==========
@Composable
private fun StatusBadge(alertLevel: AlertLevel) {
    val (color, text) = when (alertLevel) {
        AlertLevel.NORMAL -> Color(0xFF4CAF50) to "OK"
        AlertLevel.MEDIUM -> Color(0xFFFFA726) to "⚠️"
        AlertLevel.HIGH -> Color(0xFFFF5722) to "🚨"
        AlertLevel.CRITICAL -> Color(0xFFE63946) to "🔴"
    }

    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}