package com.deep.lumoraai.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.theme.LumoraTheme
import androidx.compose.ui.res.stringResource
import com.deep.lumoraai.R

/**
 * App footer showing "Powered by LumoraAI" credit
 * Displayed at the bottom of all main screens (except auth screens)
 */
@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.powered_by_lumora_ai),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(name = "App Footer")
@Composable
private fun AppFooterPreview() {
    LumoraTheme {
        AppFooter()
    }
}