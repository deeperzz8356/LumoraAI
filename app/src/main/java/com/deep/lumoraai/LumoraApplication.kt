package com.deep.lumoraai

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class LumoraApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var startedActivities = 0

    override fun onCreate() {
        super.onCreate()
        
        // Initialize RevenueCat
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(
                context = this,
                apiKey = "goog_placeholder_revenuecat_api_key" // Replace with real key in production
            ).build()
        )

        val appPreferences = AppPreferencesRepository.getInstance(this)
        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityStarted(activity: Activity) {
                    startedActivities++
                }

                override fun onActivityStopped(activity: Activity) {
                    startedActivities--
                    if (startedActivities <= 0) {
                        startedActivities = 0
                        appScope.launch {
                            appPreferences.resetDeveloperSession()
                        }
                    }
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityResumed(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            }
        )
    }
}
