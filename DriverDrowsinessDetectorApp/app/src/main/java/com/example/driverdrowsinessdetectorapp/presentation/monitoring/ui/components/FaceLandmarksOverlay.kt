package com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Overlay para dibujar puntos faciales de MediaPipe (debugging)
 * Por ahora solo dibuja un grid de ejemplo
 */
@Composable
fun FaceLandmarksOverlay(
    modifier: Modifier = Modifier,
    faceLandmarks: List<Offset> = emptyList() // TODO: Recibir puntos reales de MediaPipe
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Dibujar mesh facial (placeholder)
        // En producción, aquí se dibujarían los 478 puntos de MediaPipe
        
        // Ejemplo: dibujar círculo central
        drawCircle(
            color = Color.Green,
            radius = 5f,
            center = Offset(size.width / 2, size.height / 2),
            style = Stroke(width = 2f)
        )
        
        // TODO: Dibujar puntos faciales reales cuando tengamos MediaPipe integrado
        faceLandmarks.forEach { point ->
            drawCircle(
                color = Color.Cyan,
                radius = 3f,
                center = point
            )
        }
    }
}