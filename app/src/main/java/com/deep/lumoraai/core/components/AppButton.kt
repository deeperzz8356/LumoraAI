package com.deep.lumoraai.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.ui.theme.tokens.Spacing

enum class AppButtonVariant { Filled, Tonal, Outlined, Ghost, Destructive }

@Composable
fun AppButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Filled,
    leadingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    val enabled2 = enabled && !isLoading
    // Use full pill shape for primary buttons per DESIGN.md
    val shape = MaterialTheme.shapes.extraLarge 
    
    val content: @Composable RowScope.() -> Unit = {
        AnimatedContent(targetState = isLoading, label = "btnContent") { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), 
                    strokeWidth = 2.dp, 
                    color = LocalContentColor.current
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.xs))
                    }
                    Text(text, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
    
    val mod = modifier.height(48.dp)
    
    when (variant) {
        AppButtonVariant.Filled -> Button(onClick, mod, enabled = enabled2, shape = shape, content = content)
        AppButtonVariant.Tonal -> FilledTonalButton(onClick, mod, enabled = enabled2, shape = shape, content = content)
        AppButtonVariant.Outlined -> OutlinedButton(onClick, mod, enabled = enabled2, shape = shape, content = content)
        AppButtonVariant.Ghost -> TextButton(onClick, mod, enabled = enabled2, shape = shape, content = content)
        AppButtonVariant.Destructive -> Button(
            onClick, mod, enabled = enabled2, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ), 
            content = content
        )
    }
}
