package com.deep.lumoraai.feature.credits

import android.graphics.Color
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

private const val REWARD_SPIN = "spin"

@Composable
fun CreditsScreen(
    uiState: CreditsUiState,
    viewModel: CreditsViewModel,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val ads = LocalAdsManager.current
    val adActivity = rememberCurrentActivity()
    val rewardAmount = LocalAdsConfigStore.current?.current?.rewardCreditsAmount ?: 2
    var showSpinWheel by remember { mutableStateOf(false) }
    val onWatchAdForCredits: () -> Unit = {
        ads?.showRewarded(
            activity = adActivity,
            placement = AdPlacement.REWARD_CREDITS,
            onReward = { viewModel.addRewardedCredits(rewardAmount) },
            onClosed = {},
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ComposeColor(0xFF081020))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                CreditsScreenBinding.inflate(LayoutInflater.from(context)).apply {
                    scroll.isVerticalScrollBarEnabled = false
                    scroll.overScrollMode = View.OVER_SCROLL_NEVER
                    checkInScroll.setOnTouchListener { view, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN,
                            MotionEvent.ACTION_MOVE -> view.parent?.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP,
                            MotionEvent.ACTION_CANCEL -> view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        false
                    }
                    root.tag = this
                }.root
            },
            update = { root ->
                val binding = root.tag as CreditsScreenBinding
                binding.backButton.setOnClickListener { onBack() }

                when (uiState) {
                    CreditsUiState.Loading -> binding.showState("Loading", "Reading your credit balance.")
                    is CreditsUiState.Error -> binding.showState("Error", uiState.message)
                    is CreditsUiState.Success -> {
                        binding.renderRewardCenter(
                            state = uiState,
                            rewardAdEnabled = ads != null && !uiState.isDeveloperMode,
                            rewardAdAmount = rewardAmount,
                            onSubscribe = { onNavigate(Screen.Subscription.route) },
                            onGenerate = { onNavigate(Screen.Home.route) },
                            onReward = {
                                if (it == REWARD_SPIN) showSpinWheel = true else viewModel.claimReward(it)
                            },
                            onMessageDismiss = {
                                viewModel.clearRewardMessage()
                                viewModel.clearSpinResult()
                            },
                            onWatchAd = onWatchAdForCredits,
                        )
                    }
                }
            }
        )
    }

    if (uiState is CreditsUiState.Success && showSpinWheel) {
        SpinWheelDialog(
            state = uiState,
            onSpin = { viewModel.claimReward(REWARD_SPIN) },
            onDismiss = {
                showSpinWheel = false
                viewModel.clearSpinResult()
            },
        )
    }
}

private fun CreditsScreenBinding.showState(title: String, subtitle: String) {
    stateCard.visibility = View.VISIBLE
    successContent.visibility = View.GONE
    stateTitle.text = title
    stateSubtitle.text = subtitle
}

