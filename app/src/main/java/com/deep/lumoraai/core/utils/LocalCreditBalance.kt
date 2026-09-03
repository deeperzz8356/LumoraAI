package com.deep.lumoraai.core.utils

import android.content.Context

object LocalCreditBalance {
    private const val PREFS = "lumora_credit_rewards"
    private const val KEY_LOCAL_REWARD_BALANCE = "local_reward_balance"

    fun get(context: Context): Int =
        prefs(context).getInt(KEY_LOCAL_REWARD_BALANCE, 0).coerceAtLeast(0)

    fun add(context: Context, amount: Int): Int {
        if (amount <= 0) return get(context)
        val next = get(context) + amount
        prefs(context).edit().putInt(KEY_LOCAL_REWARD_BALANCE, next).apply()
        return next
    }

    fun maxWith(context: Context, backendCredits: Int?): Int =
        maxOf(backendCredits ?: 0, get(context))

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
