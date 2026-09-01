package com.deep.lumoraai.core.utils

import android.content.Context
import android.os.Bundle
import com.deep.lumoraai.data.model.HistoryModel
import com.google.firebase.analytics.FirebaseAnalytics
import java.time.Instant
import java.util.UUID

object HistoryFeedbackReporter {
    private const val PREFS_NAME = "history_feedback"
    private const val KEY_RECORDS = "records"
    private const val EVENT_NAME = "history_media_feedback"

    fun submit(context: Context, item: HistoryModel, reason: String) {
        val appContext = context.applicationContext
        store(appContext, item, reason)
        FirebaseAnalytics.getInstance(appContext).logEvent(
            EVENT_NAME,
            Bundle().apply {
                putString("feedback_id", UUID.randomUUID().toString())
                putString("history_id", item.id)
                putString("media_type", item.type)
                putString("reason", reason)
                putString("title", item.title.take(64))
            }
        )
    }

    private fun store(context: Context, item: HistoryModel, reason: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_RECORDS, emptySet()).orEmpty()
        val record = listOf(
            Instant.now().toString(),
            item.id,
            item.type,
            reason,
            item.title.replace("|", " ")
        ).joinToString("|")
        prefs.edit()
            .putStringSet(KEY_RECORDS, existing + record)
            .apply()
    }
}