private fun CreditsScreenBinding.renderRewardCenter(
    state: CreditsUiState.Success,
    rewardAdEnabled: Boolean,
    rewardAdAmount: Int,
    onSubscribe: () -> Unit,
    onGenerate: () -> Unit,
    onReward: (String) -> Unit,
    onMessageDismiss: () -> Unit,
    onWatchAd: () -> Unit,
) {
    stateCard.visibility = View.GONE
    successContent.visibility = View.VISIBLE

    val balance = if (state.isDeveloperMode || state.credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        state.credits.toString()
    }

    balanceText.text = balance
    balanceText.textSize = if (balance.length > 8) 28f else 34f
    balanceSubtitle.text = if (state.isDeveloperMode) "Developer mode active" else "Available credits"
    subscribeButton.setOnClickListener { onSubscribe() }
    generateButton.setOnClickListener { onGenerate() }

    noticeContainer.removeAllViews()
    state.purchaseMessage?.let { noticeContainer.addNotice("Purchase", it, onMessageDismiss) }
    state.rewardMessage?.let { noticeContainer.addNotice("Reward", it, onMessageDismiss) }

    checkInDaysRow.removeAllViews()
    val rewards = listOf(1, 1, 2, 2, 2, 3, 4)
    rewards.forEachIndexed { index, amount ->
        checkInDaysRow.addView(
            checkInDaysRow.dayTile(index, amount, state.checkInDayIndex == index),
            LinearLayout.LayoutParams(checkInDaysRow.dp(66), checkInDaysRow.dp(80)).apply {
                marginEnd = checkInDaysRow.dp(8)
            }
        )
    }

    val checkIn = state.rewards.firstOrNull { it.id == "check_in" }
    checkInButton.text = checkIn?.actionLabel ?: "Claim"
    val checkInAvailable = checkIn?.isAvailable == true && !state.isRewardBusy
    checkInButton.setTextColor(
        if (checkIn?.isAvailable == true) Color.rgb(8, 16, 32) else Color.rgb(156, 165, 186)
    )
    checkInButton.background = checkInButton.pill(
        if (checkIn?.isAvailable == true) Color.rgb(214, 255, 47) else Color.rgb(21, 31, 51),
        checkInButton.dp(27)
    )
    checkInButton.alpha = if (state.isRewardBusy) 0.55f else 1f
    checkInButton.isEnabled = checkInAvailable
    checkInButton.isClickable = checkInAvailable
    checkInButton.setOnClickListener { if (checkInButton.isEnabled) onReward("check_in") }

    dailyRewardsList.removeAllViews()
    state.rewards.filterNot { it.id == "check_in" }.forEach { reward ->
        dailyRewardsList.addRewardRow(reward, state.isRewardBusy, onReward)
    }

    rewardAdButton.visibility = if (rewardAdEnabled) View.VISIBLE else View.GONE
    rewardAdButton.text = "WATCH AD FOR $rewardAdAmount CREDITS"
    rewardAdButton.isEnabled = !state.isRewardBusy
    rewardAdButton.alpha = if (state.isRewardBusy) 0.5f else 1f
    rewardAdButton.setOnClickListener { if (rewardAdButton.isEnabled) onWatchAd() }
}

