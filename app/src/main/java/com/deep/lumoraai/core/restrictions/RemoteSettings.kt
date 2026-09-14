package com.deep.lumoraai.core.restrictions

import android.content.Context
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.ads.AdsConfigStore
import com.deep.lumoraai.ads.AdsManager
import com.google.firebase.remoteconfig.*
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/** Application-lifetime configuration loading, independent of ad eligibility. */
@Singleton
class RemoteSettings @Inject constructor(private val adsConfig: AdsConfigStore, private val ads: AdsManager) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false
    fun initialize(context: Context) {
        if (started) return
        started = true
        val app = context.applicationContext
        ToolPolicyStore.initialize(app)
        scope.launch { while (isActive) { ToolPolicyStore.refresh(); delay(45_000) } }
        val remote = FirebaseRemoteConfig.getInstance()
        fun apply() { adsConfig.applyRemoteConfig(remote); ads.initialize(app) }
        remote.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600).build())
        remote.ensureInitialized().addOnCompleteListener { apply() }
        remote.fetchAndActivate().addOnCompleteListener { apply() }
        remote.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(update: ConfigUpdate) {
                remote.activate().addOnCompleteListener {
                    apply()
                    scope.launch { ToolPolicyStore.refresh() }
                }
            }
            override fun onError(error: FirebaseRemoteConfigException) = Unit
        })
    }
}
