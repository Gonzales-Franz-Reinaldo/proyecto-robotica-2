package com.example.driverdrowsinessdetectorapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: Int,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String, // ACTIVE, PAUSED, COMPLETED
    val durationMs: Long = 0,
    val totalAlerts: Int = 0,
    val criticalAlerts: Int = 0
)