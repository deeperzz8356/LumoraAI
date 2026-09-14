package com.deep.lumoraai.feature.templates

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.deep.lumoraai.R

/** Only visible cards own a decoder; leaving the screen releases it immediately. */
@androidx.annotation.OptIn(UnstableApi::class)
class TemplateVideoPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val surface = LayoutInflater.from(context)
        .inflate(R.layout.template_video_surface, this, false) as PlayerView
    private var player: ExoPlayer? = null
    private var asset: String? = null
    private var failed = false
    private var owner: LifecycleOwner? = null
    private val visibleRect = Rect()
    private val observer = LifecycleEventObserver { _, _ -> updatePlayback() }
    private val preDraw = ViewTreeObserver.OnPreDrawListener {
        updatePlayback()
        true
    }

    init {
        addView(surface)
        surface.alpha = 0f
        surface.isClickable = false
        surface.isFocusable = false
    }

    fun bind(fileName: String?) {
        if (asset != fileName) {
            releasePlayer()
            asset = fileName
            failed = false
        }
        visibility = if (fileName == null) GONE else VISIBLE
        updatePlayback()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        owner = findViewTreeLifecycleOwner()
        owner?.lifecycle?.addObserver(observer)
        viewTreeObserver.addOnPreDrawListener(preDraw)
        updatePlayback()
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnPreDrawListener(preDraw)
        owner?.lifecycle?.removeObserver(observer)
        owner = null
        releasePlayer()
        super.onDetachedFromWindow()
    }

    private fun updatePlayback() {
        val fileName = asset ?: return
        val visible = isAttachedToWindow && isShown && windowVisibility == VISIBLE &&
            owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true &&
            getGlobalVisibleRect(visibleRect) &&
            visibleRect.height() >= height / 2 && visibleRect.width() >= width / 2
        if (!visible) {
            releasePlayer()
            return
        }
        if (player != null || failed) return
        val next = ExoPlayer.Builder(context).build()
        player = next
        surface.player = next
        next.volume = 0f
        next.repeatMode = Player.REPEAT_MODE_ONE
        next.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() { surface.alpha = 1f }
            override fun onPlayerError(error: PlaybackException) {
                failed = true
                releasePlayer()
            }
        })
        next.setMediaItem(MediaItem.fromUri("asset:///templates/${Uri.encode(fileName)}"))
        next.prepare()
        next.playWhenReady = true
    }

    private fun releasePlayer() {
        surface.alpha = 0f
        surface.player = null
        player?.release()
        player = null
    }
}
