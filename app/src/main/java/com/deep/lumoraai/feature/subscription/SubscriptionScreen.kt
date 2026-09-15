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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.dp
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.databinding.ScreenSubscriptionRootBinding
import com.deep.lumoraai.databinding.SubscriptionPlanCardBinding
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
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            val binding = ScreenSubscriptionRootBinding.inflate(LayoutInflater.from(context))
            binding.root.tag = binding
            binding.root
        },
        update = { root ->
            val binding = root.tag as ScreenSubscriptionRootBinding
            binding.backButton.setOnClickListener { onBack() }

            when (uiState) {
                SubscriptionUiState.Loading ->
                    binding.bindLoadingOrError("Checking available plans.")
                SubscriptionUiState.Disabled ->
                    binding.bindHidden()
                is SubscriptionUiState.Error ->
                    binding.bindLoadingOrError(uiState.message)
                is SubscriptionUiState.Success -> binding.bindSuccess(
                    state = uiState,
                    onSelectPlan = onSelectPlan,
                    onPurchase = onPurchase,
                    onRestore = onRestore,
                    onClearMessage = onClearMessage,
                    onCredits = { onNavigate(Screen.Credits.route) },
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(ComposeColor(0xFF081020))
            .statusBarsPadding()
            .navigationBarsPadding(),
    )
}

private fun ScreenSubscriptionRootBinding.bindHidden() {
    heroCard.visibility = View.GONE
    featureTiles.visibility = View.GONE
    purchaseMessage.visibility = View.GONE
    plansCard.visibility = View.GONE
    planContainer.removeAllViews()
    planContainer.setTag(R.id.subscription_plan_signature, null)
    featureTiles.setTag(R.id.subscription_feature_tiles_bound, null)
    checkoutCard.visibility = View.GONE
    creditsButton.visibility = View.GONE
    restoreButton.visibility = View.GONE
}

private fun ScreenSubscriptionRootBinding.bindLoadingOrError(message: String) {
    heroSubtitle.text = message
    heroCard.visibility = View.VISIBLE
    featureTiles.visibility = View.GONE
    purchaseMessage.visibility = View.GONE
    plansCard.visibility = View.GONE
    planContainer.removeAllViews()
    planContainer.setTag(R.id.subscription_plan_signature, null)
    checkoutCard.visibility = View.GONE
    creditsButton.visibility = View.GONE
    restoreButton.visibility = View.GONE
}

private fun ScreenSubscriptionRootBinding.bindSuccess(
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
    heroIconHost.setImageResource(R.drawable.ic_lumora_star)
    heroIconHost.setColorFilter(0xFFFF4AA2.toInt())
    heroCard.visibility = View.VISIBLE
    if (featureTiles.getTag(R.id.subscription_feature_tiles_bound) != true) {
        featureTiles.removeAllViews()
        addFeatureTiles(featureTiles)
        featureTiles.setTag(R.id.subscription_feature_tiles_bound, true)
    }
    featureTiles.visibility = View.VISIBLE

    purchaseMessage.visibility = if (state.purchaseMessage.isNullOrBlank()) View.GONE else View.VISIBLE
    purchaseMessage.text = state.purchaseMessage.orEmpty()
    purchaseMessage.setOnClickListener { onClearMessage() }

    plansCard.visibility = View.VISIBLE
    val planSignature = state.planSignature()
    if (planContainer.getTag(R.id.subscription_plan_signature) != planSignature) {
        planContainer.removeAllViews()
        state.plans.forEach { plan ->
            planContainer.addView(
                createPlanCard(plan, plan.id == state.selectedPlanId) { onSelectPlan(plan.id) }
            )
        }
        planContainer.setTag(R.id.subscription_plan_signature, planSignature)
    }

    val selectedPlan = state.plans.firstOrNull { it.id == state.selectedPlanId }
        ?: state.plans.firstOrNull()
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
        activateButton.setTextColor(0xFF081020.toInt())
        activateButton.background = activateButton.rounded(SubscriptionLime, activateButton.dp(27))
        activateButton.setOnClickListener { if (activateButton.isEnabled) onPurchase() }
    }
    creditsButton.visibility = View.VISIBLE
    creditsButton.setOnClickListener { onCredits() }
    restoreButton.visibility = View.VISIBLE
    restoreButton.setOnClickListener { onRestore() }
}

