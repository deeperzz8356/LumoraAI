package com.deep.lumoraai.feature.credits

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.databinding.CreditsScreenBinding

private const val REWARD_SPIN = "spin"
private const val REWARD_CHECK_IN = "check_in"
private val WEEKLY_CHECK_IN_REWARDS = listOf(1, 1, 2, 2, 2, 3, 4)

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
    val adsConfig = LocalAdsConfigStore.current?.current
    val rewardAmount = adsConfig?.rewardCreditsAmount ?: 2
    val subscriptionEnabled = adsConfig?.subscriptionEnabled ?: true
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
                    root.tag = this
                    scroll.isVerticalScrollBarEnabled = false
                    scroll.overScrollMode = View.OVER_SCROLL_NEVER
                }.root
            },
            update = { root ->
                val binding = root.tag as CreditsScreenBinding
                binding.backButton.setOnClickListener { onBack() }

                when (uiState) {
                    CreditsUiState.Loading -> binding.showState("Loading", "Reading your credit balance.")
                    is CreditsUiState.Error -> binding.showState("Error", uiState.message)
                    is CreditsUiState.Success -> binding.renderCredits(
                        state = uiState,
                        rewardAdEnabled = ads != null && !uiState.isDeveloperMode,
                        rewardAdAmount = rewardAmount,
                        subscriptionEnabled = subscriptionEnabled,
                        onSubscribe = { onNavigate(Screen.Subscription.route) },
                        onGenerate = { onNavigate(Screen.Home.route) },
                        onReward = { rewardId ->
                            if (rewardId == REWARD_SPIN) showSpinWheel = true else viewModel.claimReward(rewardId)
                        },
                        onMessageDismiss = {
                            viewModel.clearRewardMessage()
                            viewModel.clearSpinResult()
                        },
                        onWatchAd = onWatchAdForCredits,
                    )
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

private fun CreditsScreenBinding.renderCredits(
    state: CreditsUiState.Success,
    rewardAdEnabled: Boolean,
    rewardAdAmount: Int,
    subscriptionEnabled: Boolean,
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
    balanceText.textSize = if (balance.length > 8) 30f else 38f
    balanceSubtitle.text = if (state.isDeveloperMode) {
        "Developer mode active"
    } else {
        "Use credits to generate images and videos."
    }
    generateButton.setOnClickListener { onGenerate() }
    generateButton.alpha = 1f
    generateButton.setTextColor(Color.rgb(8, 16, 32))
    generateButton.background = generateButton.rounded(Color.rgb(214, 255, 47), generateButton.dp(25))
    subscribeButton.visibility =
    if (subscriptionEnabled) View.VISIBLE else View.GONE

    subscribeButton.setOnClickListener {
        if (subscriptionEnabled) {
            onSubscribe()
        }
    }

    noticeContainer.removeAllViews()
    state.purchaseMessage?.takeIf { it.isNotBlank() }?.let {
        noticeContainer.addNotice("Purchase update", it, onMessageDismiss)
    }
    state.rewardMessage?.takeIf { it.isNotBlank() }?.let {
        noticeContainer.addNotice("Reward update", it, onMessageDismiss)
    }

    renderCheckIn(state, onReward)
    renderRewards(state, rewardAdEnabled, rewardAdAmount, onReward, onWatchAd)
}

private fun CreditsScreenBinding.renderCheckIn(
    state: CreditsUiState.Success,
    onReward: (String) -> Unit,
) {
    checkInDaysGrid.removeAllViews()
    val selectedIndex = state.checkInDayIndex.coerceIn(0, WEEKLY_CHECK_IN_REWARDS.lastIndex)
    WEEKLY_CHECK_IN_REWARDS.chunked(4).forEachIndexed { rowIndex, rowRewards ->
        val row = LinearLayout(checkInDaysGrid.context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        rowRewards.forEachIndexed { columnIndex, amount ->
            val index = rowIndex * 4 + columnIndex
            row.addView(
                row.dayTile(index = index, amount = amount, selected = index == selectedIndex),
                LinearLayout.LayoutParams(0, row.dp(92), 1f).apply {
                    if (columnIndex < 3) marginEnd = row.dp(8)
                },
            )
        }
        repeat(4 - rowRewards.size) { spacerIndex ->
            row.addView(View(row.context), LinearLayout.LayoutParams(0, row.dp(92), 1f).apply {
                if (rowRewards.size + spacerIndex < 3) marginEnd = row.dp(8)
            })
        }
        checkInDaysGrid.addView(
            row,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                if (rowIndex > 0) topMargin = checkInDaysGrid.dp(8)
            },
        )
    }

    val checkIn = state.rewards.firstOrNull { it.id == REWARD_CHECK_IN }
    val available = checkIn?.isAvailable == true && !state.isRewardBusy
    checkInButton.text = checkIn?.actionLabel ?: "Claim"
    checkInButton.isEnabled = available
    checkInButton.isClickable = available
    checkInButton.alpha = if (state.isRewardBusy) 0.55f else 1f
    checkInButton.setTextColor(if (checkIn?.isAvailable == true) Color.rgb(8, 16, 32) else Color.rgb(152, 162, 184))
    checkInButton.background = checkInButton.rounded(
        color = if (checkIn?.isAvailable == true) Color.rgb(214, 255, 47) else Color.rgb(21, 31, 51),
        radius = checkInButton.dp(26),
        strokeColor = if (checkIn?.isAvailable == true) null else Color.rgb(45, 55, 76),
        strokeWidth = checkInButton.dp(1),
    )
    checkInButton.setOnClickListener { if (checkInButton.isEnabled) onReward(REWARD_CHECK_IN) }
}

private fun CreditsScreenBinding.renderRewards(
    state: CreditsUiState.Success,
    rewardAdEnabled: Boolean,
    rewardAdAmount: Int,
    onReward: (String) -> Unit,
    onWatchAd: () -> Unit,
) {
    dailyRewardsList.removeAllViews()
    val rewards = state.rewards.filterNot { it.id == REWARD_CHECK_IN }
    if (rewards.isEmpty()) {
        dailyRewardsList.addView(TextView(dailyRewardsList.context).apply {
            text = "No tasks available right now."
            setTextColor(Color.rgb(148, 158, 180))
            textSize = 13f
        })
    } else {
        rewards.forEach { reward ->
            dailyRewardsList.addView(
                dailyRewardsList.rewardRow(reward, state.isRewardBusy, onReward),
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dailyRewardsList.dp(8)
                },
            )
        }
    }

    rewardAdButton.visibility = if (rewardAdEnabled) View.VISIBLE else View.GONE
    rewardAdButton.text = "WATCH AD FOR $rewardAdAmount CREDITS"
    rewardAdButton.isEnabled = !state.isRewardBusy
    rewardAdButton.alpha = if (state.isRewardBusy) 0.5f else 1f
    rewardAdButton.setTextColor(Color.rgb(8, 16, 32))
    rewardAdButton.background = rewardAdButton.rounded(Color.rgb(214, 255, 47), rewardAdButton.dp(26))
    rewardAdButton.setOnClickListener { if (rewardAdButton.isEnabled) onWatchAd() }
}

private fun LinearLayout.dayTile(index: Int, amount: Int, selected: Boolean): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(4), dp(7), dp(4), dp(7))
        background = rounded(
            color = if (selected) Color.rgb(38, 50, 39) else Color.rgb(24, 34, 53),
            radius = dp(12),
            strokeColor = if (selected) Color.rgb(214, 255, 47) else Color.rgb(45, 55, 76),
            strokeWidth = dp(1),
        )
        addView(TextView(context).apply {
            text = "D${index + 1}"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(if (selected) Color.rgb(214, 255, 47) else Color.rgb(165, 174, 194))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
        })
        addView(ImageView(context).apply {
            background = rounded(Color.rgb(43, 54, 73), dp(20), Color.rgb(68, 80, 101), dp(1))
            imageTintList = ColorStateList.valueOf(if (selected) Color.rgb(214, 255, 47) else Color.rgb(170, 178, 198))
            setImageResource(R.drawable.ic_lumora_star)
            setPadding(dp(9), dp(9), dp(9), dp(9))
        }, LinearLayout.LayoutParams(dp(40), dp(40)).apply {
            topMargin = dp(6)
        })
        addView(TextView(context).apply {
            text = "+$amount"
            includeFontPadding = false
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(5)
        })
    }

