package com.example.driverdrowsinessdetectorapp.domain.repository

import com.example.driverdrowsinessdetectorapp.data.local.entity.MetricsEntity
import kotlinx.coroutines.flow.Flow

interface MetricsRepository {
    suspend fun saveMetrics(metrics: MetricsEntity): Long
    suspend fun saveMetricsBatch(metrics: List<MetricsEntity>)
    fun getMetricsBySession(sessionId: Long): Flow<List<MetricsEntity>>
    fun getCriticalAlertsBySession(sessionId: Long): Flow<List<MetricsEntity>>
    suspend fun deleteOlderThan(timestamp: Long)
    suspend fun getCriticalAlertsCount(sessionId: Long): Int
}