private fun ScreenSubscriptionRootBinding.createPlanCard(
    plan: SubscriptionPlan,
    selected: Boolean,
    onClick: () -> Unit,
): View {
    val binding = SubscriptionPlanCardBinding.inflate(
        LayoutInflater.from(root.context), planContainer, false
    )
    binding.root.background = root.rounded(
        SubscriptionCard, root.dp(18),
        if (selected) SubscriptionLime else SubscriptionStroke,
        root.dp(if (selected) 2 else 1)
    )
    binding.name.text = plan.name
    binding.period.text = plan.billingPeriod
    binding.price.text = plan.price
    binding.price.setTextColor(if (selected) SubscriptionLime else SubscriptionMuted)
    binding.badge.visibility = if (plan.highlighted) View.VISIBLE else View.GONE
    binding.selectedBadge.visibility = if (selected) View.VISIBLE else View.GONE
    binding.badge.background = binding.badge.rounded(SubscriptionLime, binding.badge.dp(14))
    binding.selectedBadge.background =
        binding.selectedBadge.rounded(SubscriptionLime, binding.selectedBadge.dp(17))
    binding.selectedIconHost.setImageResource(R.drawable.ic_lumora_check)
    binding.selectedIconHost.setColorFilter(0xFF081020.toInt())
    binding.root.setOnClickListener { onClick() }
    binding.features.removeAllViews()
    plan.features.forEach { feature ->
        binding.features.addView(featureLine(binding.root, feature, selected))
    }
    return binding.root
}

private fun addFeatureTiles(container: LinearLayout) {
    container.addView(
        container.featureTile("Faster", "priority runs", R.drawable.ic_lumora_magic, 0xFF223B1F.toInt(), SubscriptionLime),
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
    )
    container.addView(
        container.featureTile("Storage", "history sync", R.drawable.ic_lumora_check, 0xFF10404D.toInt(), 0xFF30D8DE.toInt()),
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = container.dp(10)
        }
    )
    container.addView(
        container.featureTile("Tools", "Pro access", R.drawable.ic_lumora_settings, 0xFF30245F.toInt(), 0xFFA360FF.toInt()),
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = container.dp(10)
        }
    )
}

private fun View.featureTile(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    iconBg: Int,
    tint: Int,
): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START
        setPadding(dp(12), dp(12), dp(12), dp(14))
        background = rounded(SubscriptionCard, dp(16), SubscriptionStroke, dp(1))
        addView(iconBlock(icon, iconBg, tint), LinearLayout.LayoutParams(dp(42), dp(42)))
        addView(TextView(context).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(10)
        })
        addView(TextView(context).apply {
            text = subtitle
            setTextColor(SubscriptionMuted)
            textSize = 11f
            includeFontPadding = false
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(3)
        })
    }

private fun featureLine(parent: View, text: String, selected: Boolean): LinearLayout =
    LinearLayout(parent.context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
        addView(
            parent.nativeIcon(R.drawable.ic_lumora_check, if (selected) SubscriptionLime else SubscriptionMuted),
            LinearLayout.LayoutParams(parent.dp(18), parent.dp(18)).apply { marginEnd = parent.dp(10) }
        )
        addView(TextView(context).apply {
            this.text = text
            setTextColor(if (selected) 0xFFE6EAF2.toInt() else 0xFFD2D6DF.toInt())
            textSize = 13f
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = parent.dp(10)
        }
    }

private fun View.iconBlock(@DrawableRes icon: Int, background: Int, tint: Int): FrameLayout =
    FrameLayout(context).apply {
        this.background = rounded(background, dp(18))
        addView(
            nativeIcon(icon, tint).apply {
                setPadding(dp(11), dp(11), dp(11), dp(11))
            },
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

private fun View.nativeIcon(@DrawableRes icon: Int, tint: Int): ImageView =
    ImageView(context).apply {
        setImageResource(icon)
        setColorFilter(tint)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
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

private fun SubscriptionUiState.Success.planSignature(): String =
    buildString {
        append(selectedPlanId)
        append('|')
        plans.forEach { plan ->
            append(plan.id)
            append(',')
            append(plan.name)
            append(',')
            append(plan.price)
            append(',')
            append(plan.billingPeriod)
            append(',')
            append(plan.highlighted)
            append(',')
            append(plan.features.joinToString("~"))
            append('|')
        }
    }
