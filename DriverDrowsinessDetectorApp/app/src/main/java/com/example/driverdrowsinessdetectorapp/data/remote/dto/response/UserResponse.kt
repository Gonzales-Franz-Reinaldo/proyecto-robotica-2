package com.example.driverdrowsinessdetectorapp.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id_usuario")
    val id: Int,
    
    @SerializedName("usuario")
    val username: String,
    
    @SerializedName("nombre_completo")
    val fullName: String,
    
    @SerializedName("rol")
    val role: String,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("activo")
    val active: Boolean
)
