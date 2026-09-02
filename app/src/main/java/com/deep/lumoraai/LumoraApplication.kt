package com.deep.lumoraai

import android.app.Application
import com.deep.lumoraai.core.notification.NotificationChannelManager
import com.deep.lumoraai.core.notification.NotificationLifecycleHandler
import com.deep.lumoraai.core.notification.OneSignalManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LumoraApplication : Application() {
    @Inject
    lateinit var oneSignalManager: OneSignalManager

    @Inject
    lateinit var notificationLifecycleHandler: NotificationLifecycleHandler

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channels (Android 8.0+)
        notificationChannelManager.createNotificationChannels()
        
        // Initialize OneSignal for push notifications
        oneSignalManager.initialize()
        
        // Initialize notification lifecycle handler
        notificationLifecycleHandler.initialize()
    }
}
