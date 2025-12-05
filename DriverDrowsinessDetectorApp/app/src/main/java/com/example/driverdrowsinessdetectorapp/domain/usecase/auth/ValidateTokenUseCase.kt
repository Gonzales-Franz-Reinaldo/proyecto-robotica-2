package com.example.driverdrowsinessdetectorapp.domain.usecase.auth

import com.example.driverdrowsinessdetectorapp.domain.repository.AuthRepository
import javax.inject.Inject

class ValidateTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
}