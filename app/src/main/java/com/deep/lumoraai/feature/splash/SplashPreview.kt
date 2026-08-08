package com.deep.lumoraai.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

@Preview(name = "Splash Light Preview", showBackground = true)
@Composable
fun SplashLightPreview() {
    LumoraTheme(darkTheme = false) { SplashScreen(isReady = true, onNext = {}) }
}

@Preview(name = "Splash Dark Preview", showBackground = true)
@Composable
fun SplashDarkPreview() {
    LumoraTheme(darkTheme = true) { SplashScreen(isReady = true, onNext = {}) }
}

@Preview(name = "Splash Tablet Preview", device = Devices.TABLET, showBackground = true)
@Composable
fun SplashTabletPreview() {
    LumoraTheme(darkTheme = true) { SplashScreen(isReady = true, onNext = {}) }
}

@Preview(name = "Splash Landscape Preview", widthDp = 891, heightDp = 411, showBackground = true)
@Composable
fun SplashLandscapePreview() {
    LumoraTheme(darkTheme = true) { SplashScreen(isReady = true, onNext = {}) }
}
