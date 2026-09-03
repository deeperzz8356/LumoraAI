package com.deep.lumoraai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.res.stringResource

/**
 * A reusable composable that displays an image with zoom, rotation, and scroll capabilities.
 * Supports pinch zoom, manual zoom controls, 90° rotation, and smooth scrolling.
 * Can be reused anywhere in the app.
 */
@Composable
fun ZoomableImageViewer(
    filePath: String,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    controlsBackgroundColor: Color = Color.Black.copy(alpha = 0.6f),
    enableGestureDetection: Boolean = true,
) {
    val file = File(filePath)
    
    // State management
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableIntStateOf(0) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clip(RectangleShape)
            .pointerInput(enableGestureDetection) {
                if (enableGestureDetection) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Only allow zoom in (scale > 1), min is 1f (original size)
                        if (zoom != 1f) {
                            scale = (scale * zoom).coerceIn(1f, 3f)  // Min: 1f (actual size), Max: 3f
                        }
                        
                        // Apply pan/scroll only if zoomed in
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                }
            }
    ) {
        // Image content
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = file,
                contentDescription = stringResource(com.deep.lumoraai.R.string.ui_zoomable_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                        rotationZ = rotation.toFloat()
                    }
                    .onSizeChanged { imageSize = it }
            )
        }

        // Control buttons
        if (showControls) {
            ControlsPanel(
                scale = scale,
                rotation = rotation,
                onZoomIn = { scale = (scale * 1.2f).coerceIn(1f, 3f) },
                onZoomOut = { scale = (scale / 1.2f).coerceIn(1f, 3f) },
                onRotate = { rotation = (rotation + 90) % 360 },
                onResetZoom = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                    rotation = 0
                },
                backgroundColor = controlsBackgroundColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * Control panel for image viewer with zoom in/out, rotate, and reset buttons.
 */
@Composable
private fun ControlsPanel(
    scale: Float,
    rotation: Int,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRotate: () -> Unit,
    onResetZoom: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row {
            IconButton(onClick = onZoomOut, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = stringResource(com.deep.lumoraai.R.string.ui_zoom_out),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onZoomIn, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(com.deep.lumoraai.R.string.ui_zoom_in),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRotate, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Rotate90DegreesCcw,
                    contentDescription = stringResource(com.deep.lumoraai.R.string.ui_rotate),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
