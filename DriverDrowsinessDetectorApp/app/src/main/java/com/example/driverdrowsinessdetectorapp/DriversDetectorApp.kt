package com.example.driverdrowsinessdetectorapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DriversDetectorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicialización global si es necesaria
    }
}