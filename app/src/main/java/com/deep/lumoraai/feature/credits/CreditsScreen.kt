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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
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
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.style.TextAlign
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
    val ads = LocalAdsManager.current
    val adActivity = rememberCurrentActivity()
    val rewardAmount = LocalAdsConfigStore.current?.current?.rewardCreditsAmount ?: 2
    // Show a rewarded ad; grant credits ONLY on the genuine onReward callback.
    val onWatchAdForCredits: () -> Unit = {
        ads?.showRewarded(
            activity = adActivity,
            placement = AdPlacement.REWARD_CREDITS,
            onReward = { viewModel.addRewardedCredits(rewardAmount) },
            onClosed = {},
        )
    }
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
                    spinResult = uiState.spinResult,
                    onBack = onBack,
                    onClaimReward = { viewModel.claimReward(it) },
                    onClearRewardMessage = { viewModel.clearRewardMessage() },
                    onClearSpinResult = { viewModel.clearSpinResult() },
                    onNavigate = onNavigate,
                    rewardAdAmount = rewardAmount,
                    showWatchAd = ads != null && !uiState.isDeveloperMode,
                    onWatchAdForCredits = onWatchAdForCredits,
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
    spinResult: SpinResult?,
    onBack: () -> Unit,
    onClaimReward: (String) -> Unit,
    onClearRewardMessage: () -> Unit,
    onClearSpinResult: () -> Unit,
    onNavigate: (String) -> Unit,
    rewardAdAmount: Int = 2,
    showWatchAd: Boolean = false,
    onWatchAdForCredits: () -> Unit = {},
) {
    val balanceLabel = if (isDeveloperMode || credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) stringResource(com.deep.lumoraai.R.string.ui_unlimited) else "$credits"
    val showSpinWheel = remember { mutableStateOf(false) }
    val spinReward = rewards.firstOrNull { it.id == "spin" }

    if (showSpinWheel.value && spinReward != null) {
        SpinWheelDialog(
            reward = spinReward,
            isRewardBusy = isRewardBusy,
            spinResult = spinResult,
            onDismiss = {
                showSpinWheel.value = false
                onClearSpinResult()
            },
            onSpin = { onClaimReward(spinReward.id) },
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
        PageTopBar(title = "Reward Center", onBack = onBack)
        RewardCenterHero(
            balanceLabel = balanceLabel,
            isDeveloperMode = isDeveloperMode,
            onSubscribe = { onNavigate(Screen.Subscription.route) },
            onGenerate = { onNavigate(Screen.Home.route) },
        )
        if (purchaseMessage != null) {
            Text(purchaseMessage, color = Muted, fontSize = 12.sp)
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

        if (showWatchAd) {
            WatchAdForCreditsCard(amount = rewardAdAmount, onClick = onWatchAdForCredits)
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun SpinWheelDialog(
    reward: CreditRewardUi,
    isRewardBusy: Boolean,
    spinResult: SpinResult?,
    onDismiss: () -> Unit,
    onSpin: () -> Unit,
) {
    val wheelColors = listOf(Color(0xFF222B42), Lime, Cyan, Purple, Pink, Color(0xFF7D86FF))
    // Prize amount per segment (index-aligned with prizeLabels below).
    val prizeAmounts = listOf(0, 2, 2, 10, 25, 50)
    val prizeLabels = listOf(
        stringResource(com.deep.lumoraai.R.string.ui_spin_prize_better_luck),
        "+2",
        "+2",
        "+10",
        "+25",
        "+50"
    )
    val sweepDeg = 360f / wheelColors.size

    // spinning = backend call in flight (waiting for the authoritative result);
    // landed = the wheel finished animating to the winning segment.
    var spinning by remember { mutableStateOf(false) }
    var landed by remember { mutableStateOf(false) }
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    // Track which spinResult we've already animated so we react once per spin.
    var handledNonce by remember { mutableStateOf<Long?>(null) }
    var landedAmount by remember { mutableStateOf<Int?>(null) }

    val wheelRotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
        finishedListener = {
            if (spinning) {
                spinning = false
                landed = true
            }
        },
        label = "spinWheelRotation"
    )

    // When the server result arrives, spin the wheel so it LANDS on the segment
    // matching the awarded amount (the visual now always matches the credits).
    LaunchedEffect(spinResult?.nonce) {
        val result = spinResult ?: return@LaunchedEffect
        if (handledNonce == result.nonce) return@LaunchedEffect
        handledNonce = result.nonce
        // Pick the segment whose amount equals the award (first match).
        val targetIndex = prizeAmounts.indexOf(result.creditsAwarded).let { if (it >= 0) it else 0 }
        // The pointer is at the top (-90°). Segment i center sits at
        // (-90 + i*sweep + sweep/2) in wheel space; rotate so that lands at -90.
        val segmentCenter = targetIndex * sweepDeg + sweepDeg / 2f
        val currentMod = ((rotationTarget % 360f) + 360f) % 360f
        val needed = (360f - segmentCenter - currentMod + 360f) % 360f
        rotationTarget += 360f * 5 + needed // 5 full turns then settle on target
        landedAmount = result.creditsAwarded
    }

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

                // Result banner shown after the wheel lands, so the user clearly
                // sees what they won (matching the segment the wheel stopped on).
                if (landed && landedAmount != null) {
                    val amount = landedAmount ?: 0
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Lime.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Lime.copy(alpha = 0.4f)),
                    ) {
                        Text(
                            text = if (amount > 0) {
                                stringResource(com.deep.lumoraai.R.string.ui_spin_you_won, amount)
                            } else {
                                stringResource(com.deep.lumoraai.R.string.ui_spin_better_luck_result)
                            },
                            color = Lime,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        )
                    }
                }

                Button(
                    onClick = {
                        if (landed) {
                            onDismiss()
                        } else if (!spinning && reward.isAvailable) {
                            // Ask the server for the authoritative result; the wheel
                            // lands on the matching segment when it arrives.
                            spinning = true
                            onSpin()
                        }
                    },
                    enabled = landed || (reward.isAvailable && !isRewardBusy && !spinning),
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
                            landed -> stringResource(com.deep.lumoraai.R.string.ui_done)
                            spinning -> stringResource(com.deep.lumoraai.R.string.ui_spinning)
                            reward.isAvailable -> stringResource(com.deep.lumoraai.R.string.ui_spin_weekly_wheel)
                            else -> stringResource(com.deep.lumoraai.R.string.ui_weekly_spin_used)
                        },
                        color = if (landed || reward.isAvailable) Color.Black else Muted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    stringResource(com.deep.lumoraai.R.string.ui_odds_better_luck_40_2_credits_30_10_credits_15_25_credits_5_50_credits),
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

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("Daily Check-in", color = Color.White, fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Check in and earn verified rewards across the week.", color = Muted, fontSize = 13.sp, lineHeight = 17.sp)
            }
            Text(stringResource(com.deep.lumoraai.R.string.ui_live_2), color = Lime, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
        }

            if (checkInReward != null) {
                CheckInTrackCard(
                    reward = checkInReward,
                    currentDayIndex = checkInDayIndex,
                    enabled = checkInReward.isAvailable && !isRewardBusy,
                    onClick = { onClaimReward(checkInReward.id) }
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(com.deep.lumoraai.R.string.ui_daily_task), color = Color.White, fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold)
            if (spinReward != null) {
                DailyTaskRow(
                    reward = spinReward,
                    icon = Icons.Default.Star,
                    accent = Lime,
                    enabled = spinReward.isAvailable && !isRewardBusy,
                    onClick = { onClaimReward(spinReward.id) },
                )
            }
            otherRewards.forEachIndexed { index, reward ->
                DailyTaskRow(
                    reward = reward,
                    icon = rewardIcon(reward.id),
                    accent = rewardAccent(index),
                    enabled = reward.isAvailable && !isRewardBusy,
                    onClick = { onClaimReward(reward.id) },
                )
            }
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
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color(0xFF131D34),
        border = BorderStroke(1.dp, Lime.copy(alpha = if (reward.isAvailable) 0.56f else 0.22f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
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
                Text(stringResource(com.deep.lumoraai.R.string.ui_tap_to_open_the_weekly_wheel_rewards_50_25_10_2_2_or_better_luck), color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F80FF),
                disabledContainerColor = CredStroke,
                disabledContentColor = Muted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text(
                reward.actionLabel,
                color = if (enabled) Color.White else Muted,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
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
            .defaultMinSize(minHeight = 92.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (selected) 0.16f else 0.10f),
                        color.copy(alpha = if (selected) 0.20f else 0.08f)
                    )
                )
            )
            .border(1.dp, color.copy(alpha = if (selected) 0.58f else 0.14f), RoundedCornerShape(7.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            stringResource(com.deep.lumoraai.R.string.ui_day_short_format, day),
            color = color,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
        }
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
        modifier = modifier.defaultMinSize(minHeight = 82.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF10192D),
        border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
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
private fun PageTopBar(title: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.deep.lumoraai.R.string.ui_back), tint = Color.White)
        }
        Text(
            title,
            color = Color.White,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun RewardCenterHero(
    balanceLabel: String,
    isDeveloperMode: Boolean,
    onSubscribe: () -> Unit,
    onGenerate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF171B2E), Color(0xFF261C44), Color(0xFF131524))
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(com.deep.lumoraai.R.string.ui_monthly_refills_and_pro_tools),
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            Button(
                onClick = onSubscribe,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                modifier = Modifier.height(42.dp).widthIn(min = 118.dp),
            ) {
                Text(stringResource(com.deep.lumoraai.R.string.ui_subscribe_now), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1A1642), Color(0xFF29205A), Color(0xFF10192D))
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Purple.copy(alpha = 0.18f),
                    radius = size.minDimension * 0.42f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.84f, size.height * 0.47f),
                )
                drawCircle(
                    color = Lime.copy(alpha = 0.16f),
                    radius = size.minDimension * 0.34f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.87f, size.height * 0.55f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(com.deep.lumoraai.R.string.ui_current_balance), color = Color.White.copy(alpha = 0.86f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Lime, modifier = Modifier.size(34.dp))
                    Text(balanceLabel, color = Color.White, fontSize = 42.sp, lineHeight = 46.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(
                    if (isDeveloperMode) stringResource(com.deep.lumoraai.R.string.ui_developer_mode_active) else stringResource(com.deep.lumoraai.R.string.ui_lum_credits_available),
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Surface(
                onClick = onGenerate,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp),
                shape = RoundedCornerShape(50),
                color = Lime,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
            ) {
                Text(
                    stringResource(com.deep.lumoraai.R.string.ui_generate_now),
                    color = CredBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                )
            }
        }
    }
}

@Composable
private fun DailyTaskRow(
    reward: CreditRewardUi,
    icon: ImageVector,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.95f), accent.copy(alpha = 0.55f), Color(0xFF2F80FF))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(29.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        reward.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    RewardAmountPill(reward.rewardLabel)
                }
                Text(reward.subtitle, color = Muted.copy(alpha = 0.82f), fontSize = 13.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = if (enabled) 0.08f else 0.045f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.widthIn(min = 86.dp),
            ) {
                Text(
                    reward.actionLabel,
                    color = if (enabled) Color(0xFF2F80FF) else Muted.copy(alpha = 0.72f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun RewardAmountPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, color = Lime, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CreditStatCard(title: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 82.dp),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
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
private fun WatchAdForCreditsCard(amount: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, Lime.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccentIcon(Icons.Default.Bolt, Lime)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    stringResource(com.deep.lumoraai.R.string.ui_watch_ad_for_credits),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(com.deep.lumoraai.R.string.ui_watch_ad_for_credits_subtitle),
                    color = Muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Lime)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("+$amount", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
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
