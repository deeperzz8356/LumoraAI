package com.deep.lumoraai.feature.credits

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate

private val RewardBackground = Color(0xFF080D18)
private val RewardSurface = Color(0xFF111827)
private val RewardSurfaceRaised = Color(0xFF172033)
private val RewardStroke = Color(0xFF273249)
private val RewardLime = Color(0xFFD6FF3F)
private val RewardViolet = Color(0xFF8B74FF)
private val RewardText = Color(0xFFF7F8FA)
private val RewardMuted = Color(0xFF98A5BC)
private val RewardShape = RoundedCornerShape(20.dp)

@Composable
fun CreditsScreen(
    uiState: CreditsUiState,
    viewModel: CreditsViewModel,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val ads = LocalAdsManager.current
    val activity = rememberCurrentActivity()
    val rewardAmount = LocalAdsConfigStore.current?.current?.rewardCreditsAmount ?: 2

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = RewardBackground,
        bottomBar = { BottomNavigationBar(emptyList(), "credits", onNavigate) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RewardBackground)
                .padding(innerPadding),
        ) {
            when (uiState) {
                CreditsUiState.Loading -> AppLoadingScreen()
                is CreditsUiState.Error -> AppErrorScreen(message = uiState.message)
                is CreditsUiState.Success -> RewardCenterContent(
                    uiState = uiState,
                    rewardAdAmount = rewardAmount,
                    showWatchAd = ads != null && !uiState.isDeveloperMode,
                    onBack = onBack,
                    onNavigate = onNavigate,
                    onClaimReward = viewModel::claimReward,
                    onClearRewardMessage = viewModel::clearRewardMessage,
                    onClearSpinResult = viewModel::clearSpinResult,
                    onWatchAdForCredits = {
                        ads?.showRewarded(
                            activity = activity,
                            placement = AdPlacement.REWARD_CREDITS,
                            onReward = { viewModel.addRewardedCredits(rewardAmount) },
                            onClosed = {},
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RewardCenterContent(
    uiState: CreditsUiState.Success,
    rewardAdAmount: Int,
    showWatchAd: Boolean,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onClearRewardMessage: () -> Unit,
    onClearSpinResult: () -> Unit,
    onWatchAdForCredits: () -> Unit,
) {
    val balance = if (
        uiState.isDeveloperMode || uiState.credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY
    ) "Unlimited" else uiState.credits.toString()
    var showSpinWheel by remember { mutableStateOf(false) }
    val spinReward = uiState.rewards.firstOrNull { it.id == "spin" }

    if (showSpinWheel && spinReward != null) {
        SpinWheelDialog(
            reward = spinReward,
            isRewardBusy = uiState.isRewardBusy,
            spinResult = uiState.spinResult,
            onDismiss = {
                showSpinWheel = false
                onClearSpinResult()
            },
            onSpin = { onClaimReward(spinReward.id) },
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val sidePadding = if (maxWidth >= 600.dp) 32.dp else 18.dp
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = sidePadding)
                .padding(top = 18.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            RewardTopBar(onBack)
            BalanceHero(
                balance = balance,
                isDeveloperMode = uiState.isDeveloperMode,
                onCreate = { onNavigate(Screen.Home.route) },
                onSubscribe = { onNavigate(Screen.Subscription.route) },
            )
            uiState.purchaseMessage?.let { RewardMessage(it) }
            uiState.rewardMessage?.let { RewardMessage(it, onClearRewardMessage) }
            EarnCreditsSection(
                rewards = uiState.rewards,
                currentDay = uiState.checkInDayIndex,
                isBusy = uiState.isRewardBusy,
                onClaim = { reward ->
                    if (reward.id == "spin") showSpinWheel = true else onClaimReward(reward.id)
                },
            )
            if (showWatchAd) {
                WatchAdCard(rewardAdAmount, !uiState.isRewardBusy, onWatchAdForCredits)
            }
        }
    }
}

@Composable
private fun RewardTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back", tint = RewardText)
        }
        Column {
            Text(
                "REWARDS · LUMORA",
                color = RewardLime,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
            Text(
                "Reward Center",
                color = RewardText,
                fontSize = 28.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.7).sp,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
}

@Composable
private fun BalanceHero(
    balance: String,
    isDeveloperMode: Boolean,
    onCreate: () -> Unit,
    onSubscribe: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = RewardSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shadowElevation = 14.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF202548), Color(0xFF161B2E), Color(0xFF0E1625)),
                    ),
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    RewardViolet.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.68f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.94f, size.height * 0.12f),
                )
                drawCircle(
                    RewardLime.copy(alpha = 0.12f),
                    radius = size.minDimension * 0.42f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.90f, size.height * 0.36f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()),
                )
            }
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "AVAILABLE BALANCE",
                            color = RewardMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = RewardLime, modifier = Modifier.size(30.dp))
                            Text(
                                balance,
                                color = RewardText,
                                fontSize = if (balance.length > 5) 38.sp else 52.sp,
                                lineHeight = 54.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.8).sp,
                            )
                        }
                        Text(
                            if (isDeveloperMode) "Developer access active" else "Lumora credits ready to use",
                            color = RewardMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RewardLime.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, RewardLime.copy(alpha = 0.28f)),
                    ) {
                        Text(
                            "ACTIVE",
                            color = RewardLime,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCreate,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RewardLime),
                    ) {
                        Text("Start creating", color = RewardBackground, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(7.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(17.dp))
                    }
                    TextButton(onClick = onSubscribe, modifier = Modifier.height(50.dp)) {
                        Text("Get Pro", color = RewardText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardMessage(message: String, onDismiss: (() -> Unit)? = null) {
    Surface(
        onClick = { onDismiss?.invoke() },
        enabled = onDismiss != null,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(15.dp),
        color = RewardLime.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, RewardLime.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Default.Check, null, tint = RewardLime, modifier = Modifier.size(18.dp))
            Text(message, color = RewardText, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EarnCreditsSection(
    rewards: List<CreditRewardUi>,
    currentDay: Int,
    isBusy: Boolean,
    onClaim: (CreditRewardUi) -> Unit,
) {
    val checkIn = rewards.firstOrNull { it.id == "check_in" }
    val spin = rewards.firstOrNull { it.id == "spin" }
    val otherRewards = rewards.filterNot { it.id == "check_in" || it.id == "spin" }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Earn more",
                    color = RewardText,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.semantics { heading() },
                )
                Text("Small actions, real credits.", color = RewardMuted, fontSize = 13.sp)
            }
            Text(
                "LIVE",
                color = RewardLime,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.3.sp,
            )
        }
        checkIn?.let {
            CheckInCard(it, currentDay, it.isAvailable && !isBusy) { onClaim(it) }
        }
        spin?.let {
            SpinCard(it, it.isAvailable && !isBusy) { onClaim(it) }
        }
        otherRewards.forEach { reward ->
            RewardTaskCard(
                reward = reward,
                icon = rewardIcon(reward.id),
                enabled = reward.isAvailable && !isBusy,
                onClick = { onClaim(reward) },
            )
        }
    }
}

@Composable
private fun CheckInCard(
    reward: CreditRewardUi,
    currentDay: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RewardShape,
        color = RewardSurface,
        border = BorderStroke(1.dp, RewardStroke.copy(alpha = 0.82f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("7-day streak", color = RewardText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(reward.subtitle, color = RewardMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                RewardAmount(reward.rewardLabel)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(1, 1, 2, 2, 2, 3, 4).forEachIndexed { index, amount ->
                    DayReward(index + 1, amount, index == currentDay, index < currentDay, Modifier.weight(1f))
                }
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RewardLime,
                    contentColor = RewardBackground,
                    disabledContainerColor = RewardStroke,
                    disabledContentColor = RewardMuted,
                ),
            ) {
                Text(reward.actionLabel, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun DayReward(
    day: Int,
    amount: Int,
    selected: Boolean,
    claimed: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = when {
        selected -> RewardLime
        claimed -> RewardViolet
        else -> RewardMuted
    }
    Column(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accent.copy(alpha = 0.14f) else RewardSurfaceRaised)
            .border(1.dp, accent.copy(alpha = if (selected) 0.48f else 0.12f), RoundedCornerShape(10.dp))
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("D$day", color = accent, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier.size(23.dp).clip(CircleShape).background(accent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (claimed) Icons.Default.Check else Icons.Default.AutoAwesome,
                null,
                tint = accent,
                modifier = Modifier.size(14.dp),
            )
        }
        Text("+$amount", color = RewardText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SpinCard(reward: CreditRewardUi, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RewardShape,
        color = RewardSurface,
        border = BorderStroke(1.dp, RewardLime.copy(alpha = if (reward.isAvailable) 0.38f else 0.14f)),
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MiniWheel(reward.isAvailable)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(reward.title, color = RewardText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    RewardAmount(reward.rewardLabel)
                }
                Text(reward.subtitle, color = RewardMuted, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = if (enabled) RewardLime else RewardMuted)
        }
    }
}

@Composable
private fun MiniWheel(enabled: Boolean, rotation: Float = 0f, size: Int = 68) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp)) {
        Canvas(Modifier.fillMaxSize().graphicsLayer(rotationZ = rotation)) {
            val colors = listOf(RewardLime, RewardViolet, RewardText, Color(0xFF44516B), RewardLime, RewardViolet)
            val sweep = 360f / colors.size
            colors.forEachIndexed { index, color ->
                drawArc(
                    color.copy(alpha = if (enabled) 0.92f else 0.28f),
                    startAngle = -90f + index * sweep,
                    sweepAngle = sweep - 2f,
                    useCenter = true,
                )
            }
            drawCircle(RewardSurface, radius = this.size.minDimension * 0.24f)
        }
        Icon(Icons.Default.Star, null, tint = RewardLime, modifier = Modifier.size((size * 0.28f).dp))
    }
}

@Composable
private fun RewardTaskCard(
    reward: CreditRewardUi,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = RewardSurface,
        border = BorderStroke(1.dp, RewardStroke.copy(alpha = 0.70f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(RewardLime.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = RewardLime, modifier = Modifier.size(23.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        reward.title,
                        color = RewardText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    RewardAmount(reward.rewardLabel)
                }
                Text(
                    reward.subtitle,
                    color = RewardMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                reward.actionLabel,
                color = if (enabled) RewardLime else RewardMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RewardAmount(text: String) {
    Text(
        text,
        color = RewardLime,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(RewardLime.copy(alpha = 0.10f))
            .padding(horizontal = 7.dp, vertical = 4.dp),
    )
}

@Composable
private fun WatchAdCard(amount: Int, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RewardShape,
        color = RewardLime,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(RewardBackground.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Bolt, null, tint = RewardBackground, modifier = Modifier.size(23.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Watch & earn", color = RewardBackground, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Text("A short ad adds credits instantly", color = RewardBackground.copy(alpha = 0.68f), fontSize = 11.sp)
            }
            Text("+$amount", color = RewardBackground, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
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
    val prizes = listOf(0, 2, 10, 25, 50, 2)
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    var handledNonce by remember { mutableStateOf<Long?>(null) }
    var spinning by remember { mutableStateOf(false) }
    var landed by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf<Int?>(null) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(2600, easing = FastOutSlowInEasing),
        finishedListener = {
            if (spinning) {
                spinning = false
                landed = true
            }
        },
        label = "rewardWheelRotation",
    )

    LaunchedEffect(spinResult?.nonce) {
        val result = spinResult ?: return@LaunchedEffect
        if (handledNonce == result.nonce) return@LaunchedEffect
        handledNonce = result.nonce
        val index = prizes.indexOf(result.creditsAwarded).coerceAtLeast(0)
        rotationTarget += 1800f + (360f - (index * 60f + 30f))
        won = result.creditsAwarded
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            color = RewardSurface,
            border = BorderStroke(1.dp, RewardLime.copy(alpha = 0.30f)),
            shadowElevation = 20.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Spin the wheel", color = RewardText, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(reward.subtitle, color = RewardMuted, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    TextButton(onClick = onDismiss) { Text("Close", color = RewardMuted) }
                }
                Box(contentAlignment = Alignment.TopCenter) {
                    MiniWheel(enabled = reward.isAvailable, rotation = rotation, size = 236)
                    Box(
                        modifier = Modifier
                            .size(width = 28.dp, height = 38.dp)
                            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                            .background(RewardText),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(RewardViolet))
                    }
                }
                if (landed && won != null) {
                    Text(
                        if (won == 0) "Better luck on your next spin" else "You won $won credits",
                        color = RewardLime,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(RewardLime.copy(alpha = 0.10f))
                            .padding(12.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
                Button(
                    onClick = {
                        if (landed) onDismiss() else if (!spinning) {
                            spinning = true
                            onSpin()
                        }
                    },
                    enabled = landed || (reward.isAvailable && !isRewardBusy && !spinning),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RewardLime,
                        contentColor = RewardBackground,
                        disabledContainerColor = RewardStroke,
                        disabledContentColor = RewardMuted,
                    ),
                ) {
                    Text(
                        when {
                            landed -> "Collect reward"
                            spinning -> "Finding your reward…"
                            else -> reward.actionLabel
                        },
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

private fun rewardIcon(id: String): ImageVector = when (id) {
    "daily_reset" -> Icons.Default.Refresh
    "referral", "social_share" -> Icons.Default.Share
    "email_login" -> Icons.Default.CreditCard
    "signup" -> Icons.Default.AutoAwesome
    else -> Icons.Default.Star
}
