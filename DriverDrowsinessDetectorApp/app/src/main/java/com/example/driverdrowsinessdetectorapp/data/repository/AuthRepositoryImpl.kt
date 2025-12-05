package com.example.driverdrowsinessdetectorapp.data.repository

import android.util.Log
import com.example.driverdrowsinessdetectorapp.data.local.preferences.PreferencesManager
import com.example.driverdrowsinessdetectorapp.data.remote.api.AuthApi
import com.example.driverdrowsinessdetectorapp.data.remote.dto.request.LoginRequest
import com.example.driverdrowsinessdetectorapp.domain.model.User
import com.example.driverdrowsinessdetectorapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            // LOG 1: Datos que se van a enviar
            Log.d("AuthRepository", "=== INICIO LOGIN ===")
            Log.d("AuthRepository", "Username: $username")
            Log.d("AuthRepository", "Password length: ${password.length}")
            
            val loginRequest = LoginRequest(username, password)
            
            // LOG 2: Request preparado
            Log.d("AuthRepository", "Request creado: $loginRequest")
            
            // Realizar llamada a la API
            val response = authApi.login(loginRequest)
            
            // LOG 3: Respuesta recibida
            Log.d("AuthRepository", "=== RESPUESTA EXITOSA ===")
            Log.d("AuthRepository", "Access Token: ${response.accessToken.take(20)}...")
            Log.d("AuthRepository", "User ID: ${response.user.id}")
            Log.d("AuthRepository", "Username: ${response.user.username}")
            Log.d("AuthRepository", "Role: ${response.user.role}")
            
            val user = User(
                id = response.user.id,
                username = response.user.username,
                fullName = response.user.fullName,
                role = response.user.role,
                email = response.user.email,
                active = response.user.active
            )
            
            // Guardar token y datos del usuario
            preferencesManager.saveAuthData(response.accessToken, user)
            
            Log.d("AuthRepository", "Login completado exitosamente")
            Result.success(user)
            
        } catch (e: retrofit2.HttpException) {
            // LOG 4: Error HTTP
            Log.e("AuthRepository", "=== ERROR HTTP ===")
            Log.e("AuthRepository", "Code: ${e.code()}")
            Log.e("AuthRepository", "Message: ${e.message()}")
            
            // Intentar leer el cuerpo del error
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthRepository", "Error body: $errorBody")
            } catch (ex: Exception) {
                Log.e("AuthRepository", "No se pudo leer error body: ${ex.message}")
            }
            
            // Mapear error HTTP a mensaje legible
            val errorMessage = when (e.code()) {
                401 -> "Credenciales incorrectas. Verifica tu usuario y contraseña."
                403 -> "Usuario inactivo. Contacta al administrador."
                429 -> "Cuenta bloqueada. Intenta nuevamente en 15 minutos."
                else -> "Error del servidor: ${e.code()} - ${e.message()}"
            }
            
            Result.failure(Exception(errorMessage))
            
        } catch (e: java.net.UnknownHostException) {
            // LOG 5: Error de conexión
            Log.e("AuthRepository", "=== ERROR DE CONEXIÓN ===")
            Log.e("AuthRepository", "No se puede resolver el host: ${e.message}")
            
            Result.failure(
                Exception("No se puede conectar al servidor. Verifica tu conexión a internet.")
            )
            
        } catch (e: java.net.SocketTimeoutException) {
            // LOG 6: Timeout
            Log.e("AuthRepository", "=== TIMEOUT ===")
            Log.e("AuthRepository", "Timeout: ${e.message}")
            
            Result.failure(
                Exception("Tiempo de espera agotado. Intenta nuevamente.")
            )
            
        } catch (e: Exception) {
            // LOG 7: Otro error
            Log.e("AuthRepository", "=== ERROR DESCONOCIDO ===")
            Log.e("AuthRepository", "Type: ${e.javaClass.simpleName}")
            Log.e("AuthRepository", "Message: ${e.message}")
            Log.e("AuthRepository", "Stack trace:")
            e.printStackTrace()
            
            Result.failure(
                Exception("Error inesperado: ${e.message ?: "Desconocido"}")
            )
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authApi.logout()
            preferencesManager.clearAuthData()
            Result.success(Unit)
        } catch (e: Exception) {
            // Limpiar datos locales aunque falle la API
            preferencesManager.clearAuthData()
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = authApi.getCurrentUser()
            val user = User(
                id = response.id,
                username = response.username,
                fullName = response.fullName,
                role = response.role,
                email = response.email,
                active = response.active
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStoredToken(): String? {
        return preferencesManager.getAuthToken().first()
    }

    override suspend fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn().first()
    }

    override suspend fun getStoredUser(): User? {
        return preferencesManager.getUserData().first()
    }
}