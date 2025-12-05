package com.example.driverdrowsinessdetectorapp.domain.repository

import com.example.driverdrowsinessdetectorapp.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User>
    suspend fun getStoredToken(): String?
    suspend fun isLoggedIn(): Boolean
    suspend fun getStoredUser(): User?
}