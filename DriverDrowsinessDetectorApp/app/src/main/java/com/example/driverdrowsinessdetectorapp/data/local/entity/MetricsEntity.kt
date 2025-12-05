package com.example.driverdrowsinessdetectorapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.driverdrowsinessdetectorapp.data.local.converter.Converters

/**
 * Entidad Room: Métricas de Somnolencia
 * 
 * Equivalente al CSV generado en: reports/main.py
 */
@Entity(tableName = "metrics")
@TypeConverters(Converters::class)
data class MetricsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Timestamp
    val timestamp: Long,
    val sessionId: Long, // FK a SessionEntity
    
    // Métricas básicas
    val ear: Float,
    val mar: Float,
    val headPitch: Float,
    val headYaw: Float,
    val headRoll: Float,
    
    // Microsueño
    val isMicrosleep: Boolean,
    val microsleepCount: Int,
    val microsleepDurations: List<Long>,
    
    // Bostezo
    val isYawning: Boolean,
    val yawnCount: Int,
    val yawnDurations: List<Long>,
    
    // Cabeceo
    val isNodding: Boolean,
    val noddingCount: Int,
    val noddingDurations: List<Long>,
    
    // Frotamiento primera mano
    val eyeRubFirstHandDetected: Boolean,
    val eyeRubFirstHandCount: Int,
    val eyeRubFirstHandDurations: List<Long>,
    
    // Frotamiento segunda mano
    val eyeRubSecondHandDetected: Boolean,
    val eyeRubSecondHandCount: Int,
    val eyeRubSecondHandDurations: List<Long>,
    
    // Nivel de alerta
    val alertLevel: String, // NORMAL, MEDIUM, HIGH, CRITICAL
    val alertType: String? // MICROSLEEP, YAWNING, HEAD_NODDING, EYE_RUB
)