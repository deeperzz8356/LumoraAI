package com.deep.lumoraai.feature.result

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.core.components.ZoomableImageViewer
import com.deep.lumoraai.core.components.ZoomableVideoPlayer
import com.deep.lumoraai.core.utils.HistoryFeedbackReporter
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.data.model.HistoryModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

@Composable
fun ResultScreen(path: String, mediaType: String, mimeType: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isVideo = mediaType.equals("VIDEO", true) || mimeType.startsWith("video/")
    var showViewer by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color(0xFF081020)).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text(if (isVideo) "Video Ready" else "Image Ready", color = Color.White)
        Box(Modifier.fillMaxWidth().height(420.dp)) {
            if (path.isBlank() || !File(path).exists()) Text("Result preview unavailable", color = Color.White)
            else if (showViewer) Text("Viewing full screen", color = Color.White)
            else if (isVideo) ZoomableVideoPlayer(path)
            else ZoomableImageViewer(path)
        }
        Button(onClick = { showViewer = true }) { Text("Full screen") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scope.launch {
                val result = MediaGallerySaver.saveToGallery(context, path, mimeType, mediaType)
                Toast.makeText(context, result.getOrElse { it.message ?: "Could not save media" }, Toast.LENGTH_SHORT).show()
            } }) { Text("Save") }
            Button(onClick = { MediaShareUtils.shareMedia(context, path, mimeType) }) { Text("Share") }
            Button(onClick = { showReport = true }) { Text("Report") }
        }
    }
    if (showViewer) MediaViewerDialog(filePath = path, mediaType = if (isVideo) "VIDEO" else "IMAGE", mimeType = mimeType, onDismiss = { showViewer = false })
    if (showReport) AlertDialog(
        onDismissRequest = { showReport = false },
        title = { Text("Report result") },
        text = { Text("Tell us why this result should be reviewed.") },
        confirmButton = { TextButton(onClick = {
            HistoryFeedbackReporter.submit(context, HistoryModel(path, "Generated result", Instant.now().toString(), mediaType, path), "Inappropriate result")
            showReport = false
            Toast.makeText(context, "Report submitted", Toast.LENGTH_SHORT).show()
        }) { Text("Report") } },
        dismissButton = { TextButton(onClick = { showReport = false }) { Text("Cancel") } },
    )
}
