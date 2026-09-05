package com.deep.lumoraai.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deep.lumoraai.data.repository.GenerationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.creditCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "credit_cache")

/**
 * App-wide, persistent, optimistic credit balance — the single source the UI
 * header reads on every screen.
 *
 * Design (server is always the authority):
 *  - PERSISTENT: the last-known balance is cached in DataStore per user, so on a
 *    cold start the header shows the cached number instantly (no 0 → flash → real).
 *  - OPTIMISTIC: [applyOptimistic] adjusts the visible balance the moment a user
 *    acts (e.g. -1 on generate) so the UI feels instant.
 *  - RECONCILED: [set]/[refresh] overwrite with the authoritative server balance
 *    on every completion (success OR failure), so any optimistic guess is
 *    corrected. The local value is display-only and is NEVER used to gate
 *    spending — the generation gate always reads the server balance directly.
 *  - PER-USER: the cache is namespaced by uid so switching accounts never shows
 *    another user's balance.
 *
 * This is intentionally lightweight (no polling, no offline write-queue): at ~1k
 * DAU we refresh on the events that matter (app foreground, screen entry,
 * post-action), which keeps the header fresh without hammering a free-tier backend.
 */
object CreditBalanceStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = GenerationRepository()
    private val mutex = Mutex()

    private lateinit var appContext: Context
    private var initialized = false

    private val _balance = MutableStateFlow<Int?>(null)
    /** Latest known balance for display; null until the first load. */
    val balance: StateFlow<Int?> = _balance.asStateFlow()

    private val keyBalance = intPreferencesKey("credit_balance")
    private val keyOwnerUid = stringPreferencesKey("credit_owner_uid")

    private val currentUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    /**
     * Seed the in-memory value from the persisted cache (call once, e.g. from
     * Application.onCreate). Only restores the cached balance if it belongs to
     * the currently signed-in user.
     */
    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext
        scope.launch {
            val prefs = appContext.creditCacheDataStore.data.first()
            val cachedUid = prefs[keyOwnerUid]
            val cached = prefs[keyBalance]
            if (cached != null && cachedUid == currentUid) {
                _balance.value = cached
            }
        }
    }

    /** Overwrite with an authoritative value (server response) and persist it. */
    fun set(value: Int?) {
        if (value == null) return
        _balance.value = value
        persist(value)
    }

    /**
     * Apply an optimistic delta immediately (e.g. -1 when a generation starts)
     * for instant UI feedback. Clamped at 0. The server value replaces this on
     * the next [set]/[refresh], so a wrong guess self-corrects. No-op if we have
     * no known balance yet (nothing to adjust from).
     */
    fun applyOptimistic(delta: Int) {
        val current = _balance.value ?: return
        val next = (current + delta).coerceAtLeast(0)
        _balance.value = next
        persist(next)
    }

    /** Fetch the authoritative balance from the backend and publish/persist it. */
    fun refresh() {
        scope.launch {
            repository.getCredits().getOrNull()?.let { set(it) }
        }
    }

    /** Suspending refresh for callers that want to await the reconciled value. */
    suspend fun refreshAndGet(): Int? {
        val credits = repository.getCredits().getOrNull()
        if (credits != null) set(credits)
        return credits
    }

    /** Clear the cache on sign-out so the next user never sees a stale balance. */
    fun clear() {
        _balance.value = null
        if (!initialized) return
        scope.launch {
            appContext.creditCacheDataStore.edit { it.clear() }
        }
    }

    private fun persist(value: Int) {
        if (!initialized) return
        val uid = currentUid
        scope.launch {
            mutex.withLock {
                appContext.creditCacheDataStore.edit { prefs ->
                    prefs[keyBalance] = value
                    prefs[keyOwnerUid] = uid
                }
            }
        }
    }
}
