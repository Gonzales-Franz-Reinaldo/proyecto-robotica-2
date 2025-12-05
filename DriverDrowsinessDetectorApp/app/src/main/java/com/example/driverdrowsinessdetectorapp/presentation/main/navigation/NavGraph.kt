package com.example.driverdrowsinessdetectorapp.presentation.main.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.driverdrowsinessdetectorapp.presentation.auth.ui.LoginScreen
import com.example.driverdrowsinessdetectorapp.presentation.dashboard.ui.DashboardScreen
import com.example.driverdrowsinessdetectorapp.presentation.monitoring.ui.MonitoringScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onStartMonitoring = {
                    navController.navigate(Screen.Monitoring.route)
                }
            )
        }

        //  RUTA: Monitoring
        composable(Screen.Monitoring.route) {
            MonitoringScreen(
                onNavigateBack = {  // ← CORRECCIÓN AQUÍ
                    navController.popBackStack()
                }
            )
        }
    }
}