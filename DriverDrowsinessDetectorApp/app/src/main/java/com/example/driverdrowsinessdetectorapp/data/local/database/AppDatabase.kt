package com.example.driverdrowsinessdetectorapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.driverdrowsinessdetectorapp.data.local.converter.Converters
import com.example.driverdrowsinessdetectorapp.data.local.dao.*
import com.example.driverdrowsinessdetectorapp.data.local.entity.*

@Database(
    entities = [
        SessionEntity::class,
        AlertEntity::class,
        LocationEntity::class,
        MetricsEntity::class 
    ],
    version = 2, // ← INCREMENTAR VERSIÓN
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun alertDao(): AlertDao
    abstract fun locationDao(): LocationDao
    abstract fun metricsDao(): MetricsDao // ← AGREGAR
    
    companion object {
        const val DATABASE_NAME = "drowsiness_detector.db"
    }
}