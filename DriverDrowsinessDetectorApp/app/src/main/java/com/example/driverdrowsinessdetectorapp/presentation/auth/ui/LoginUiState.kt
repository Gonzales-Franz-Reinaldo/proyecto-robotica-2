package com.example.driverdrowsinessdetectorapp.presentation.auth.ui

import com.example.driverdrowsinessdetectorapp.domain.model.User

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}