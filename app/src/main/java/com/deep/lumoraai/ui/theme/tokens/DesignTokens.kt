package com.deep.lumoraai.ui.theme.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

object Spacing {
    val none  = 0.dp
    val xs    = 4.dp
    val sm    = 8.dp
    val md    = 12.dp // According to DESIGN.md gutter
    val lg    = 16.dp
    val xl    = 24.dp
    val xxl   = 32.dp
    val xxxl  = 48.dp
    val containerMargin = 20.dp
}

object Elevation {
    val none   = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
    val level4 = 8.dp
    val level5 = 12.dp
}

object Duration {
    const val short1      = 50
    const val short2      = 100
    const val short3      = 150
    const val short4      = 200
    const val medium1     = 250
    const val medium2     = 300
    const val medium3     = 350
    const val medium4     = 400
    const val long1       = 450
    const val long2       = 500
    const val extraLong1  = 700
}

object AppEasing {
    val Standard        = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecel = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccel = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val Linear          = LinearEasing
}

object MotionTokens {
    fun spatialEnter() = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    fun spatialExit()  = tween<Float>(Duration.short4, easing = AppEasing.EmphasizedAccel)

    fun effectsStandard(duration: Int = Duration.medium2) = tween<Float>(duration, easing = AppEasing.Standard)
    fun effectsDecel(duration: Int = Duration.medium4)    = tween<Float>(duration, easing = AppEasing.EmphasizedDecel)

    fun containerEnter() = tween<IntOffset>(Duration.medium4, easing = AppEasing.EmphasizedDecel)
    fun containerExit()  = tween<IntOffset>(Duration.short4,  easing = AppEasing.EmphasizedAccel)
}
