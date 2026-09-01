package com.deep.lumoraai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Navigation tab with icon centered in a circle and text label below
 *
 * @param icon The icon to display in the center of the circle
 * @param label The text label to display below the circle
 * @param onClick Callback when the tab is clicked
 * @param modifier Optional modifier for the entire composable
 * @param circleBackgroundColor Color of the circle background
 * @param iconColor Color of the icon
 * @param labelColor Color of the label text
 * @param circleSize Size of the circle (default 60dp)
 * @param isSelected Whether this tab is currently selected
 */
@Composable
fun NavTabWithIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    circleBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    circleSize: Float = 60f,
    isSelected: Boolean = false
) {
    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Circular background with icon
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(circleSize.dp)
                .background(
                    color = if (isSelected) circleBackgroundColor else circleBackgroundColor.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            tint = if (isSelected) iconColor else iconColor.copy(alpha = 0.6f)
        )

        // Label text below the circle
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = labelColor,
            maxLines = 2
        )
    }
}

/**
 * Navigation tab item with customizable circle appearance
 *
 * @param icon The icon to display
 * @param label The text label
 * @param onClick Callback when clicked
 * @param modifier Optional modifier
 * @param circleBackgroundColor Background color of the circle
 * @param iconColor Color of the icon inside the circle
 * @param labelColor Color of the label text
 * @param circleSize Size of the circle in dp
 * @param isSelected Whether the tab is selected
 * @param circleElevation Optional elevation for the circle (creates shadow effect)
 */
@Composable
fun NavTabWithIconAdvanced(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    circleBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    circleSize: Float = 60f,
    isSelected: Boolean = false,
    circleElevation: Float = 0f
) {
    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon inside circle with optional padding for elevation effect
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(circleSize.dp)
                .background(
                    color = if (isSelected) circleBackgroundColor else circleBackgroundColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            tint = if (isSelected) iconColor else labelColor.copy(alpha = 0.5f)
        )

        // Label text positioned below the circle
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = labelColor,
            maxLines = 2
        )
    }
}
