package com.example.driverdrowsinessdetectorapp.domain.model

data class User(
    val id: Int,
    val username: String,
    val fullName: String,
    val role: String,
    val email: String?,
    val active: Boolean
)
