package com.example.driverdrowsinessdetectorapp.data.remote.interceptor

import com.example.driverdrowsinessdetectorapp.data.local.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Obtener token de manera síncrona (runBlocking es aceptable en interceptors)
        val token = runBlocking {
            preferencesManager.getAuthToken().first()
        }
        
        // Si no hay token, continuar sin modificar el request
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }
        
        // Agregar Authorization header
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(newRequest)
    }
}