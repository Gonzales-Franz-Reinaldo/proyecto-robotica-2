package com.example.driverdrowsinessdetectorapp.presentation.main.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Monitoring : Screen("monitoring")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}