private fun LinearLayout.rewardRow(
    reward: CreditRewardUi,
    busy: Boolean,
    onReward: (String) -> Unit,
): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(8), 0, dp(8))

        addView(ImageView(context).apply {
            background = taskIconBackground(reward.id)
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            setImageResource(rewardIcon(reward.id))
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }, LinearLayout.LayoutParams(dp(54), dp(54)).apply {
            marginEnd = dp(12)
        })

        val copyColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        copyColumn.addView(TextView(context).apply {
            text = reward.title
            includeFontPadding = false
            maxLines = 2
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        })
        copyColumn.addView(TextView(context).apply {
            text = reward.subtitle
            includeFontPadding = false
            maxLines = 3
            setTextColor(Color.rgb(143, 153, 174))
            textSize = 11f
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4)
        })
        addView(copyColumn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val canTap = reward.isAvailable && !busy && !reward.isAutomatic
        addView(TextView(context).apply {
            text = reward.actionLabel
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            minWidth = dp(80)
            setPadding(dp(10), 0, dp(10), 0)
            setTextColor(if (reward.isAvailable) Color.rgb(214, 255, 47) else Color.rgb(139, 148, 168))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(Color.rgb(19, 28, 46), dp(22), Color.rgb(45, 55, 76), dp(1))
            alpha = if (canTap) 1f else 0.45f
            isEnabled = canTap
            isClickable = canTap
            setOnClickListener { if (isEnabled) onReward(reward.id) }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(44)).apply {
            marginStart = dp(10)
        })
    }

