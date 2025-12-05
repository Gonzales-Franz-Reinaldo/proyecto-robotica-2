package com.example.driverdrowsinessdetectorapp.data.local.dao

import androidx.room.*
import com.example.driverdrowsinessdetectorapp.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity): Long
    
    @Update
    suspend fun update(session: SessionEntity)
    
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SessionEntity?
    
    @Query("SELECT * FROM sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?
    
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getSessionsByUser(userId: Int): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT 10")
    fun getRecentSessions(): Flow<List<SessionEntity>>
    
    @Query("UPDATE sessions SET status = :status WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: Long, status: String)
    
    @Delete
    suspend fun delete(session: SessionEntity)
}