package com.deep.lumoraai.feature.network

import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addPrimaryButton
import com.deep.lumoraai.core.nativeui.dp
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar

@Composable
fun NoInternetScreen(
    onTurnOnNetwork: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(modifier = modifier) { binding ->
        val context = binding.root.context
        fun px(value: Int) = binding.root.dp(value)
        binding.setupTopBar(
            titleText = context.getString(R.string.no_internet_connection),
            subtitleText = null,
            onBack = null,
        )
        binding.resetContent()
        binding.content.gravity = Gravity.CENTER_HORIZONTAL
        binding.content.addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_lumora_wifi_off)
            setBackgroundResource(R.drawable.bg_common_card)
            setPadding(px(32), px(32), px(32), px(32))
        }, LinearLayout.LayoutParams(px(122), px(122)).apply {
            setMargins(0, px(30), 0, px(24))
        })
        binding.content.addView(TextView(context).apply {
            text = context.getString(R.string.no_internet_connection)
            setTextColor(Color.WHITE)
            textSize = 26f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        binding.content.addView(TextView(context).apply {
            text = context.getString(R.string.no_internet_message)
            setTextColor(Color.rgb(148, 160, 184))
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(px(8), px(12), px(8), px(18))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        binding.content.addPrimaryButton(context.getString(R.string.turn_on_network), onClick = onTurnOnNetwork)
        binding.content.addPrimaryButton(context.getString(R.string.retry), onClick = onRetry)
    }
}
