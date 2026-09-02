package com.deep.lumoraai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.data.local.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences"
)

class AppPreferencesRepository private constructor(context: Context) {

    private val dataStore = context.appPreferencesDataStore

    val isDeveloperMode: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
        .map { BuildConfig.DEBUG && (it[PreferenceKeys.IS_DEVELOPER_MODE] ?: false) }

    val isDevModeUnlocked: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
        .map { BuildConfig.DEBUG && (it[PreferenceKeys.DEV_MODE_UNLOCKED] ?: false) }

    suspend fun setDeveloperMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_DEVELOPER_MODE] = enabled && BuildConfig.DEBUG
        }
    }

    suspend fun unlockDevMode() {
        if (!BuildConfig.DEBUG) return
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.DEV_MODE_UNLOCKED] = true
        }
    }

    suspend fun resetDeveloperSession() {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_DEVELOPER_MODE] = false
            prefs[PreferenceKeys.DEV_MODE_UNLOCKED] = false
        }
    }

    suspend fun isDeveloperModeEnabled(): Boolean = isDeveloperMode.first()

    companion object {
        @Volatile
        private var instance: AppPreferencesRepository? = null

        fun getInstance(context: Context): AppPreferencesRepository =
            instance ?: synchronized(this) {
                instance ?: AppPreferencesRepository(context.applicationContext).also { instance = it }
            }
    }
}
