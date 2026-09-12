package com.deep.lumoraai.feature.credits

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.ads.LocalAdsManager
import com.deep.lumoraai.ads.rememberCurrentActivity
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.dp
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate

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
    val onWatchAdForCredits: () -> Unit = {
        ads?.showRewarded(
            activity = adActivity,
            placement = AdPlacement.REWARD_CREDITS,
            onReward = { viewModel.addRewardedCredits(rewardAmount) },
            onClosed = {},
        )
    }

    LumoraXmlScreen(
        selectedTab = "credits",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        binding.setupTopBar("Reward Center", null, onBack = onBack)
        binding.resetContent()
        when (uiState) {
            CreditsUiState.Loading -> binding.content.addTextCard("Loading", "Reading your credit balance.")
            is CreditsUiState.Error -> binding.content.addTextCard("Error", uiState.message)
            is CreditsUiState.Success -> binding.content.renderRewardCenter(
                state = uiState,
                rewardAdEnabled = ads != null && !uiState.isDeveloperMode,
                rewardAdAmount = rewardAmount,
                onSubscribe = { onNavigate(Screen.Subscription.route) },
                onGenerate = { onNavigate(Screen.Home.route) },
                onReward = { viewModel.claimReward(it) },
                onMessageDismiss = {
                    viewModel.clearRewardMessage()
                    viewModel.clearSpinResult()
                },
                onWatchAd = onWatchAdForCredits,
            )
        }
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
    setPadding(paddingLeft, dp(18), paddingRight, dp(28))
    val balance = if (state.isDeveloperMode || state.credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        state.credits.toString()
    }
    addHeroCard(balance, state.isDeveloperMode, onSubscribe, onGenerate)
    state.purchaseMessage?.let { addNotice("Purchase", it, onMessageDismiss) }
    state.rewardMessage?.let { addNotice("Reward", it, onMessageDismiss) }
    state.spinResult?.let {
        val result = if (it.creditsAwarded > 0) "+${it.creditsAwarded} credits awarded" else "Better luck next time"
        addNotice("Spin Result", result, onMessageDismiss)
    }

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
        setPadding(dp(22), dp(22), dp(22), dp(22))
        background = roundedGradient(
            intArrayOf(Color.rgb(29, 24, 51), Color.rgb(42, 31, 92), Color.rgb(17, 26, 45)),
            radius = dp(24),
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
        textSize = 20f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    top.addView(TextView(context).apply {
        text = "Subscribe Now"
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.08f
        background = roundedGradient(intArrayOf(Color.rgb(154, 92, 245), Color.rgb(174, 88, 255)), dp(28))
        setOnClickListener { onSubscribe() }
        isClickable = true
    }, LinearLayout.LayoutParams(dp(178), dp(56)))
    card.addView(top)

    card.addView(TextView(context).apply {
        text = "Current Balance"
        setTextColor(Color.rgb(205, 212, 228))
        textSize = 19f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(34)
    })

    val balanceRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    balanceRow.addView(ImageView(context).apply {
        setImageResource(R.drawable.ic_lumora_star)
        setColorFilter(Color.rgb(214, 255, 47))
    }, LinearLayout.LayoutParams(dp(60), dp(60)).apply { marginEnd = dp(10) })
    balanceRow.addView(TextView(context).apply {
        text = balance
        setTextColor(Color.WHITE)
        textSize = if (balance.length > 8) 34f else 44f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
        maxLines = 1
    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    balanceRow.addView(TextView(context).apply {
        text = "Generate Now"
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(8, 16, 32))
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(214, 255, 47), dp(28))
        setOnClickListener { onGenerate() }
        isClickable = true
    }, LinearLayout.LayoutParams(dp(148), dp(58)))
    card.addView(balanceRow, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(26)
    })
    card.addView(TextView(context).apply {
        text = if (developerMode) "Developer mode active" else "Available credits"
        setTextColor(Color.rgb(154, 164, 186))
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(18)
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
    }
    val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
    rewards.forEachIndexed { index, amount ->
        row.addView(dayTile(index, amount, state.checkInDayIndex == index), LinearLayout.LayoutParams(dp(80), dp(94)).apply {
            marginEnd = dp(10)
        })
    }
    scroll.addView(row)
    addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(16)
    })
    val button = TextView(context).apply {
        text = checkIn?.actionLabel ?: "Claim"
        gravity = Gravity.CENTER
        setTextColor(if (checkIn?.isAvailable == true) Color.rgb(8, 16, 32) else Color.rgb(156, 165, 186))
        textSize = 22f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        background = pill(if (checkIn?.isAvailable == true) Color.rgb(214, 255, 47) else Color.rgb(21, 31, 51), dp(42))
        alpha = if (state.isRewardBusy) 0.55f else 1f
        isEnabled = checkIn?.isAvailable == true && !state.isRewardBusy
        isClickable = isEnabled
        setOnClickListener { if (isEnabled) onReward("check_in") }
    }
    addView(button, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(74)).apply {
        topMargin = dp(20)
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
        setPadding(0, dp(10), 0, dp(10))
    }
    row.addView(FrameLayout(context).apply {
        background = taskIconBackground(reward.id)
        addView(ImageView(context).apply {
            setImageResource(rewardIcon(reward.id))
            setColorFilter(Color.WHITE)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }, LinearLayout.LayoutParams(dp(76), dp(76)).apply { marginEnd = dp(18) })

    val textCol = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val titleLine = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    titleLine.addView(TextView(context).apply {
        text = reward.title
        setTextColor(Color.WHITE)
        textSize = 19f
        typeface = Typeface.DEFAULT_BOLD
        maxLines = 2
        includeFontPadding = false
    }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    titleLine.addView(TextView(context).apply {
        text = reward.rewardLabel
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(214, 255, 47))
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(28, 36, 56), dp(13), Color.rgb(53, 64, 88), dp(1))
        setPadding(dp(10), 0, dp(10), 0)
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(26)).apply { marginStart = dp(10) })
    textCol.addView(titleLine)
    textCol.addView(TextView(context).apply {
        text = reward.subtitle
        setTextColor(Color.rgb(143, 153, 174))
        textSize = 14f
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(7)
    })
    row.addView(textCol, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    row.addView(TextView(context).apply {
        text = reward.actionLabel
        gravity = Gravity.CENTER
        setTextColor(if (reward.isAvailable) Color.rgb(214, 255, 47) else Color.rgb(139, 148, 168))
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(19, 28, 46), dp(28), Color.rgb(45, 55, 76), dp(1))
        isEnabled = reward.isAvailable && !busy
        isClickable = isEnabled
        alpha = if (busy && reward.isAvailable) 0.55f else 1f
        setOnClickListener { if (isEnabled) onReward(reward.id) }
    }, LinearLayout.LayoutParams(dp(132), dp(58)).apply { marginStart = dp(14) })
    addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(12)
    })
}

