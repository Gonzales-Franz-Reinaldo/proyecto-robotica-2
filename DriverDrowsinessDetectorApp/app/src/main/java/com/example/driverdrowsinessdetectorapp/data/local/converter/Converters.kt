package com.example.driverdrowsinessdetectorapp.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Converters para Room
 * Permite almacenar List<Long> como JSON
 */
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }
}