@Composable
private fun SpinWheelDialog(
    state: CreditsUiState.Success,
    onSpin: () -> Unit,
    onDismiss: () -> Unit,
) {
    val result = state.spinResult
    val targetRotation = when {
        state.isRewardBusy -> 360f
        result == null -> 0f
        result.creditsAwarded >= 50 -> 720f + 18f
        result.creditsAwarded >= 25 -> 720f + 78f
        result.creditsAwarded >= 10 -> 720f + 138f
        result.creditsAwarded > 0 -> 720f + 258f
        else -> 720f + 318f
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
    val colors = listOf(
        ComposeColor(0xFFD6FF2F),
        ComposeColor(0xFF9C63FF),
        ComposeColor(0xFF30D8DE),
        ComposeColor(0xFFFF3D9D),
        ComposeColor(0xFF273249),
        ComposeColor(0xFF5DD96B),
    )
    Canvas(modifier = Modifier.size(214.dp).rotate(rotation)) {
        val sweep = 360f / colors.size
        colors.forEachIndexed { index, color ->
            drawArc(
                color = color,
                startAngle = -90f + index * sweep,
                sweepAngle = sweep,
                useCenter = true,
            )
        }
        drawCircle(
            color = ComposeColor.White.copy(alpha = 0.22f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f, cap = StrokeCap.Round),
        )
    }
}

private fun LinearLayout.renderRewardCenter(
    state: CreditsUiState.Success,
    rewardAdEnabled: Boolean,
    rewardAdAmount: Int,
    onSubscribe: () -> Unit,
    onGenerate: () -> Unit,
    onReward: (String) -> Unit,
    onMessageDismiss: () -> Unit,
    onWatchAd: () -> Unit,
) {
    // Do not override padding here — the shell XML already defines consistent
    // paddingStart/End/Top/Bottom on the content LinearLayout.
    val balance = if (state.isDeveloperMode || state.credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        state.credits.toString()
    }
    addHeroCard(balance, state.isDeveloperMode, onSubscribe, onGenerate)
    state.purchaseMessage?.let { addNotice("Purchase", it, onMessageDismiss) }
    state.rewardMessage?.let { addNotice("Reward", it, onMessageDismiss) }

    val checkIn = state.rewards.firstOrNull { it.id == "check_in" }
    addCheckInSection(state, checkIn, onReward)
    addDailyTaskSection(state, onReward)
    if (rewardAdEnabled) {
        addRewardAdButton(rewardAdAmount, state.isRewardBusy, onWatchAd)
    }
}

private fun LinearLayout.addHeroCard(
    balance: String,
    developerMode: Boolean,
    onSubscribe: () -> Unit,
    onGenerate: () -> Unit,
) {
    val card = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = roundedGradient(
            intArrayOf(Color.rgb(29, 24, 51), Color.rgb(42, 31, 92), Color.rgb(17, 26, 45)),
            radius = dp(20),
            strokeColor = Color.rgb(55, 64, 91),
            strokeWidth = dp(1)
        )
    }
    val top = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    top.addView(TextView(context).apply {
        text = "Monthly refills and\nPro tools"
        setTextColor(Color.WHITE)
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
        maxLines = 2
    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    top.addView(TextView(context).apply {
        text = "Subscribe Now"
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.08f
        background = roundedGradient(intArrayOf(Color.rgb(154, 92, 245), Color.rgb(174, 88, 255)), dp(23))
        setOnClickListener { onSubscribe() }
        isClickable = true
        minWidth = dp(118)
        setPadding(dp(12), 0, dp(12), 0)
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(46)).apply {
        marginStart = dp(10)
    })
    card.addView(top)

    card.addView(TextView(context).apply {
        text = "Current Balance"
        setTextColor(Color.rgb(205, 212, 228))
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(20)
    })

    val balanceRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    balanceRow.addView(tablerIcon(TablerIcons.Coin, ComposeColor(0xFFD6FF2F)), LinearLayout.LayoutParams(dp(38), dp(38)).apply { marginEnd = dp(8) })
    balanceRow.addView(TextView(context).apply {
        text = balance
        setTextColor(Color.WHITE)
        textSize = if (balance.length > 8) 28f else 34f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    balanceRow.addView(TextView(context).apply {
        text = "Generate Now"
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(8, 16, 32))
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(214, 255, 47), dp(23))
        setOnClickListener { onGenerate() }
        isClickable = true
        minWidth = dp(112)
        setPadding(dp(12), 0, dp(12), 0)
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(46)).apply {
        marginStart = dp(8)
    })
    card.addView(balanceRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(16)
    })
    card.addView(TextView(context).apply {
        text = if (developerMode) "Developer mode active" else "Available credits"
        setTextColor(Color.rgb(154, 164, 186))
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(10)
    })
    addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
}

private fun LinearLayout.addCheckInSection(
    state: CreditsUiState.Success,
    checkIn: CreditRewardUi?,
    onReward: (String) -> Unit,
) {
    addTitle("Daily Check-in", "Check in and earn verified rewards across the week.", "Live")
    val rewards = listOf(1, 1, 2, 2, 2, 3, 4)
    val scroll = HorizontalScrollView(context).apply {
        isHorizontalScrollBarEnabled = false
        overScrollMode = View.OVER_SCROLL_NEVER
        // Prevent the parent vertical ScrollView from stealing horizontal swipe
        // gestures — without this, diagonal drags feel broken.
        setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> view.parent?.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> view.parent?.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }
    val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
    rewards.forEachIndexed { index, amount ->
        row.addView(dayTile(index, amount, state.checkInDayIndex == index), LinearLayout.LayoutParams(dp(66), dp(80)).apply {
            marginEnd = dp(8)
        })
    }
    scroll.addView(row)
    addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(12)
    })
    val button = TextView(context).apply {
        text = checkIn?.actionLabel ?: "Claim"
        gravity = Gravity.CENTER
        setTextColor(if (checkIn?.isAvailable == true) Color.rgb(8, 16, 32) else Color.rgb(156, 165, 186))
        textSize = 16f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        background = pill(if (checkIn?.isAvailable == true) Color.rgb(214, 255, 47) else Color.rgb(21, 31, 51), dp(27))
        alpha = if (state.isRewardBusy) 0.55f else 1f
        isEnabled = checkIn?.isAvailable == true && !state.isRewardBusy
        isClickable = isEnabled
        setOnClickListener { if (isEnabled) onReward("check_in") }
    }
    addView(button, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply {
        topMargin = dp(14)
    })
}

