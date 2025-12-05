package com.example.driverdrowsinessdetectorapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driverdrowsinessdetectorapp.domain.usecase.auth.LoginUseCase
import com.example.driverdrowsinessdetectorapp.domain.usecase.auth.ValidateTokenUseCase
import com.example.driverdrowsinessdetectorapp.presentation.auth.ui.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateTokenUseCase: ValidateTokenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val isLoggedIn = validateTokenUseCase()
            if (isLoggedIn) {
                // Usuario ya tiene sesión activa, redirigir al dashboard
                // Nota: el estado Success se manejará en la navegación
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            loginUseCase(username, password)
                .onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    val errorMessage = when {
                        error.message?.contains("Unable to resolve host") == true -> 
                            "No se puede conectar al servidor. Verifica tu conexión a internet."
                        error.message?.contains("401") == true || 
                        error.message?.contains("Unauthorized") == true -> 
                            "Credenciales incorrectas. Verifica tu usuario y contraseña."
                        error.message?.contains("timeout") == true -> 
                            "Tiempo de espera agotado. Intenta nuevamente."
                        else -> error.message ?: "Error desconocido. Intenta nuevamente."
                    }
                    _uiState.value = LoginUiState.Error(errorMessage)
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}