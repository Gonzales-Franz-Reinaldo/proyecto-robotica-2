package com.example.driverdrowsinessdetectorapp.data.remote.api

import com.example.driverdrowsinessdetectorapp.data.remote.dto.request.LoginRequest
import com.example.driverdrowsinessdetectorapp.data.remote.dto.response.LoginResponse
import com.example.driverdrowsinessdetectorapp.data.remote.dto.response.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("/api/v1/auth/logout")
    suspend fun logout()
    
    @GET("/api/v1/users/me")
    suspend fun getCurrentUser(): UserResponse
}