private fun LinearLayout.addDailyTaskSection(
    state: CreditsUiState.Success,
    onReward: (String) -> Unit,
) {
    addSectionHeader("Daily Task")
    state.rewards.filterNot { it.id == "check_in" }.forEach { reward ->
        addRewardRow(reward, state.isRewardBusy, onReward)
    }
}

private fun LinearLayout.addRewardRow(
    reward: CreditRewardUi,
    busy: Boolean,
    onReward: (String) -> Unit,
) {
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(7), 0, dp(7))
    }
    row.addView(FrameLayout(context).apply {
        background = taskIconBackground(reward.id)
        addView(tablerIcon(rewardIcon(reward.id), ComposeColor.White).apply {
            setPadding(dp(13), dp(13), dp(13), dp(13))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }, LinearLayout.LayoutParams(dp(56), dp(56)).apply { marginEnd = dp(12) })

    val textCol = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val titleLine = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    titleLine.addView(TextView(context).apply {
        text = reward.title
        setTextColor(Color.WHITE)
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        maxLines = 2
        includeFontPadding = false
    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    titleLine.addView(TextView(context).apply {
        text = reward.rewardLabel
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(214, 255, 47))
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(28, 36, 56), dp(13), Color.rgb(53, 64, 88), dp(1))
        setPadding(dp(8), 0, dp(8), 0)
        maxLines = 1
        includeFontPadding = false
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(22)).apply { marginStart = dp(7) })
    textCol.addView(titleLine)
    textCol.addView(TextView(context).apply {
        text = reward.subtitle
        setTextColor(Color.rgb(143, 153, 174))
        textSize = 11f
        maxLines = 3
        includeFontPadding = false
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(4)
    })
    row.addView(textCol, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    row.addView(TextView(context).apply {
        text = reward.actionLabel
        gravity = Gravity.CENTER
        setTextColor(if (reward.isAvailable) Color.rgb(214, 255, 47) else Color.rgb(139, 148, 168))
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(19, 28, 46), dp(23), Color.rgb(45, 55, 76), dp(1))
        alpha = if (reward.isAvailable && !busy) 1f else 0.4f
        isEnabled = reward.isAvailable && !busy
        isClickable = isEnabled
        setOnClickListener { if (isEnabled) onReward(reward.id) }
        maxLines = 1
        includeFontPadding = false
        minWidth = dp(82)
        setPadding(dp(10), 0, dp(10), 0)
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)).apply { marginStart = dp(10) })
    addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(8)
    })
}

private fun LinearLayout.addRewardAdButton(amount: Int, busy: Boolean, onWatchAd: () -> Unit) {
    val button = TextView(context).apply {
        text = "WATCH AD FOR $amount CREDITS"
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(8, 16, 32))
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(214, 255, 47), dp(26))
        isEnabled = !busy
        alpha = if (busy) 0.5f else 1f
        setOnClickListener { if (isEnabled) onWatchAd() }
    }
    addView(button, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
        topMargin = dp(16)
    })
}

