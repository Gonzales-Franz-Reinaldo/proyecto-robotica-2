package com.example.driverdrowsinessdetectorapp.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.driverdrowsinessdetectorapp.domain.model.User
import com.example.driverdrowsinessdetectorapp.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFS_NAME
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys
    private val KEY_TOKEN = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
    private val KEY_USER_ID = intPreferencesKey(Constants.KEY_USER_ID)
    private val KEY_USERNAME = stringPreferencesKey(Constants.KEY_USERNAME)
    private val KEY_FULL_NAME = stringPreferencesKey(Constants.KEY_FULL_NAME)
    private val KEY_ROLE = stringPreferencesKey(Constants.KEY_ROLE)

    // Save auth data
    suspend fun saveAuthData(token: String, user: User) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USERNAME] = user.username
            prefs[KEY_FULL_NAME] = user.fullName
            prefs[KEY_ROLE] = user.role
        }
    }

    // Get auth token
    fun getAuthToken(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    // Get user ID
    fun getUserId(): Flow<Int?> = dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    // Get full name
    fun getFullName(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_FULL_NAME]
    }

    // Get role
    fun getRole(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_ROLE]
    }

    // Get complete user data
    fun getUserData(): Flow<User?> = dataStore.data.map { prefs ->
        val userId = prefs[KEY_USER_ID]
        val username = prefs[KEY_USERNAME]
        val fullName = prefs[KEY_FULL_NAME]
        val role = prefs[KEY_ROLE]

        if (userId != null && username != null && fullName != null && role != null) {
            User(
                id = userId,
                username = username,
                fullName = fullName,
                role = role,
                email = null,
                active = true
            )
        } else {
            null
        }
    }

    // Clear all auth data
    suspend fun clearAuthData() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[KEY_TOKEN].isNullOrBlank()
    }
}