package com.deep.lumoraai.feature.splash

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.PlacementBanner
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.databinding.SplashScreenBinding
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(isReady: Boolean, onNext: () -> Unit, modifier: Modifier = Modifier) {
    val ads = LocalAdsManager.current
    val activity = rememberCurrentActivity()
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }
    val messages = remember { listOf("INITIALIZING ENGINE...", "LOADING MODELS...", "OPTIMIZING GENERATION...") }
    LaunchedEffect(isReady) {
        if (isReady) {
            delay(1_400)
            if (ads == null) onNext()
            else if (ads.isFirstLaunch(context)) { ads.markLaunched(context); onNext() }
            else ads.showInterstitial(activity, AdPlacement.INTER_POST_SPLASH, onContinue = onNext)
        }
    }
    DisposableEffect(Unit) { onDispose { handler.removeCallbacksAndMessages(null) } }
    Box(modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                val binding = SplashScreenBinding.inflate(LayoutInflater.from(context))
                var index = 0
                val ticker = object : Runnable {
                    override fun run() {
                        index = (index + 1) % messages.size
                        binding.statusText.text = messages[index]
                        handler.postDelayed(this, 900)
                    }
                }
                handler.postDelayed(ticker, 900)
                binding.root
            }, modifier = Modifier.fillMaxSize()
        )
        PlacementBanner(placement = AdPlacement.BANNER_SPLASH, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
