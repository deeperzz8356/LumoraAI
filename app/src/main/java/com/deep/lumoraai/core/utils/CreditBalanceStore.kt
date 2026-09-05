package com.deep.lumoraai.core.utils

import com.deep.lumoraai.data.repository.GenerationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * App-wide observable credit balance — the single source of truth the UI header
 * reads from every screen.
 *
 * The header used to show a stale value because each screen fetched credits once
 * at ViewModel init and never refreshed after a generation deducted credits or a
 * reward added them. Screens now observe [balance] and any action that changes
 * the balance calls [refresh] (or [set] with the authoritative server value),
 * so the displayed number updates in real time everywhere.
 */
object CreditBalanceStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = GenerationRepository()

    private val _balance = MutableStateFlow<Int?>(null)
    /** Latest known balance; null until the first successful fetch. */
    val balance: StateFlow<Int?> = _balance.asStateFlow()

    /** Overwrite the balance with an authoritative value (e.g. a server response). */
    fun set(value: Int?) {
        if (value != null) _balance.value = value
    }

    /** Fetch the authoritative balance from the backend and publish it. */
    fun refresh() {
        scope.launch {
            repository.getCredits().getOrNull()?.let { _balance.value = it }
        }
    }

    /** Suspending refresh for callers that want to await the new value. */
    suspend fun refreshAndGet(): Int? {
        val credits = repository.getCredits().getOrNull()
        if (credits != null) _balance.value = credits
        return credits
    }
}
