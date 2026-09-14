package com.deep.lumoraai

import android.app.Application
import com.deep.lumoraai.ads.AdsManager
import com.deep.lumoraai.ads.appopen.AppOpenLifecycleObserver
import com.deep.lumoraai.core.notification.NotificationChannelManager
import com.deep.lumoraai.core.notification.NotificationLifecycleHandler
import com.deep.lumoraai.core.notification.OneSignalManager
import com.deep.lumoraai.core.utils.CreditBalanceStore
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

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var appOpenLifecycleObserver: AppOpenLifecycleObserver

    @Inject lateinit var remoteSettings: com.deep.lumoraai.core.restrictions.RemoteSettings

    override fun onCreate() {
        super.onCreate()

        // Seed the credit header from the persisted cache so it shows instantly
        // on cold start (server reconciles it shortly after).
        CreditBalanceStore.initialize(this)

        // Initialize notification channels (Android 8.0+)
        notificationChannelManager.createNotificationChannels()
        
        // Initialize OneSignal for push notifications
        oneSignalManager.initialize()
        
        // Initialize notification lifecycle handler
        notificationLifecycleHandler.initialize()

        // Initialize Google Mobile Ads after fetching ad unit IDs from Remote Config.
        remoteSettings.initialize(this)

        // Observe foreground/background transitions to show App Open ads.
        appOpenLifecycleObserver.register(this)
    }
}
