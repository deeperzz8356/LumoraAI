package com.deep.lumoraai.feature.notifications

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.CompletionNotificationEvent
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.core.utils.LumoraNotificationCenter
import com.deep.lumoraai.data.model.ActiveJobInfo
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.data.repository.SettingsRepository
import com.deep.lumoraai.feature.notifications.model.NotificationModel
import com.deep.lumoraai.feature.notifications.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefs = appContext.getSharedPreferences("lumora_notifications", Context.MODE_PRIVATE)
    private val settingsRepository = SettingsRepository(appContext)
    private val appPreferences = AppPreferencesRepository.getInstance(appContext)
    private val generationRepository = GenerationRepository()

    private var activeJobs: List<ActiveJobInfo> = emptyList()
    private var credits: Int? = null

    var uiState: NotificationsUiState by mutableStateOf(NotificationsUiState.Loading)
        private set

    init {
        load()
        observeJobs()
        observeCompletionEvents()
    }

    fun load() {
        rebuild()
        loadCredits()
    }

    fun markAllRead() {
        val current = uiState as? NotificationsUiState.Success ?: return
        persistSet(KEY_READ_IDS, current.items.map { it.id }.toSet())
        rebuild()
    }

    fun markRead(id: String) {
        updateSet(KEY_READ_IDS) { it + id }
        rebuild()
    }

    fun dismiss(id: String) {
        updateSet(KEY_DISMISSED_IDS) { it + id }
        rebuild()
    }

    fun clearDismissed() {
        prefs.edit().remove(KEY_DISMISSED_IDS).apply()
        rebuild()
    }

    private fun observeJobs() {
        viewModelScope.launch {
            GenerationRepository.activeJobs.collect { jobs ->
                activeJobs = jobs
                rebuild()
            }
        }
    }

    private fun observeCompletionEvents() {
        viewModelScope.launch {
            LumoraNotificationCenter.eventsVersion.collect {
                rebuild()
            }
        }
    }

    private fun loadCredits() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            credits = null
            rebuild()
            return
        }

        viewModelScope.launch {
            credits = if (appPreferences.isDeveloperModeEnabled()) {
                GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
            } else {
                LocalCreditBalance.maxWith(getApplication(), generationRepository.getCredits().getOrNull())
            }
            rebuild()
        }
    }

    private fun rebuild() {
        val readIds = prefs.getStringSet(KEY_READ_IDS, emptySet()).orEmpty()
        val dismissedIds = prefs.getStringSet(KEY_DISMISSED_IDS, emptySet()).orEmpty()
        val notificationsEnabled = settingsRepository.notificationsEnabled

        val generated = buildList {
            addAll(activeJobs.map(::jobNotification))
            addAll(LumoraNotificationCenter.completionEvents(appContext).map(::completionNotification))
            credits?.takeIf { it < GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY && it <= 10 }?.let {
                add(creditNotification(it))
            }
            if (!notificationsEnabled) add(disabledNotification())
        }
            .distinctBy { it.id }
            .filterNot { it.id in dismissedIds }
            .map { it.copy(isRead = it.id in readIds) }
            .sortedWith(compareBy<NotificationModel> { it.isRead }.thenBy { it.type.ordinal })

        uiState = if (generated.isEmpty()) {
            NotificationsUiState.Empty(notificationsEnabled = notificationsEnabled)
        } else {
            NotificationsUiState.Success(
                items = generated,
                unreadCount = generated.count { !it.isRead },
                notificationsEnabled = notificationsEnabled
            )
        }
    }

    private fun jobNotification(job: ActiveJobInfo): NotificationModel {
        val progress = job.progressPercent?.coerceIn(0f, 1f)
        val percent = progress?.let { (it * 100).toInt() }
        val done = job.isCompleted
        val route = if (done) Screen.History.route else Screen.Queue.route
        return NotificationModel(
            id = "job:${job.title}:${job.statusText}",
            title = if (done) "${job.title} is ready" else job.title,
            message = if (done) "Your ${job.mediaType.lowercase()} finished rendering." else "${job.statusText}${percent?.let { " - $it%" }.orEmpty()}",
            timeLabel = if (done) "Ready now" else "In progress",
            type = NotificationType.Generation,
            route = route,
            progress = if (done) null else progress
        )
    }

    private fun completionNotification(event: CompletionNotificationEvent): NotificationModel =
        NotificationModel(
            id = event.id,
            title = event.title,
            message = event.message,
            timeLabel = "Just now",
            type = NotificationType.Generation,
            route = event.route
        )

    private fun creditNotification(balance: Int): NotificationModel {
        val isUnlimited = balance >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
        val isLow = !isUnlimited && balance <= 10
        return NotificationModel(
            id = "credits:$balance",
            title = if (isLow) "Credits running low" else "Credits balance updated",
            message = if (isUnlimited) "Developer mode is active with unlimited credits." else "You have $balance LUM credits available.",
            timeLabel = "Now",
            type = NotificationType.Credits,
            route = if (isLow) Screen.Credits.route else Screen.Profile.route
        )
    }

    private fun disabledNotification(): NotificationModel =
        NotificationModel(
            id = "settings:notifications-disabled",
            title = "Push notifications are off",
            message = "Turn them on in Settings to keep up with generation updates.",
            timeLabel = "Action needed",
            type = NotificationType.Account,
            route = Screen.Settings.route
        )

    private fun updateSet(key: String, block: (Set<String>) -> Set<String>) {
        val current = prefs.getStringSet(key, emptySet()).orEmpty()
        persistSet(key, block(current))
    }

    private fun persistSet(key: String, values: Set<String>) {
        prefs.edit().putStringSet(key, values).apply()
    }

    companion object {
        private const val KEY_READ_IDS = "read_ids"
        private const val KEY_DISMISSED_IDS = "dismissed_ids"
    }
}
