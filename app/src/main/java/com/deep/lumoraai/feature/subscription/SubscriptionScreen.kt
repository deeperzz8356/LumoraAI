package com.deep.lumoraai.feature.subscription

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.dp
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.databinding.SubscriptionPlanCardBinding
import com.deep.lumoraai.databinding.SubscriptionScreenBinding
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

private const val SubscriptionCard = 0xFF111A2E.toInt()
private const val SubscriptionStroke = 0xFF223049.toInt()
private const val SubscriptionLime = 0xFFD6FF3F.toInt()
private const val SubscriptionMuted = 0xFF9AA5BB.toInt()

@Composable
fun SubscriptionScreen(
    uiState: SubscriptionUiState,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onClearMessage: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(modifier = modifier) { shell ->
        shell.topBar.visibility = View.GONE
        shell.content.removeAllViews()
        shell.content.setPadding(0, 0, 0, 0)
        val binding = SubscriptionScreenBinding.inflate(LayoutInflater.from(shell.content.context), shell.content, true)
        binding.backButton.setOnClickListener { onBack() }

        when (uiState) {
            SubscriptionUiState.Loading -> binding.bindLoadingOrError("Checking available plans.")
            is SubscriptionUiState.Error -> binding.bindLoadingOrError(uiState.message)
            is SubscriptionUiState.Success -> binding.bindSuccess(
                state = uiState,
                onSelectPlan = onSelectPlan,
                onPurchase = onPurchase,
                onRestore = onRestore,
                onClearMessage = onClearMessage,
                onCredits = { onNavigate(Screen.Credits.route) },
            )
        }
    }
}

private fun SubscriptionScreenBinding.bindLoadingOrError(message: String) {
    heroSubtitle.text = message
    featureTiles.visibility = View.GONE
    purchaseMessage.visibility = View.GONE
    planContainer.removeAllViews()
    checkoutCard.visibility = View.GONE
    creditsButton.visibility = View.GONE
    restoreButton.visibility = View.GONE
}

private fun SubscriptionScreenBinding.bindSuccess(
    state: SubscriptionUiState.Success,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onClearMessage: () -> Unit,
    onCredits: () -> Unit,
) {
    heroSubtitle.text = if (state.isDeveloperMode) {
        "Developer mode gives unlimited trial access."
    } else {
        "Unlock more credits, faster runs, and Pro tools."
    }
    featureTiles.removeAllViews()
    addFeatureTiles(featureTiles)

    purchaseMessage.visibility = if (state.purchaseMessage.isNullOrBlank()) View.GONE else View.VISIBLE
    purchaseMessage.text = state.purchaseMessage.orEmpty()
    purchaseMessage.setOnClickListener { onClearMessage() }

    planContainer.removeAllViews()
    state.plans.forEach { plan ->
        planContainer.addView(createPlanCard(plan, plan.id == state.selectedPlanId) { onSelectPlan(plan.id) })
    }
    val selectedPlan = state.plans.firstOrNull { it.id == state.selectedPlanId } ?: state.plans.firstOrNull()
    checkoutCard.visibility = if (selectedPlan == null) View.GONE else View.VISIBLE
    if (selectedPlan != null) {
        checkoutPlanName.text = selectedPlan.name
        checkoutPrice.text = selectedPlan.price
        activateButton.text = when {
            state.isPurchasing -> "Processing..."
            state.isDeveloperMode -> "Activate Plan (Dev)"
            else -> "Activate Plan"
        }
        activateButton.isEnabled = !state.isPurchasing
        activateButton.alpha = if (state.isPurchasing) 0.55f else 1f
        activateButton.setOnClickListener { if (activateButton.isEnabled) onPurchase() }
    }
    creditsButton.setOnClickListener { onCredits() }
    restoreButton.setOnClickListener { onRestore() }
}

private fun SubscriptionScreenBinding.createPlanCard(plan: SubscriptionPlan, selected: Boolean, onClick: () -> Unit): View {
    val binding = SubscriptionPlanCardBinding.inflate(LayoutInflater.from(root.context), planContainer, false)
    binding.root.background = root.rounded(SubscriptionCard, root.dp(18), if (selected) SubscriptionLime else SubscriptionStroke, root.dp(if (selected) 2 else 1))
    binding.name.text = plan.name
    binding.period.text = plan.billingPeriod
    binding.price.text = plan.price
    binding.price.setTextColor(if (selected) SubscriptionLime else SubscriptionMuted)
    binding.badge.visibility = if (plan.highlighted) View.VISIBLE else View.GONE
    binding.selectedBadge.visibility = if (selected) View.VISIBLE else View.GONE
    binding.root.setOnClickListener { onClick() }
    binding.features.removeAllViews()
    plan.features.forEach { feature ->
        binding.features.addView(featureLine(binding.root, feature, selected))
    }
    return binding.root
}

private fun addFeatureTiles(container: LinearLayout) {
    container.addView(container.featureTile("Faster", "priority runs", R.drawable.ic_lumora_magic, 0xFF223B1F.toInt(), SubscriptionLime), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
    container.addView(container.featureTile("Storage", "history sync", R.drawable.ic_lumora_check, 0xFF10404D.toInt(), 0xFF30D8DE.toInt()), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
        marginStart = container.dp(18)
    })
    container.addView(container.featureTile("Tools", "Pro access", R.drawable.ic_lumora_settings, 0xFF30245F.toInt(), 0xFFA360FF.toInt()), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
        marginStart = container.dp(18)
    })
}

private fun View.featureTile(title: String, subtitle: String, @DrawableRes icon: Int, iconBg: Int, tint: Int): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START
        setPadding(dp(18), dp(18), dp(18), dp(26))
        background = rounded(SubscriptionCard, dp(16), SubscriptionStroke, dp(1))
        addView(iconBlock(icon, iconBg, tint), LinearLayout.LayoutParams(dp(64), dp(64)))
        addView(TextView(context).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18) })
        addView(TextView(context).apply {
            text = subtitle
            setTextColor(SubscriptionMuted)
            textSize = 13f
            includeFontPadding = false
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(5) })
    }

private fun featureLine(parent: View, text: String, selected: Boolean): LinearLayout =
    LinearLayout(parent.context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_lumora_check)
            setColorFilter(if (selected) SubscriptionLime else SubscriptionMuted)
        }, LinearLayout.LayoutParams(parent.dp(26), parent.dp(26)).apply { marginEnd = parent.dp(20) })
        addView(TextView(context).apply {
            this.text = text
            setTextColor(if (selected) 0xFFE6EAF2.toInt() else 0xFFD2D6DF.toInt())
            textSize = 16f
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = parent.dp(24)
        }
    }

private fun View.iconBlock(@DrawableRes icon: Int, background: Int, tint: Int): FrameLayout =
    FrameLayout(context).apply {
        this.background = rounded(background, dp(18))
        addView(ImageView(context).apply {
            setImageResource(icon)
            setColorFilter(tint)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

private fun View.rounded(color: Int, radius: Int, strokeColor: Int? = null, strokeWidth: Int = 0): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        if (strokeColor != null && strokeWidth > 0) setStroke(strokeWidth, strokeColor)
    }
