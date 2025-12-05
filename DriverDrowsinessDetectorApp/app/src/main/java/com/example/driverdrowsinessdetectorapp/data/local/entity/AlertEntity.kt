package com.example.driverdrowsinessdetectorapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alerts",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val sessionId: Long,
    val timestamp: Long,
    val alertType: String, // MICROSLEEP, YAWN, NODDING, EYE_RUB
    val alertLevel: String, // NORMAL, MEDIUM, HIGH, CRITICAL
    val latitude: Double? = null,
    val longitude: Double? = null
)