private fun LinearLayout.addNotice(title: String, body: String, onDismiss: () -> Unit) {
    addView(LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(14), dp(12), dp(14), dp(12))
        background = rounded(Color.rgb(18, 29, 47), dp(14), Color.rgb(54, 68, 95), dp(1))
        isClickable = true
        setOnClickListener { onDismiss() }

        val copy = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        copy.addView(TextView(context).apply {
            text = title
            includeFontPadding = false
            setTextColor(Color.rgb(214, 255, 47))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        })
        copy.addView(TextView(context).apply {
            text = body
            setTextColor(Color.WHITE)
            textSize = 13f
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(3)
        })
        addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(TextView(context).apply {
            text = "OK"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(Color.rgb(8, 16, 32))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(Color.rgb(214, 255, 47), dp(16))
            setPadding(dp(12), 0, dp(12), 0)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(32)).apply {
            marginStart = dp(10)
        })
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(12)
    })
}

private fun View.rewardIcon(id: String): Int = when (id) {
    REWARD_SPIN -> R.drawable.ic_lumora_refresh
    "daily_reset" -> R.drawable.ic_lumora_refresh
    "signup" -> R.drawable.ic_lumora_magic
    "email_login" -> R.drawable.ic_lumora_check
    "referral", "social_share" -> R.drawable.ic_lumora_share
    else -> R.drawable.ic_lumora_star
}

private fun View.taskIconBackground(id: String): GradientDrawable {
    val colors = when (id) {
        "signup" -> intArrayOf(Color.rgb(174, 58, 218), Color.rgb(42, 140, 238))
        "email_login" -> intArrayOf(Color.rgb(34, 214, 203), Color.rgb(39, 111, 218))
        "referral", "social_share" -> intArrayOf(Color.rgb(236, 45, 143), Color.rgb(74, 91, 225))
        REWARD_SPIN -> intArrayOf(Color.rgb(214, 255, 47), Color.rgb(36, 152, 226))
        else -> intArrayOf(Color.rgb(89, 101, 128), Color.rgb(43, 55, 79))
    }
    return GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
        cornerRadius = dp(27).toFloat()
    }
}

private fun View.rounded(
    color: Int,
    radius: Int,
    strokeColor: Int? = null,
    strokeWidth: Int = 0,
): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        if (strokeColor != null && strokeWidth > 0) setStroke(strokeWidth, strokeColor)
    }

private fun View.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