private fun LinearLayout.addNotice(title: String, subtitle: String, onDismiss: () -> Unit) {
    addTextCard(title, subtitle, onDismiss)
}

private fun LinearLayout.addTitle(title: String, subtitle: String, tag: String) {
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.BOTTOM
    }
    val copy = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    copy.addView(TextView(context).apply {
        text = title
        setTextColor(Color.WHITE)
        textSize = 21f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    })
    copy.addView(TextView(context).apply {
        text = subtitle
        setTextColor(Color.rgb(148, 158, 180))
        textSize = 12f
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(4)
    })
    row.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    row.addView(TextView(context).apply {
        text = tag
        setTextColor(Color.rgb(214, 255, 47))
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
    })
    addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(24)
    })
}

private fun LinearLayout.addSectionHeader(title: String) {
    addView(TextView(context).apply {
        text = title
        setTextColor(Color.WHITE)
        textSize = 21f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(24)
    })
}

private fun LinearLayout.dayTile(index: Int, amount: Int, selected: Boolean): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(4), dp(6), dp(4), dp(6))
        background = rounded(
            if (selected) Color.rgb(37, 50, 39) else Color.rgb(24, 34, 53),
            dp(10),
            if (selected) Color.rgb(158, 194, 58) else Color.rgb(45, 55, 76),
            dp(1)
        )
        addView(TextView(context).apply {
            text = "D${index + 1}"
            setTextColor(if (selected) Color.rgb(214, 255, 47) else Color.rgb(165, 174, 194))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        addView(tablerIcon(TablerIcons.Stars, if (selected) ComposeColor(0xFFD6FF2F) else ComposeColor(0xFFAAB2C6)).apply {
            background = rounded(Color.rgb(43, 54, 73), dp(20), Color.rgb(68, 80, 101), dp(1))
            setPadding(dp(9), dp(9), dp(9), dp(9))
        }, LinearLayout.LayoutParams(dp(40), dp(40)).apply { topMargin = dp(6) })
        addView(TextView(context).apply {
            text = "+$amount"
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(5)
        })
    }

private fun rewardIcon(id: String): ImageVector = when (id) {
    REWARD_SPIN -> TablerIcons.RotateClockwise
    "daily_reset" -> TablerIcons.Refresh
    "signup" -> TablerIcons.Gift
    "email_login" -> TablerIcons.Mail
    "referral", "social_share" -> TablerIcons.Share
    else -> TablerIcons.Stars
}

private fun View.tablerIcon(imageVector: ImageVector, tint: ComposeColor): ComposeView =
    ComposeView(context).apply {
        setContent {
            Icon(imageVector = imageVector, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
    }

private fun View.taskIconBackground(id: String): GradientDrawable {
    val colors = when (id) {
        "signup" -> intArrayOf(Color.rgb(174, 58, 218), Color.rgb(42, 140, 238))
        "email_login" -> intArrayOf(Color.rgb(34, 214, 203), Color.rgb(39, 111, 218))
        "referral" -> intArrayOf(Color.rgb(236, 45, 143), Color.rgb(74, 91, 225))
        else -> intArrayOf(Color.rgb(214, 255, 47), Color.rgb(36, 152, 226))
    }
    return roundedGradient(colors, dp(28))
}

private fun View.roundedGradient(colors: IntArray, radius: Int, strokeColor: Int? = null, strokeWidth: Int = 0): GradientDrawable =
    GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
        cornerRadius = radius.toFloat()
        if (strokeColor != null && strokeWidth > 0) setStroke(strokeWidth, strokeColor)
    }

private fun View.rounded(color: Int, radius: Int, strokeColor: Int? = null, strokeWidth: Int = 0): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        if (strokeColor != null && strokeWidth > 0) setStroke(strokeWidth, strokeColor)
    }

private fun View.pill(color: Int, radius: Int, strokeColor: Int? = null, strokeWidth: Int = 0): GradientDrawable =
    rounded(color, radius, strokeColor, strokeWidth)
