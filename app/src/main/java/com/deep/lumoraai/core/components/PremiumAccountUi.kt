package com.deep.lumoraai.core.components

import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PremiumBackground = Color(0xFF080D18)
val PremiumSurface = Color(0xFF111827)
val PremiumSurfaceRaised = Color(0xFF172033)
val PremiumStroke = Color(0xFF273249)
val PremiumLime = Color(0xFFD6FF3F)
val PremiumViolet = Color(0xFF8B74FF)
val PremiumText = Color(0xFFF7F8FA)
val PremiumMuted = Color(0xFF98A5BC)
val PremiumDanger = Color(0xFFFF7D88)
val PremiumShape = RoundedCornerShape(20.dp)

@Composable
fun PremiumPage(
    title: String,
    eyebrow: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(PremiumBackground)) {
        val sidePadding = if (maxWidth >= 600.dp) 32.dp else 18.dp
        val scrollModifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .then(scrollModifier)
                .padding(horizontal = sidePadding)
                .padding(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PremiumTopBar(title, eyebrow, subtitle, onBack, action)
            content()
        }
    }
}

@Composable
fun PremiumTopBar(
    title: String,
    eyebrow: String,
    subtitle: String,
    onBack: () -> Unit,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(TablerIcons.ArrowLeft, "Go back", tint = PremiumText)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                eyebrow.uppercase(),
                color = PremiumLime,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
            )
            Text(
                title,
                color = PremiumText,
                fontSize = 22.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.7).sp,
                modifier = Modifier.semantics { heading() },
            )
            Text(subtitle, color = PremiumMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        action?.invoke()
    }
}

@Composable
fun PremiumSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                color = PremiumText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.35).sp,
                modifier = Modifier.semantics { heading() },
            )
            subtitle?.let { Text(it, color = PremiumMuted, fontSize = 11.sp, lineHeight = 15.sp) }
        }
        content()
    }
}

@Composable
fun PremiumHero(
    label: String,
    title: String,
    body: String,
    icon: ImageVector,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PremiumSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shadowElevation = 12.dp,
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(listOf(Color(0xFF202548), Color(0xFF151B2D), Color(0xFF0F1726))),
            ),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(PremiumLime.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = PremiumLime, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            label.uppercase(),
                            color = PremiumLime,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                        )
                        Text(title, color = PremiumText, fontSize = 19.sp, lineHeight = 22.sp, fontWeight = FontWeight.Black)
                        Text(body, color = PremiumMuted, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                content?.invoke(this)
            }
        }
    }
}

@Composable
fun PremiumActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    danger: Boolean = false,
    enabled: Boolean = true,
) {
    val accent = if (danger) PremiumDanger else PremiumLime
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = PremiumSurface,
        border = BorderStroke(1.dp, PremiumStroke.copy(alpha = 0.72f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(accent.copy(alpha = 0.10f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = if (danger) PremiumDanger else PremiumText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = PremiumMuted, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            trailing?.let {
                Text(it, color = accent, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } ?: Icon(TablerIcons.ChevronRight, null, tint = accent.copy(alpha = 0.9f), modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
fun PremiumToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = PremiumSurface,
        border = BorderStroke(1.dp, if (checked) PremiumLime.copy(alpha = 0.24f) else PremiumStroke.copy(alpha = 0.70f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(PremiumLime.copy(alpha = 0.09f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = if (checked) PremiumLime else PremiumMuted, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = PremiumText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = PremiumMuted, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PremiumBackground,
                    checkedTrackColor = PremiumLime,
                    uncheckedThumbColor = PremiumMuted,
                    uncheckedTrackColor = PremiumSurfaceRaised,
                    uncheckedBorderColor = PremiumStroke,
                ),
            )
        }
    }
}
