package com.deep.lumoraai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.deep.lumoraai.core.deeplink.DeepLinkHandler
import com.deep.lumoraai.core.navigation.NavGraph
import com.deep.lumoraai.ui.theme.LumoraAITheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity to handle deep links from notifications and other sources
 */
@AndroidEntryPoint
class DeepLinkActivity : ComponentActivity() {
    companion object {
        private const val TAG = "DeepLinkActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep link from intent
        val deepLinkUri = intent?.data
        
        setContent {
            LumoraAITheme {
                val navController = rememberNavController()
                
                LaunchedEffect(deepLinkUri) {
                    if (deepLinkUri != null) {
                        Log.d(TAG, "Handling deep link: $deepLinkUri")
                        val handled = DeepLinkHandler.handleDeepLink(deepLinkUri, navController)
                        if (!handled) {
                            Log.w(TAG, "Failed to handle deep link: $deepLinkUri")
                        }
                    }
                }
                
                NavGraph(modifier = Modifier)
            }
        }
        
        // Log deep link for debugging
        logDeepLink()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        val deepLinkUri = intent.data
        if (deepLinkUri != null) {
            Log.d(TAG, "New deep link intent: $deepLinkUri")
            // Re-create UI with new deep link
            recreate()
        }
    }

    private fun logDeepLink() {
        val action = intent?.action
        val data = intent?.data
        val extras = intent?.extras
        
        Log.d(TAG, "Deep Link Info:")
        Log.d(TAG, "  Action: $action")
        Log.d(TAG, "  Data: $data")
        Log.d(TAG, "  Type: ${intent?.type}")
        
        extras?.keySet()?.forEach { key ->
            Log.d(TAG, "  Extra: $key = ${extras.get(key)}")
        }
    }
}
