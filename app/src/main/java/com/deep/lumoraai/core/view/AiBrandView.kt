package com.deep.lumoraai.core.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class AiBrandView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFD4FF3B.toInt() }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun onDraw(canvas: Canvas) {
        val onboardingMark = width > 40 * resources.displayMetrics.density
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(width.toFloat(), 0f)
            lineTo(width.toFloat(), if (onboardingMark) height.toFloat() else height * .8f)
            lineTo(0f, if (onboardingMark) height * .84f else height.toFloat())
            close()
        }
        canvas.drawPath(path, shapePaint)
        val baseline = height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f -
            if (onboardingMark) 0f else resources.displayMetrics.density * 3f
        canvas.drawText("ai", width / 2f, baseline, textPaint)
    }
}
