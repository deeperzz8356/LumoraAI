package com.deep.lumoraai.feature.templates

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

private val previewState =
    TemplatesUiState.Success(
        categories = emptyList()
    )

@Preview(
    name = "Templates Screen",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
private fun TemplatesPreview() {

    LumoraTheme {

        TemplatesScreen(
            uiState = previewState,
            onNext = {},
            onNavigate = {}
        )
    }
}
