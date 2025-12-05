package com.example.driverdrowsinessdetectorapp.domain.usecase.auth

import com.example.driverdrowsinessdetectorapp.domain.model.User
import com.example.driverdrowsinessdetectorapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        // Validaciones de negocio
        if (username.isBlank()) {
            return Result.failure(Exception("El usuario no puede estar vacío"))
        }
        
        if (password.length < 4) {
            return Result.failure(Exception("La contraseña debe tener al menos 4 caracteres"))
        }
        
        return authRepository.login(username, password)
    }
}