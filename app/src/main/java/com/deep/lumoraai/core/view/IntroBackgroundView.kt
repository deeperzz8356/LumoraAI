package com.deep.lumoraai.core.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class IntroBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val stars = Random(42).let { random ->
        List(100) { floatArrayOf(random.nextFloat(), random.nextFloat(), random.nextFloat() * 1.5f, random.nextFloat() * .3f + .1f) }
    }

    override fun onDraw(canvas: Canvas) {
        val radius = maxOf(width, height).toFloat()
        backgroundPaint.shader = RadialGradient(
            width / 2f, height / 2f, radius,
            intArrayOf(0x66161A2D, 0x33121212, 0xFF090909.toInt()),
            floatArrayOf(0f, .55f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        val density = resources.displayMetrics.density
        stars.forEach { star ->
            starPaint.alpha = (star[3] * 255).toInt()
            canvas.drawCircle(star[0] * width, star[1] * height, star[2] * density, starPaint)
        }
    }
}
