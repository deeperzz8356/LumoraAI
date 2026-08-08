package com.deep.lumoraai.core.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography aligned with onboarding — SansSerif, explicit sizes, IntroPalette colors.
 */
object IntroTypography {
    private val sans = FontFamily.SansSerif

    val greetingLabel = TextStyle(
        fontFamily = sans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = IntroPalette.TextMuted,
        lineHeight = 20.sp,
    )

    val greetingName = TextStyle(
        fontFamily = sans,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextPrimary,
        lineHeight = 28.sp,
    )

    val creditsChip = TextStyle(
        fontFamily = sans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.SecondaryText,
        lineHeight = 16.sp,
    )

    val statLabel = TextStyle(
        fontFamily = sans,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextSubtle,
        letterSpacing = 2.sp,
        lineHeight = 12.sp,
    )

    val statValue = TextStyle(
        fontFamily = sans,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextPrimary,
        lineHeight = 24.sp,
    )

    val sectionTitle = TextStyle(
        fontFamily = sans,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextPrimary,
        lineHeight = 24.sp,
    )

    val sectionLink = TextStyle(
        fontFamily = sans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.SecondaryText,
        lineHeight = 20.sp,
    )

    val cardTitle = TextStyle(
        fontFamily = sans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextPrimary,
        lineHeight = 20.sp,
    )

    val cardSubtitle = TextStyle(
        fontFamily = sans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = IntroPalette.TextSubtle,
        lineHeight = 16.sp,
    )

    val body = TextStyle(
        fontFamily = sans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = IntroPalette.TextMuted,
        lineHeight = 20.sp,
    )

    val toolTitle = TextStyle(
        fontFamily = sans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextPrimary,
        lineHeight = 18.sp,
    )

    val toolDescription = TextStyle(
        fontFamily = sans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = IntroPalette.TextSubtle,
        lineHeight = 16.sp,
    )

    val badge = TextStyle(
        fontFamily = sans,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 12.sp,
    )

    val upgradeTitle = TextStyle(
        fontFamily = sans,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextPrimary,
        lineHeight = 24.sp,
    )

    val buttonLabel = TextStyle(
        fontFamily = sans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = IntroPalette.TextPrimary,
        lineHeight = 20.sp,
    )

    val navLabel = TextStyle(
        fontFamily = sans,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
    )
}
