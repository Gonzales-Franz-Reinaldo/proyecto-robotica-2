package com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PermissionRequestDialog(
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permisos Requeridos",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Esta aplicación necesita acceso a la cámara y ubicación para funcionar correctamente.\n\n" +
                      "• Cámara: Para detectar somnolencia\n" +
                      "• Ubicación: Para registrar rutas"
            )
        },
        confirmButton = {
            Button(onClick = onRequestPermissions) {
                Text("Otorgar Permisos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}