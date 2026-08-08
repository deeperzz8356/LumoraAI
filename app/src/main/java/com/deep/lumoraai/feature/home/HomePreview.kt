package com.deep.lumoraai.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState = HomeUiState.Success(
    userName = "Dev",
    credits = 120,
    creationCount = 12,
    planLabel = "Premium",
    recentItems = emptyList(),
)

@Preview(name = "Home Preview", showBackground = true)
@Composable
fun HomePreview() {
    LumoraTheme(darkTheme = true) {
        HomeScreen(uiState = previewState, onNext = {})
    }
}

@Preview(name = "Home With Recent", showBackground = true)
@Composable
fun HomeWithRecentPreview() {
    LumoraTheme(darkTheme = true) {
        HomeScreen(
            uiState = previewState.copy(
                recentItems = listOf(
                    HomeRecentItem("1", "Fantasy Portrait", "2h ago", null, com.deep.lumoraai.R.drawable.style_fantasy),
                    HomeRecentItem("2", "Product Render", "Yesterday", null, com.deep.lumoraai.R.drawable.style_digital),
                )
            ),
            onNext = {}
        )
    }
}

@Preview(name = "Home Tablet", device = Devices.TABLET, showBackground = true)
@Composable
fun HomeTabletPreview() {
    LumoraTheme(darkTheme = true) { HomeScreen(uiState = previewState, onNext = {}) }
}
