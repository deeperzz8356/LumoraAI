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
import java.io.File
import java.util.Collections

/** Only visible cards own a decoder; leaving the screen releases it immediately. */
@androidx.annotation.OptIn(UnstableApi::class)
class TemplateVideoPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val surface = LayoutInflater.from(context)
        .inflate(R.layout.template_video_surface, this, false) as PlayerView
    private val mediaState = TemplateMediaStateOverlay(context)
    private var player: ExoPlayer? = null
    private var asset: String? = null
    private var failed = false
    private var renderedFirstFrame = false
    private var owner: LifecycleOwner? = null
    private val visibleRect = Rect()
    private val observer = LifecycleEventObserver { _, _ -> updatePlayback() }
    private val preDraw = ViewTreeObserver.OnPreDrawListener {
        updatePlayback()
        true
    }

    init {
        addView(surface)
        addView(mediaState, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        surface.alpha = 0f
        surface.isClickable = false
        surface.isFocusable = false
    }

    fun bind(fileName: String?) {
        if (asset != fileName) {
            releasePlayer(showLoading = false)
            asset = fileName
            failed = false
            renderedFirstFrame = false
            surface.alpha = 0f
            if (fileName != null) mediaState.showLoading("Loading video...")
        }
        visibility = if (fileName == null) GONE else VISIBLE
        if (fileName == null) {
            mediaState.visibility = GONE
        } else if (!failed && !renderedFirstFrame) {
            mediaState.showLoading("Loading video...")
        }
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
        releasePlayer(showLoading = false)
        super.onDetachedFromWindow()
    }

    private fun updatePlayback() {
        val fileName = asset ?: return
        val visible = isAttachedToWindow && isShown && windowVisibility == VISIBLE &&
            owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true &&
            getGlobalVisibleRect(visibleRect) &&
            visibleRect.height() >= height / 2 && visibleRect.width() >= width / 2
        if (!visible) {
            releasePlayer(showLoading = false)
            return
        }
        if (player != null || failed) return
        if (!tryRegisterActivePreview(this)) return
        if (!renderedFirstFrame) mediaState.showLoading("Loading video...")
        val next = ExoPlayer.Builder(context).build()
        player = next
        surface.player = next
        next.volume = 0f
        next.repeatMode = Player.REPEAT_MODE_ONE
        next.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                renderedFirstFrame = true
                surface.animate().alpha(1f).setDuration(160L).start()
                mediaState.showSuccess()
            }

            override fun onPlayerError(error: PlaybackException) {
                failed = true
                releasePlayer(showLoading = false)
                mediaState.showError("Video preview unavailable")
            }
        })
        val uri = when {
            fileName.startsWith("http://") || fileName.startsWith("https://") -> Uri.parse(fileName)
            fileName.startsWith("file://") -> Uri.parse(fileName)
            fileName.startsWith("/") -> Uri.fromFile(File(fileName))
            else -> Uri.parse("asset:///templates/${Uri.encode(fileName)}")
        }
        next.setMediaItem(MediaItem.fromUri(uri))
        next.prepare()
        next.playWhenReady = true
    }

    private fun releasePlayer(showLoading: Boolean) {
        surface.alpha = 0f
        surface.player = null
        player?.release()
        player = null
        unregisterActivePreview(this)
        if (asset != null && !failed && showLoading && !renderedFirstFrame) {
            mediaState.showLoading("Loading video...")
        } else if (renderedFirstFrame && !failed) {
            mediaState.visibility = GONE
        }
    }

    private companion object {
        private const val MAX_ACTIVE_PLAYERS = 2
        private val activePreviews = Collections.synchronizedList(mutableListOf<TemplateVideoPreview>())

        fun tryRegisterActivePreview(preview: TemplateVideoPreview): Boolean {
            synchronized(activePreviews) {
                if (activePreviews.contains(preview)) return true
                if (activePreviews.size >= MAX_ACTIVE_PLAYERS) return false
                activePreviews.add(preview)
                return true
            }
        }

        fun unregisterActivePreview(preview: TemplateVideoPreview) {
            synchronized(activePreviews) {
                activePreviews.remove(preview)
            }
        }
    }
}
