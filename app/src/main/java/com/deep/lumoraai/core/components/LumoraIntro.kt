package com.deep.lumoraai.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.theme.IntroTypography

private val IntroButtonShape = RoundedCornerShape(28.dp)
private val IntroFieldShape = RoundedCornerShape(24.dp)

@Composable
fun LumoraIntroBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        IntroPalette.GradientInner.copy(alpha = 0.4f),
                        IntroPalette.GradientMid.copy(alpha = 0.2f),
                        IntroPalette.BackgroundBase
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw random stars for a premium starry background
            val starColor = Color.White
            val random = java.util.Random(42) // Fixed seed for consistent starry pattern
            for (i in 0 until 100) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val radius = random.nextFloat() * 1.5.dp.toPx()
                val alpha = random.nextFloat() * 0.3f + 0.1f // Reduced opacity for subtler sparkle
                drawCircle(
                    color = starColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun LumoraIntroLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = 36.dp, height = 48.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.8f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = IntroPalette.AccentLime)
        }
        Text(
            text = "ai",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 6.dp)
        )
    }
}

@Composable
fun LumoraIntroPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(IntroButtonShape)
            .background(
                color = if (enabled) IntroPalette.PrimaryButton else IntroPalette.PrimaryButton.copy(alpha = 0.5f),
                shape = IntroButtonShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            when {
                leadingContent != null -> {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.offset(y = 2.dp)) {
                            leadingContent()
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                leadingIcon != null -> {
                    Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
            Text(text = text, style = IntroTypography.buttonLabel)
        }
    }
}

@Composable
fun LumoraIntroSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 56.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(IntroPalette.SurfaceRaised, IntroButtonShape)
            .border(1.dp, IntroPalette.BorderSubtle, IntroButtonShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = IntroPalette.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(text = text, style = IntroTypography.buttonLabel)
        }
    }
}

@Composable
fun LumoraIntroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = IntroFieldShape,
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions(keyboardType = KeyboardType.Email)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IntroPalette.SurfaceRaised,
            unfocusedContainerColor = IntroPalette.SurfaceRaised,
            focusedBorderColor = IntroPalette.PrimaryButton.copy(alpha = 0.6f),
            unfocusedBorderColor = IntroPalette.BorderSubtle,
            focusedTextColor = IntroPalette.TextPrimary,
            unfocusedTextColor = IntroPalette.TextPrimary,
            focusedLabelColor = IntroPalette.TextMuted,
            unfocusedLabelColor = IntroPalette.TextSubtle,
            cursorColor = IntroPalette.PrimaryButton
        )
    )
}

@Composable
fun GoogleBrandIcon(
    modifier: Modifier = Modifier,
    tint: Color = IntroPalette.TextPrimary,
) {
    Canvas(modifier = modifier) {
        val iconSize = 24f
        val scale = minOf(size.width / iconSize, size.height / iconSize)
        val offsetX = (size.width - iconSize * scale) / 2f
        val offsetY = (size.height - iconSize * scale) / 2f + 1.5f * scale
        drawContext.canvas.save()
        drawContext.transform.translate(offsetX, offsetY)
        drawContext.transform.scale(scale, scale)
        drawGoogleSegments(tint)
        drawContext.canvas.restore()
    }
}

private fun DrawScope.drawGoogleSegments(tint: Color) {
    val parser = PathParser()
    val bluePath = parser.parsePathString(
        "M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
    ).toPath()
    drawPath(bluePath, tint)
    val greenPath = parser.parsePathString(
        "M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
    ).toPath()
    drawPath(greenPath, tint)
    val yellowPath = parser.parsePathString(
        "M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
    ).toPath()
    drawPath(yellowPath, tint)
    val redPath = parser.parsePathString(
        "M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
    ).toPath()
    drawPath(redPath, tint)
}
