package com.deep.lumoraai.feature.templates

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.deep.lumoraai.R

class TemplateMediaStateOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val progress = ProgressBar(context).apply {
        isIndeterminate = true
        indeterminateTintList = ColorStateList.valueOf(context.getColor(R.color.lumora_lime))
    }
    private val title = TextView(context).apply {
        gravity = Gravity.CENTER
        includeFontPadding = false
        setTextColor(Color.WHITE)
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val subtitle = TextView(context).apply {
        gravity = Gravity.CENTER
        includeFontPadding = false
        setTextColor(0xFF9AA5BB.toInt())
        textSize = 11f
    }

    init {
        isClickable = false
        isFocusable = false
        setBackgroundColor(0xF20F1726.toInt())
        addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(12), dp(12), dp(12))
                addView(progress, LinearLayout.LayoutParams(dp(30), dp(30)))
                addView(title, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(10)
                })
                addView(subtitle, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(5)
                })
            },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER),
        )
        showLoading()
    }

    fun showLoading(message: String = "Loading media...") {
        animate().cancel()
        setBackgroundColor(0xF20F1726.toInt())
        alpha = 1f
        visibility = VISIBLE
        progress.isVisible = true
        title.text = message
        subtitle.text = ""
    }

    fun showSuccess() {
        progress.isVisible = false
        title.text = ""
        subtitle.text = ""
        animate()
            .alpha(0f)
            .setDuration(160L)
            .withEndAction {
                visibility = GONE
                alpha = 1f
            }
            .start()
    }

    fun showError(message: String = "Preview unavailable") {
        animate().cancel()
        alpha = 1f
        visibility = VISIBLE
        progress.isVisible = false
        title.text = message
        subtitle.text = "Tap to create"
        background = rounded(0xF20F1726.toInt(), dp(18), 0xFF26364F.toInt(), dp(1))
    }

    private fun rounded(color: Int, radius: Int, strokeColor: Int, strokeWidth: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            setStroke(strokeWidth, strokeColor)
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
