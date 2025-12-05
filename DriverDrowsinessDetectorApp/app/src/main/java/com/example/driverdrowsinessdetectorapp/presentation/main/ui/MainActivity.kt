package com.example.driverdrowsinessdetectorapp.presentation.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.driverdrowsinessdetectorapp.data.local.preferences.PreferencesManager
import com.example.driverdrowsinessdetectorapp.presentation.main.navigation.NavGraph
import com.example.driverdrowsinessdetectorapp.presentation.main.navigation.Screen
import com.example.driverdrowsinessdetectorapp.ui.theme.DriverDrowsinessDetectorAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DriverDrowsinessDetectorAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    // Determinar pantalla inicial basada en si hay sesión activa
                    LaunchedEffect(Unit) {
                        val isLoggedIn = preferencesManager.isLoggedIn().first()
                        startDestination = if (isLoggedIn) {
                            Screen.Dashboard.route
                        } else {
                            Screen.Login.route
                        }
                    }

                    // Mostrar navegación solo cuando se haya determinado el destino inicial
                    startDestination?.let { destination ->
                        NavGraph(
                            navController = navController,
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}