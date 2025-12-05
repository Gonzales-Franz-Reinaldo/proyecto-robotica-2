package com.example.driverdrowsinessdetectorapp.data.local.dao

import androidx.room.*
import com.example.driverdrowsinessdetectorapp.data.local.entity.MetricsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO: Operaciones CRUD para métricas
 */
@Dao
interface MetricsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metrics: MetricsEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metrics: List<MetricsEntity>)
    
    @Query("SELECT * FROM metrics WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getMetricsBySession(sessionId: Long): Flow<List<MetricsEntity>>
    
    @Query("SELECT * FROM metrics WHERE sessionId = :sessionId AND alertLevel IN ('HIGH', 'CRITICAL') ORDER BY timestamp DESC")
    fun getCriticalAlertsBySession(sessionId: Long): Flow<List<MetricsEntity>>
    
    @Query("SELECT * FROM metrics WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMetricsByTimeRange(startTime: Long, endTime: Long): Flow<List<MetricsEntity>>
    
    @Query("DELETE FROM metrics WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
    
    @Query("DELETE FROM metrics WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM metrics WHERE sessionId = :sessionId AND alertLevel = 'CRITICAL'")
    suspend fun getCriticalAlertsCount(sessionId: Long): Int
}