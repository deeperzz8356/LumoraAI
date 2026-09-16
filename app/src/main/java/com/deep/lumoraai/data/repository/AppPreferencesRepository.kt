package com.deep.lumoraai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
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

    val isDeveloperMode: Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)

    val isDevModeUnlocked: Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)

    val localeCode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
        .map { it[PreferenceKeys.LOCALE_CODE] ?: "en" }

    suspend fun setLocaleCode(code: String) {
        dataStore.edit { it[PreferenceKeys.LOCALE_CODE] = code }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        resetDeveloperSession()
    }

    /**
     * Legacy compatibility no-op. Unlimited credits are never granted by the client.
     */
    suspend fun setUnlimitedCreditsMode(enabled: Boolean) {
        resetDeveloperSession()
    }

    suspend fun unlockDevMode() {
        resetDeveloperSession()
    }

    suspend fun resetDeveloperSession() {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_DEVELOPER_MODE] = false
            prefs[PreferenceKeys.DEV_MODE_UNLOCKED] = false
        }
    }

    suspend fun isDeveloperModeEnabled(): Boolean {
        return false
    }

    /**
     * Legacy compatibility check. Client-side credential based developer access is disabled.
     */
    fun isUnlimitedCreditsLogin(
        email: String,
        password: String,
        adsConfigStore: com.deep.lumoraai.ads.AdsConfigStore,
    ): Boolean {
        return false
    }

    companion object {
        @Volatile
        private var instance: AppPreferencesRepository? = null

        fun getInstance(context: Context): AppPreferencesRepository =
            instance ?: synchronized(this) {
                instance ?: AppPreferencesRepository(context.applicationContext).also { instance = it }
            }
    }
}
