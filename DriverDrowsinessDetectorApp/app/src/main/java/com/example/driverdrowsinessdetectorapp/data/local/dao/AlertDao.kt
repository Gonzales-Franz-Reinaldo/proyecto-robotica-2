package com.example.driverdrowsinessdetectorapp.data.local.dao

import androidx.room.*
import com.example.driverdrowsinessdetectorapp.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertEntity>)
    
    @Query("SELECT * FROM alerts WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getAlertsBySession(sessionId: Long): Flow<List<AlertEntity>>
    
    @Query("SELECT * FROM alerts WHERE sessionId = :sessionId AND alertLevel = 'CRITICAL' ORDER BY timestamp DESC")
    fun getCriticalAlertsBySession(sessionId: Long): Flow<List<AlertEntity>>
    
    @Query("SELECT COUNT(*) FROM alerts WHERE sessionId = :sessionId AND alertLevel = :alertLevel")
    suspend fun getAlertCountByLevel(sessionId: Long, alertLevel: String): Int
    
    @Query("DELETE FROM alerts WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
    
    @Delete
    suspend fun delete(alert: AlertEntity)
}