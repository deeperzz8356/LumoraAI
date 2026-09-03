package com.deep.lumoraai.feature.credits

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.res.stringResource

private val CredBackground = Color(0xFF081020)
private val CredCard = Color(0xFF10192D)
private val CredStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Pink = Color(0xFFFF3D9D)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun CreditsScreen(
    uiState: CreditsUiState,
    viewModel: CreditsViewModel,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as? Activity
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CredBackground,
        bottomBar = { BottomNavigationBar(emptyList(), "credits", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CredBackground)
                .padding(padding)
        ) {
            when (uiState) {
                CreditsUiState.Loading -> AppLoadingScreen()
                is CreditsUiState.Error -> AppErrorScreen(message = uiState.message)
                is CreditsUiState.Success -> CreditsContent(
                    credits = uiState.credits,
                    isDeveloperMode = uiState.isDeveloperMode,
                    rewards = uiState.rewards,
                    rewardMessage = uiState.rewardMessage,
                    purchaseMessage = uiState.purchaseMessage,
                    isRewardBusy = uiState.isRewardBusy,
                    checkInDayIndex = uiState.checkInDayIndex,
                    onBack = onBack,
                    onBuy = { viewModel.buyCredits(it, activity) },
                    onClaimReward = { viewModel.claimReward(it) },
                    onClearRewardMessage = { viewModel.clearRewardMessage() },
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun CreditsContent(
    credits: Int,
    isDeveloperMode: Boolean,
    rewards: List<CreditRewardUi>,
    rewardMessage: String?,
    purchaseMessage: String?,
    isRewardBusy: Boolean,
    checkInDayIndex: Int,
    onBack: () -> Unit,
    onBuy: (Int) -> Unit,
    onClaimReward: (String) -> Unit,
    onClearRewardMessage: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val balanceLabel = if (isDeveloperMode || credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "Unlimited" else "$credits"
    val showSpinWheel = remember { mutableStateOf(false) }
    val spinReward = rewards.firstOrNull { it.id == "spin" }

    if (showSpinWheel.value && spinReward != null) {
        SpinWheelDialog(
            reward = spinReward,
            isRewardBusy = isRewardBusy,
            onDismiss = { showSpinWheel.value = false },
            onSpin = {
                showSpinWheel.value = false
                onClaimReward(spinReward.id)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PageTopBar(title = stringResource(com.deep.lumoraai.R.string.ui_credits), subtitle = stringResource(com.deep.lumoraai.R.string.ui_fuel_every_generation), onBack = onBack)
        BalanceHero(balanceLabel = balanceLabel, isDeveloperMode = isDeveloperMode)
        if (purchaseMessage != null) {
            Text(purchaseMessage, color = Muted, fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
            CreditStatCard("Images", "1 credit", Icons.Default.Star, Purple, Modifier.weight(1f))
            CreditStatCard("Videos", "5 credits", Icons.Default.Bolt, Pink, Modifier.weight(1f))
        }

        if (rewardMessage != null) {
            Surface(
                onClick = onClearRewardMessage,
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = Lime.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Lime.copy(alpha = 0.34f))
            ) {
                Text(
                    text = rewardMessage,
                    color = Lime,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }

        Surface(
            onClick = { onNavigate(Screen.Subscription.route) },
            modifier = Modifier.fillMaxWidth().height(74.dp),
            shape = CardShape,
            color = CredCard,
            border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.72f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    AccentIcon(Icons.Default.CreditCard, Cyan)
                    Column {
                        Text(stringResource(com.deep.lumoraai.R.string.ui_subscription_plans), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(com.deep.lumoraai.R.string.ui_monthly_refills_and_pro_tools), color = Muted, fontSize = 11.sp)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
            }
        }

        CreditRewardsSection(
            rewards = rewards,
            isRewardBusy = isRewardBusy,
            checkInDayIndex = checkInDayIndex,
            onClaimReward = { rewardId ->
                if (rewardId == "spin") {
                    showSpinWheel.value = true
                } else {
                    onClaimReward(rewardId)
                }
            }
        )

        Text(stringResource(com.deep.lumoraai.R.string.ui_top_up), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        CreditPackageCard("Starter", "50 credits", "$4.99", Purple, onBuy = { onBuy(50) })
        CreditPackageCard("Creator", "150 credits", "$12.99", Lime, highlighted = true, badge = "Popular", onBuy = { onBuy(150) })
        CreditPackageCard("Studio", "500 credits", "$39.99", Cyan, badge = "Best for video", onBuy = { onBuy(500) })

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun SpinWheelDialog(
    reward: CreditRewardUi,
    isRewardBusy: Boolean,
    onDismiss: () -> Unit,
    onSpin: () -> Unit,
) {
    val wheelColors = listOf(Color(0xFF222B42), Lime, Cyan, Purple, Pink, Color(0xFF7D86FF))
    val prizeLabels = listOf("Better\nluck", "+2", "+2", "+10", "+25", "+50")
    var spinning by remember { mutableStateOf(false) }
    var rotationTarget by remember { mutableStateOf(0f) }
    val wheelRotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        finishedListener = {
            if (spinning) {
                spinning = false
                onSpin()
            }
        },
        label = "spinWheelRotation"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = CredCard,
            border = BorderStroke(1.dp, Lime.copy(alpha = 0.38f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(com.deep.lumoraai.R.string.ui_spin_the_wheel), color = Color.White, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.ExtraBold)
                        Text(stringResource(com.deep.lumoraai.R.string.ui_1_free_spin_resets_every_week), color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(com.deep.lumoraai.R.string.ui_close), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(contentAlignment = Alignment.Center) {
                    Canvas(
                        modifier = Modifier
                            .size(228.dp)
                            .graphicsLayer(rotationZ = wheelRotation)
                    ) {
                        val sweep = 360f / wheelColors.size
                        wheelColors.forEachIndexed { index, color ->
                            drawArc(
                                color = color.copy(alpha = if (reward.isAvailable) 0.92f else 0.35f),
                                startAngle = -90f + index * sweep,
                                sweepAngle = sweep - 1.5f,
                                useCenter = true
                            )
                        }
                        val radius = size.minDimension * 0.34f
                        prizeLabels.forEachIndexed { index, label ->
                            val sliceAngle = -90f + index * sweep + sweep / 2f
                            val radians = Math.toRadians(sliceAngle.toDouble())
                            val textX = center.x + cos(radians).toFloat() * radius
                            val textY = center.y + sin(radians).toFloat() * radius
                            val textPaint = Paint().apply {
                                isAntiAlias = true
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                                textSize = if (label.contains('\n')) 11.sp.toPx() else 15.sp.toPx()
                                color = when (index) {
                                    1, 2 -> Color.Black.toArgb()
                                    else -> Color.White.toArgb()
                                }
                            }
                            drawContext.canvas.nativeCanvas.save()
                            drawContext.canvas.nativeCanvas.rotate(sliceAngle + 90f, textX, textY)
                            val lines = label.split('\n')
                            val lineOffset = if (lines.size > 1) textPaint.textSize * 0.42f else 0f
                            lines.forEachIndexed { lineIndex, line ->
                                drawContext.canvas.nativeCanvas.drawText(
                                    line,
                                    textX,
                                    textY + (lineIndex * textPaint.textSize) - lineOffset,
                                    textPaint
                                )
                            }
                            drawContext.canvas.nativeCanvas.restore()
                        }
                        drawCircle(color = CredBackground, radius = size.minDimension * 0.28f)
                        drawCircle(color = Lime.copy(alpha = 0.16f), radius = size.minDimension * 0.2f)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 2.dp)
                            .size(width = 28.dp, height = 36.dp)
                            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Pink))
                    }
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(CredCard)
                            .border(1.dp, Lime.copy(alpha = 0.52f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Lime, modifier = Modifier.size(34.dp))
                    }
                }

                Button(
                    onClick = {
                        if (!spinning) {
                            spinning = true
                            rotationTarget += 1440f + Random.nextInt(120, 480)
                        }
                    },
                    enabled = reward.isAvailable && !isRewardBusy && !spinning,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Lime,
                        disabledContainerColor = CredStroke,
                        disabledContentColor = Muted
                    )
                ) {
                    Text(
                        when {
                            spinning -> "Spinning..."
                            reward.isAvailable -> "Spin Weekly Wheel"
                            else -> "Weekly Spin Used"
                        },
                        color = if (reward.isAvailable) Color.Black else Muted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    "Odds: better luck 40%, +2 credits 30%, +10 credits 15%, +25 credits 5%, +50 credits 1%.",
                    color = Muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun CreditRewardsSection(
    rewards: List<CreditRewardUi>,
    isRewardBusy: Boolean,
    checkInDayIndex: Int,
    onClaimReward: (String) -> Unit,
) {
    val spinReward = rewards.firstOrNull { it.id == "spin" }
    val checkInReward = rewards.firstOrNull { it.id == "check_in" }
    val otherRewards = rewards.filterNot { it.id == "spin" || it.id == "check_in" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CredCard,
        border = BorderStroke(1.dp, Lime.copy(alpha = 0.26f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(stringResource(com.deep.lumoraai.R.string.ui_daily_task), color = Color.White, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(com.deep.lumoraai.R.string.ui_open_check_in_spin_and_earn_verified_rewards), color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
            }
            Text(stringResource(com.deep.lumoraai.R.string.ui_live_2), color = Lime, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
        }

            if (spinReward != null) {
                SpinRewardCard(
                    reward = spinReward,
                    enabled = spinReward.isAvailable && !isRewardBusy,
                    onClick = { onClaimReward(spinReward.id) }
                )
            }

            if (checkInReward != null) {
                CheckInTrackCard(
                    reward = checkInReward,
                    currentDayIndex = checkInDayIndex,
                    enabled = checkInReward.isAvailable && !isRewardBusy,
                    onClick = { onClaimReward(checkInReward.id) }
                )
            }

            RewardSummaryGrid(rewards = otherRewards)
        }
    }
}

@Composable
private fun SpinRewardCard(
    reward: CreditRewardUi,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(112.dp),
        shape = CardShape,
        color = Color(0xFF131D34),
        border = BorderStroke(1.dp, Lime.copy(alpha = if (reward.isAvailable) 0.56f else 0.22f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val colors = listOf(Lime, Purple, Cyan, Pink, Color(0xFF7D86FF), Muted)
                    val sweep = 360f / colors.size
                    colors.forEachIndexed { index, color ->
                        drawArc(color = color.copy(alpha = 0.9f), startAngle = -90f + index * sweep, sweepAngle = sweep - 2f, useCenter = true)
                    }
                    drawCircle(color = CredCard, radius = size.minDimension * 0.23f)
                }
                Icon(Icons.Default.Star, contentDescription = null, tint = Lime, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(reward.title, color = Color.White, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(com.deep.lumoraai.R.string.ui_tap_to_open_the_weekly_wheel_rewards_50_25_10_2_2_or_better_luck), color = Muted, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(reward.actionLabel, color = if (reward.isAvailable) Lime else Muted, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun CheckInTrackCard(
    reward: CreditRewardUi,
    currentDayIndex: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color(0xFF131D34),
        border = BorderStroke(1.dp, Purple.copy(alpha = if (reward.isAvailable) 0.5f else 0.22f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                AccentIcon(Icons.Default.CheckCircle, Purple)
                Column(modifier = Modifier.weight(1f)) {
                    Text(reward.title, color = Color.White, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(com.deep.lumoraai.R.string.ui_weekly_credits_1_1_2_2_2_3_4), color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
                }
                Button(
                    onClick = onClick,
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple, disabledContainerColor = CredStroke, disabledContentColor = Muted),
                    modifier = Modifier.height(38.dp).widthIn(min = 72.dp)
                ) {
                    Text(reward.actionLabel, color = if (enabled) Color.White else Muted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(1, 1, 2, 2, 2, 3, 4).forEachIndexed { index, amount ->
                    DayRewardPill(
                        day = index + 1,
                        amount = amount,
                        selected = index == currentDayIndex,
                        claimed = index < currentDayIndex,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayRewardPill(
    day: Int,
    amount: Int,
    selected: Boolean,
    claimed: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = when {
        selected -> Lime
        claimed -> Cyan
        else -> Muted
    }
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (selected) 0.18f else 0.08f))
            .border(1.dp, color.copy(alpha = if (selected) 0.46f else 0.16f), RoundedCornerShape(12.dp))
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("D$day", color = color, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
        Text("+$amount", color = Color.White, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun RewardSummaryGrid(rewards: List<CreditRewardUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rewards.chunked(2).forEach { rowRewards ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowRewards.forEachIndexed { index, reward ->
                    RewardSummaryTile(
                        reward = reward,
                        accent = rewardAccent(index),
                        icon = rewardIcon(reward.id),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowRewards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RewardSummaryTile(
    reward: CreditRewardUi,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF10192D),
        border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                }
                Text(reward.rewardLabel, color = accent, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            Column {
                Text(reward.title, color = Color.White, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(reward.actionLabel, color = Muted, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CreditRewardCard(
    reward: CreditRewardUi,
    accent: Color,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, if (reward.isAvailable) accent.copy(alpha = 0.5f) else CredStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            AccentIcon(icon, accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        reward.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        reward.rewardLabel,
                        color = accent,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Text(
                    reward.subtitle,
                    color = Muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (reward.isAutomatic) {
                Text(reward.actionLabel, color = accent, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.ExtraBold)
            } else {
                Button(
                    onClick = onClick,
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        disabledContainerColor = CredStroke,
                        disabledContentColor = Muted
                    ),
                    modifier = Modifier.height(38.dp).widthIn(min = 72.dp)
                ) {
                    Text(
                        reward.actionLabel,
                        color = if (enabled && accent == Lime) Color.Black else if (enabled) Color.White else Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun rewardAccent(index: Int): Color =
    when (index % 4) {
        0 -> Lime
        1 -> Purple
        2 -> Cyan
        else -> Pink
    }

private fun rewardIcon(id: String): ImageVector =
    when (id) {
        "daily_reset" -> Icons.Default.Refresh
        "referral", "social_share" -> Icons.Default.Share
        "email_login" -> Icons.Default.CreditCard
        "signup" -> Icons.Default.AutoAwesome
        else -> Icons.Default.Star
    }

@Composable
private fun PageTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.deep.lumoraai.R.string.ui_back), tint = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, color = Color.White, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun BalanceHero(balanceLabel: String, isDeveloperMode: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(176.dp),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, Lime.copy(alpha = 0.32f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(com.deep.lumoraai.R.string.ui_current_balance), color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(balanceLabel, color = Color.White, fontSize = 40.sp, lineHeight = 44.sp, fontWeight = FontWeight.ExtraBold)
                Text(if (isDeveloperMode) "Developer mode active" else "LUM credits available", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Lime.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Lime, modifier = Modifier.size(31.dp))
            }
        }
    }
}

@Composable
private fun CreditStatCard(title: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(82.dp),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccentIcon(icon, accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = Color.White, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, color = Muted, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CreditPackageCard(
    title: String,
    credits: String,
    price: String,
    accent: Color,
    highlighted: Boolean = false,
    badge: String? = null,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, if (highlighted) Lime.copy(alpha = 0.72f) else CredStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                AccentIcon(Icons.Default.CheckCircle, accent)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(credits, color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
                    if (badge != null) {
                        Text(
                            badge,
                            color = if (highlighted) Color.Black else accent,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (highlighted) Lime else accent.copy(alpha = 0.14f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.height(40.dp).widthIn(min = 82.dp)
            ) {
                Text(price, color = if (accent == Lime) Color.Black else Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AccentIcon(icon: ImageVector, accent: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
    }
}
