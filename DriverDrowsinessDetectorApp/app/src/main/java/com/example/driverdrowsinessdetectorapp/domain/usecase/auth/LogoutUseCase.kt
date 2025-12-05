package com.example.driverdrowsinessdetectorapp.domain.usecase.auth

import com.example.driverdrowsinessdetectorapp.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}