package com.deep.lumoraai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.data.local.PreferenceKeys
import com.google.firebase.auth.FirebaseAuth
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
        .map { it[PreferenceKeys.IS_DEVELOPER_MODE] ?: false }

    val isDevModeUnlocked: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
        .map { BuildConfig.DEBUG && (it[PreferenceKeys.DEV_MODE_UNLOCKED] ?: false) }

    val localeCode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
        .map { it[PreferenceKeys.LOCALE_CODE] ?: "en" }

    suspend fun setLocaleCode(code: String) {
        dataStore.edit { it[PreferenceKeys.LOCALE_CODE] = code }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_DEVELOPER_MODE] = enabled && BuildConfig.DEBUG
            if (!enabled) {
                prefs[PreferenceKeys.DEV_MODE_UNLOCKED] = false
            }
        }
    }

    /**
     * Activates the unlimited-credits override granted by the remote config
     * credential match. Unlike [setDeveloperMode] this is NOT gated on
     * [BuildConfig.DEBUG] so it works in release builds for authorised testers.
     */
    suspend fun setUnlimitedCreditsMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_DEVELOPER_MODE] = enabled
            if (!enabled) prefs[PreferenceKeys.DEV_MODE_UNLOCKED] = false
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

    suspend fun isDeveloperModeEnabled(): Boolean {
        if (isDeveloperMode.first()) return true

        val email = FirebaseAuth.getInstance().currentUser?.email
            ?.trim()
            ?.lowercase()
            ?: return false
        return BuildConfig.TESTER_EMAILS
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .any { it.lowercase() == email }
    }

    /**
     * Returns true when all three remote-controlled conditions are met:
     *  1. [unlimitedCreditsEnabled] is true in Remote Config
     *  2. The signed-in user's email matches [unlimitedEmail] (case-insensitive)
     *  3. The password used at login matches [unlimitedPassword]
     *
     * Call this immediately after a successful email sign-in, passing the raw
     * credentials the user just typed. The password is NEVER stored — it is only
     * compared in memory at the moment of login and then discarded.
     */
    fun isUnlimitedCreditsLogin(
        email: String,
        password: String,
        adsConfigStore: com.deep.lumoraai.ads.AdsConfigStore,
    ): Boolean {
        val config = adsConfigStore.current
        if (!config.unlimitedCreditsEnabled) return false
        val targetEmail = config.unlimitedEmail.trim()
        val targetPassword = config.unlimitedPassword.trim()
        if (targetEmail.isBlank() || targetPassword.isBlank()) return false
        return email.trim().lowercase() == targetEmail.lowercase() &&
                password == targetPassword
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