private fun LinearLayout.addRewardAdButton(amount: Int, busy: Boolean, onWatchAd: () -> Unit) {
    val button = TextView(context).apply {
        text = "WATCH AD FOR $amount CREDITS"
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(8, 16, 32))
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        background = pill(Color.rgb(214, 255, 47), dp(30))
        isEnabled = !busy
        alpha = if (busy) 0.5f else 1f
        setOnClickListener { if (isEnabled) onWatchAd() }
    }
    addView(button, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)).apply {
        topMargin = dp(20)
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
        textSize = 25f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    })
    copy.addView(TextView(context).apply {
        text = subtitle
        setTextColor(Color.rgb(148, 158, 180))
        textSize = 14f
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(6)
    })
    row.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    row.addView(TextView(context).apply {
        text = tag
        setTextColor(Color.rgb(214, 255, 47))
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
    })
    addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(34)
    })
}

private fun LinearLayout.addSectionHeader(title: String) {
    addView(TextView(context).apply {
        text = title
        setTextColor(Color.WHITE)
        textSize = 25f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(34)
    })
}

private fun LinearLayout.dayTile(index: Int, amount: Int, selected: Boolean): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(6), dp(8), dp(6), dp(8))
        background = rounded(
            if (selected) Color.rgb(37, 50, 39) else Color.rgb(24, 34, 53),
            dp(12),
            if (selected) Color.rgb(158, 194, 58) else Color.rgb(45, 55, 76),
            dp(1)
        )
        addView(TextView(context).apply {
            text = "D${index + 1}"
            setTextColor(if (selected) Color.rgb(214, 255, 47) else Color.rgb(165, 174, 194))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_lumora_star)
            setColorFilter(if (selected) Color.rgb(214, 255, 47) else Color.rgb(170, 178, 198))
            background = rounded(Color.rgb(43, 54, 73), dp(26), Color.rgb(68, 80, 101), dp(1))
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }, LinearLayout.LayoutParams(dp(52), dp(52)).apply { topMargin = dp(8) })
        addView(TextView(context).apply {
            text = "+$amount"
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(7)
        })
    }

@DrawableRes
private fun rewardIcon(id: String): Int = when (id) {
    "spin" -> R.drawable.ic_lumora_star
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
        "referral" -> intArrayOf(Color.rgb(236, 45, 143), Color.rgb(74, 91, 225))
        else -> intArrayOf(Color.rgb(214, 255, 47), Color.rgb(36, 152, 226))
    }
    return roundedGradient(colors, dp(38))
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
