package com.deep.lumoraai

import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.deep.lumoraai.core.network.hasInternetConnection
import com.deep.lumoraai.core.navigation.NavGraph
import com.deep.lumoraai.core.notification.OneSignalManager
import com.deep.lumoraai.core.theme.LumoraTheme
import com.deep.lumoraai.feature.network.NoInternetScreen
import com.onesignal.OneSignal
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicBoolean

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var oneSignalManager: OneSignalManager

    private var notificationRoute by mutableStateOf<String?>(null)

    companion object {
        private const val TAG = "MainActivity"
        const val NOTIFICATION_ROUTE_EXTRA = "lumora_destination_route"
    }

    // Ensures the verification dialog is shown exactly once
    private val dialogShown = AtomicBoolean(false)

    // OneSignal stores observers weakly — keep a strong reference for the activity lifetime
    private var pushSubscriptionObserver: IPushSubscriptionObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LumoraAI)
        super.onCreate(savedInstanceState)
        notificationRoute = intent.getStringExtra(NOTIFICATION_ROUTE_EXTRA)

        // Set up push subscription observer for verification dialog
        setupPushSubscriptionObserver()

        setContent {
            LumoraTheme {
                val context = LocalContext.current
                var hasInternet by remember { mutableStateOf(context.hasInternetConnection()) }

                DisposableEffect(context) {
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val callback = object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            runOnUiThread { hasInternet = context.hasInternetConnection() }
                        }

                        override fun onLost(network: Network) {
                            runOnUiThread { hasInternet = context.hasInternetConnection() }
                        }

                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: android.net.NetworkCapabilities
                        ) {
                            runOnUiThread { hasInternet = context.hasInternetConnection() }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        connectivityManager.registerDefaultNetworkCallback(callback)
                    }

                    onDispose {
                        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
                    }
                }

                if (hasInternet) {
                    NavGraph(
                        notificationRoute = notificationRoute,
                        onNotificationRouteConsumed = { notificationRoute = null }
                    )
                } else {
                    NoInternetScreen(
                        onTurnOnNetwork = {
                            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                        },
                        onRetry = {
                            hasInternet = context.hasInternetConnection()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationRoute = intent.getStringExtra(NOTIFICATION_ROUTE_EXTRA)
    }

    /**
     * Set up push subscription observer to verify OneSignal registration
     * Shows a verification dialog when the device receives a real, server-assigned subscription ID
     */
    private fun setupPushSubscriptionObserver() {
        val observer = object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                Log.d(TAG, "Push subscription changed: ${state.current.id}")
                maybeShowIntegrationCompleteDialog(state.current.id)
            }
        }
        pushSubscriptionObserver = observer
        OneSignal.User.pushSubscription.addObserver(observer)

        // The subscription ID may already be server-assigned before the observer attaches,
        // so evaluate the current value immediately as well
        val currentId = OneSignal.User.pushSubscription.id
        Log.d(TAG, "Current push subscription ID: $currentId")
        maybeShowIntegrationCompleteDialog(currentId)
    }

    /**
     * Check if the subscription ID is a real, server-assigned value
     * Returns false for local- placeholder IDs (pre-registration)
     */
    private fun isRegistered(subscriptionId: String?): Boolean {
        return !subscriptionId.isNullOrEmpty() && !subscriptionId.startsWith("local-")
    }

    /**
     * Show the verification dialog if:
     * 1. Device has a real, server-assigned subscription ID
     * 2. Dialog hasn't been shown yet in this session
     */
    private fun maybeShowIntegrationCompleteDialog(subscriptionId: String?) {
        if (isRegistered(subscriptionId) && dialogShown.compareAndSet(false, true)) {
            Log.d(TAG, "Showing integration complete dialog")
            showIntegrationCompleteDialog()
        }
    }

    /**
     * Display the OneSignal integration verification dialog
     */
    private fun showIntegrationCompleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Your OneSignal SDK integration is complete!")
            .setMessage(
                "You can now send Push Notifications & In-App Messages through OneSignal. " +
                "Tap below to enable push notifications."
            )
            .setPositiveButton("Got it") { _, _ ->
                requestPushPermission()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Request push notification permission from the user
     */
    private fun requestPushPermission() {
        try {
            // Launch coroutine on main thread to request push permission
            CoroutineScope(Dispatchers.Main).launch {
                oneSignalManager.requestPushPermission()
                Log.d(TAG, "Push permission request completed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting push permission: ${e.message}", e)
        }
    }
}
