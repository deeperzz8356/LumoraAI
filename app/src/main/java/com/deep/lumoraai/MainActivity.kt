package com.deep.lumoraai

import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.deep.lumoraai.core.network.hasInternetConnection
import com.deep.lumoraai.core.localization.LocaleManager
import com.deep.lumoraai.core.navigation.NavGraph
import com.deep.lumoraai.core.theme.LumoraTheme
import com.deep.lumoraai.feature.network.NoInternetScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var notificationRoute by mutableStateOf<String?>(null)

    companion object {
        const val NOTIFICATION_ROUTE_EXTRA = "lumora_destination_route"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LumoraAI)
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.rgb(8, 16, 32)))
        window.statusBarColor = Color.rgb(8, 16, 32)
        window.navigationBarColor = Color.rgb(8, 16, 32)
        notificationRoute = intent.getStringExtra(NOTIFICATION_ROUTE_EXTRA)

        setContent {
            LumoraTheme {
                val context = LocalContext.current
                var hasInternet by remember { mutableStateOf(true) }

                LaunchedEffect(context) {
                    delay(500) // Brief initial check delay
                    hasInternet = context.hasInternetConnection()
                }

                DisposableEffect(context) {
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val mainHandler = Handler(Looper.getMainLooper())
                    val callback = object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            mainHandler.post {
                                hasInternet = context.hasInternetConnection()
                            }
                        }

                        override fun onLost(network: Network) {
                            mainHandler.postDelayed({
                                hasInternet = context.hasInternetConnection()
                            }, 1000) // Give some time before checking
                        }

                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: android.net.NetworkCapabilities
                        ) {
                            mainHandler.post {
                                hasInternet = context.hasInternetConnection()
                            }
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

    override fun attachBaseContext(newBase: Context) {
        val code = newBase.getSharedPreferences("lumora_settings", Context.MODE_PRIVATE)
            .getString("locale_code", "en")
        super.attachBaseContext(LocaleManager.apply(newBase, code ?: "en"))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationRoute = intent.getStringExtra(NOTIFICATION_ROUTE_EXTRA)
    }
}
