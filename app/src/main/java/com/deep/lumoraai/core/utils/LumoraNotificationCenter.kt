package com.deep.lumoraai.core.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.deep.lumoraai.MainActivity
import com.deep.lumoraai.R
import com.deep.lumoraai.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object LumoraNotificationCenter {
    const val COMPLETION_PREFS = "lumora_completion_notifications"
    const val KEY_COMPLETION_EVENTS = "completion_events"

    private const val CHANNEL_ID = "lumora_completion_updates"
    private const val CHANNEL_NAME = "Generation updates"
    private const val MAX_STORED_EVENTS = 80
    private const val EVENT_TTL_MILLIS = 6L * 60L * 60L * 1000L
    private val _eventsVersion = MutableStateFlow(0)
    val eventsVersion: StateFlow<Int> = _eventsVersion.asStateFlow()

    fun notifyCompletion(
        context: Context,
        title: String,
        message: String,
        route: String,
        mediaType: String,
    ) {
        val appContext = context.applicationContext
        storeCompletionEvent(appContext, title, message, route, mediaType)

        if (!SettingsRepository(appContext).notificationsEnabled) return
        if (!hasNotificationPermission(appContext)) return

        ensureChannel(appContext)
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.NOTIFICATION_ROUTE_EXTRA, route)
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            route.hashCode() xor title.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(appContext).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    fun completionEvents(context: Context): List<CompletionNotificationEvent> {
        val prefs = context.applicationContext.getSharedPreferences(COMPLETION_PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getStringSet(KEY_COMPLETION_EVENTS, emptySet()).orEmpty()
        val cutoff = System.currentTimeMillis() - EVENT_TTL_MILLIS
        val fresh = stored
            .mapNotNull(::decodeEvent)
            .filter { it.createdAtMillis >= cutoff }
            .sortedByDescending { it.createdAtMillis }
        if (fresh.size != stored.size) {
            prefs.edit().putStringSet(KEY_COMPLETION_EVENTS, fresh.map(::encodeEvent).toSet()).apply()
        }
        return fresh
    }

    private fun storeCompletionEvent(
        context: Context,
        title: String,
        message: String,
        route: String,
        mediaType: String,
    ) {
        val prefs = context.getSharedPreferences(COMPLETION_PREFS, Context.MODE_PRIVATE)
        val current = completionEvents(context)
        val event = CompletionNotificationEvent(
            id = "completion:${System.currentTimeMillis()}:${title.hashCode()}",
            title = title,
            message = message,
            route = route,
            mediaType = mediaType,
            createdAtMillis = System.currentTimeMillis()
        )
        val encoded = (listOf(event) + current)
            .distinctBy { it.id }
            .take(MAX_STORED_EVENTS)
            .map(::encodeEvent)
            .toSet()
        prefs.edit().putStringSet(KEY_COMPLETION_EVENTS, encoded).apply()
        _eventsVersion.update { it + 1 }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts when Lumora media processing finishes."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun encodeEvent(event: CompletionNotificationEvent): String =
        listOf(
            event.id.escape(),
            event.title.escape(),
            event.message.escape(),
            event.route.escape(),
            event.mediaType.escape(),
            event.createdAtMillis.toString()
        ).joinToString("|")

    private fun decodeEvent(value: String): CompletionNotificationEvent? {
        val parts = value.split("|")
        if (parts.size != 6) return null
        return CompletionNotificationEvent(
            id = parts[0].unescape(),
            title = parts[1].unescape(),
            message = parts[2].unescape(),
            route = parts[3].unescape(),
            mediaType = parts[4].unescape(),
            createdAtMillis = parts[5].toLongOrNull() ?: return null
        )
    }

    private fun String.escape(): String = replace("%", "%25").replace("|", "%7C")
    private fun String.unescape(): String = replace("%7C", "|").replace("%25", "%")
}

data class CompletionNotificationEvent(
    val id: String,
    val title: String,
    val message: String,
    val route: String,
    val mediaType: String,
    val createdAtMillis: Long,
)
