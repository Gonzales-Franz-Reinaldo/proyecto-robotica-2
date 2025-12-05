package com.example.driverdrowsinessdetectorapp.data.local.dao

import androidx.room.*
import com.example.driverdrowsinessdetectorapp.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)
    
    @Query("SELECT * FROM locations WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getLocationsBySession(sessionId: Long): Flow<List<LocationEntity>>
    
    @Query("SELECT * FROM locations WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLocation(sessionId: Long): LocationEntity?
    
    @Query("DELETE FROM locations WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
    
    @Delete
    suspend fun delete(location: LocationEntity)
}