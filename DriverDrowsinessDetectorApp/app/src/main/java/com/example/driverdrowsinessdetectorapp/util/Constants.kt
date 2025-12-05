package com.example.driverdrowsinessdetectorapp.util

import com.example.driverdrowsinessdetectorapp.BuildConfig

/**
 * Constantes globales de la aplicación.
 * 
 * Las URLs y configuraciones de red se obtienen de BuildConfig,
 * que se genera desde build.gradle.kts.
 * 
 * Para cambiar la IP del servidor:
 * 1. Abre app/build.gradle.kts
 * 2. Modifica API_BASE_URL en buildConfigField
 * 3. Rebuild el proyecto (Build > Rebuild Project)
 * 
 * @author Sistema de Detección de Somnolencia
 * @version 2.0
 */
object Constants {
    
    // =========================================
    // 🔧 CONFIGURACIÓN DE RED (desde BuildConfig)
    // =========================================
    
    /**
     * URL base del backend FastAPI.
     * 
     * Configurada en: app/build.gradle.kts
     * - Debug: URL de desarrollo (red local)
     * - Release: URL de producción
     */
    val BASE_URL: String = BuildConfig.API_BASE_URL
    
    /**
     * Timeouts de conexión (en segundos).
     */
    val CONNECT_TIMEOUT: Long = BuildConfig.CONNECT_TIMEOUT
    val READ_TIMEOUT: Long = BuildConfig.READ_TIMEOUT
    val WRITE_TIMEOUT: Long = BuildConfig.WRITE_TIMEOUT
    
    // =========================================
    // 💾 DATASTORE KEYS
    // =========================================
    const val PREFS_NAME = "driver_drowsiness_prefs"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_FULL_NAME = "full_name"
    const val KEY_ROLE = "role"
    
    // =========================================
    // 📊 INFORMACIÓN DE LA APP
    // =========================================
    val APP_VERSION: String = BuildConfig.VERSION_NAME
    val APP_VERSION_CODE: Int = BuildConfig.VERSION_CODE
    val IS_DEBUG: Boolean = BuildConfig.DEBUG
}