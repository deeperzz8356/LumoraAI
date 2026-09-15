package com.deep.lumoraai.feature.credits

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.dp
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.databinding.CreditsScreenBinding
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Coin
import compose.icons.tablericons.Gift
import compose.icons.tablericons.Mail
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.RotateClockwise
import compose.icons.tablericons.Share
import compose.icons.tablericons.Stars

@Composable
internal fun SpinWheelDialog(
    state: CreditsUiState.Success,
    onSpin: () -> Unit,
    onDismiss: () -> Unit,
) {
    val result = state.spinResult
    val targetRotation = when {
        state.isRewardBusy -> 360f
        result == null -> 0f
        result.creditsAwarded >= 50 -> 720f + 18f
        result.creditsAwarded >= 25 -> 720f + 90f
        result.creditsAwarded >= 10 -> 720f + 162f
        result.creditsAwarded > 0 -> 720f + 234f
        else -> 720f + 306f
    }
    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = if (result == null && !state.isRewardBusy) 0 else 1200),
        label = "spinWheelRotation",
    )
    val resultText = result?.let {
        if (it.creditsAwarded > 0) "You won +${it.creditsAwarded} credits!" else "Better luck next time!"
    } ?: if (state.isRewardBusy) "Spinning..." else "1 free spin resets every week"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ComposeColor(0xFF101827),
            tonalElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Spin Weekly Wheel", color = ComposeColor.White, fontWeight = FontWeight.Black)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(230.dp)) {
                    SpinWheel(rotation)
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(ComposeColor(0xFFD6FF2F), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(TablerIcons.RotateClockwise, contentDescription = null, tint = ComposeColor(0xFF081020), modifier = Modifier.size(30.dp))
                    }
                }
                Text(resultText, color = if (result?.creditsAwarded == 0) ComposeColor(0xFF9AA5B8) else ComposeColor(0xFFD6FF2F), fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        if (result == null) onSpin() else onDismiss()
                    },
                    enabled = !state.isRewardBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComposeColor(0xFFD6FF2F),
                        contentColor = ComposeColor(0xFF081020),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (result == null) "Spin" else "Done", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
@Composable
private fun SpinWheel(rotation: Float) {
    // Keep visual sectors equal; reward probabilities remain server-controlled.
    val labels = listOf("50", "25", "10", "2", "Better\nluck")
    val colors = listOf(
        ComposeColor(0xFFD6FF2F),
        ComposeColor(0xFF9C63FF),
        ComposeColor(0xFF30D8DE),
        ComposeColor(0xFFFF3D9D),
        ComposeColor(0xFF5DD96B),
    )
    Canvas(modifier = Modifier.size(214.dp).rotate(rotation)) {
        val sliceSweep = 360f / labels.size
        var startAngle = -90f
        labels.forEachIndexed { index, _ ->
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sliceSweep,
                useCenter = true,
            )
            startAngle += sliceSweep
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16.sp.toPx()
            setShadowLayer(5f, 0f, 2f, Color.argb(135, 0, 0, 0))
        }
        val darkLabelPaint = Paint(labelPaint).apply {
            color = Color.rgb(8, 16, 32)
            clearShadowLayer()
        }
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val labelRadius = size.minDimension * 0.34f
        var labelStartAngle = -90f
        drawContext.canvas.nativeCanvas.apply {
            labels.forEachIndexed { index, label ->
                val angle = Math.toRadians((labelStartAngle + sliceSweep / 2f).toDouble())
                val x = centerX + kotlin.math.cos(angle).toFloat() * labelRadius
                val y = centerY + kotlin.math.sin(angle).toFloat() * labelRadius
                val paint = if (index == 0) darkLabelPaint else labelPaint
                val lines = label.split('\n')
                val lineHeight = paint.textSize * 0.92f
                val firstBaseline = y - ((lines.size - 1) * lineHeight / 2f) + (paint.textSize * 0.34f)
                lines.forEachIndexed { lineIndex, line ->
                    drawText(line, x, firstBaseline + lineIndex * lineHeight, paint)
                }
                labelStartAngle += sliceSweep
            }
        }
        drawCircle(
            color = ComposeColor.White.copy(alpha = 0.22f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f, cap = StrokeCap.Round),
        )
    }
}

