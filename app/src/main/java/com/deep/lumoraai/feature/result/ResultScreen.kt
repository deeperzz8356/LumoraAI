package com.deep.lumoraai.feature.result

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.core.components.ZoomableImageViewer
import com.deep.lumoraai.core.components.ZoomableVideoPlayer
import com.deep.lumoraai.core.utils.HistoryFeedbackReporter
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.data.repository.GenerationRepository
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(path: String, mediaType: String, mimeType: String, firstNewJobIndex: Int = -1, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val jobs by GenerationRepository.activeJobs.collectAsState()
    val generatedJob = remember(jobs, firstNewJobIndex, mediaType) {
        if (firstNewJobIndex < 0) null else jobs
            .drop(firstNewJobIndex)
            .firstOrNull { it.mediaType.equals(mediaType, ignoreCase = true) }
    }
    val resultPath = path.ifBlank { generatedJob?.localMediaPath.orEmpty() }
    val effectiveMediaType = generatedJob?.mediaType ?: mediaType
    val isVideo = effectiveMediaType.equals("VIDEO", true) || mimeType.startsWith("video/")
    val isAwaitingNewJob = path.isBlank() && firstNewJobIndex >= 0
    val generationFailed = isAwaitingNewJob && generatedJob?.statusText.equals("Failed", ignoreCase = true)
    val isGenerating = isAwaitingNewJob && !generationFailed && (generatedJob == null || !generatedJob.isCompleted || resultPath.isBlank())
    var showViewer by remember { mutableStateOf(false) }
    var showReport by rememberSaveable(path) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().systemBarsPadding().background(Color(0xFF081020)).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Icon(TablerIcons.ArrowLeft, null); Spacer(Modifier.width(8.dp)); Text("Back") }
        Text(if (isGenerating) "Creating your ${if (isVideo) "video" else "image"}" else if (isVideo) "Video Ready" else "Image Ready", color = Color.White)
        Box(Modifier.fillMaxWidth().height(420.dp), contentAlignment = Alignment.Center) {
            if (generationFailed) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(TablerIcons.AlertCircle, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(64.dp))
                    Text("Generation failed", color = Color.White)
                    Text(generatedJob?.subtitle.orEmpty(), color = Color.White.copy(alpha = 0.72f))
                    Button(onClick = onBack, colors = resultButtonColors()) { Text("Back to generator") }
                }
            } else if (isGenerating) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val progress = generatedJob?.progressPercent
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
                        if (progress != null) CircularProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, color = Color(0xFFD4FF3B), modifier = Modifier.fillMaxSize())
                        else CircularProgressIndicator(color = Color(0xFFD4FF3B), modifier = Modifier.fillMaxSize())
                        Text("${((progress ?: 0f).coerceIn(0f, 1f) * 100).toInt()}%", color = Color.White, fontSize = 18.sp)
                    }
                    Text(generatedJob?.statusText?.ifBlank { null } ?: "Starting generation…", color = Color.White)
                }
            } else if (resultPath.isBlank() || !File(resultPath).exists()) Text("Result preview unavailable", color = Color.White)
            else if (showViewer) Text("Viewing full screen", color = Color.White)
            else if (isVideo) ZoomableVideoPlayer(resultPath)
            else ZoomableImageViewer(resultPath)
        }
        if (!isGenerating && resultPath.isNotBlank()) {
            Button(
                onClick = { showViewer = true },
                colors = resultButtonColors(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Icon(TablerIcons.Maximize, null); Spacer(Modifier.width(8.dp)); Text("Full screen") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                colors = resultButtonColors(), onClick = { scope.launch {
                val result = MediaGallerySaver.saveToGallery(context, resultPath, mimeType, effectiveMediaType)
                Toast.makeText(context, result.getOrElse { it.message ?: "Could not save media" }, Toast.LENGTH_SHORT).show()
            } }) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(TablerIcons.Download, null, modifier = Modifier.size(18.dp)); Text("Save", maxLines = 1, softWrap = false, fontSize = 12.sp) } }
            Button(
                modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                colors = resultButtonColors(), onClick = { MediaShareUtils.shareMedia(context, resultPath, mimeType) }) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(TablerIcons.Share, null, modifier = Modifier.size(18.dp)); Text("Share", maxLines = 1, softWrap = false, fontSize = 12.sp) } }
            Button(
                modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                onClick = { showReport = true }) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(TablerIcons.Flag, null, modifier = Modifier.size(18.dp)); Text("Report", maxLines = 1, softWrap = false, fontSize = 12.sp) } }
            }
        }
    }
    if (showViewer) MediaViewerDialog(filePath = resultPath, mediaType = if (isVideo) "VIDEO" else "IMAGE", mimeType = mimeType, onDismiss = { showViewer = false })
    if (showReport) ReportResultDialog(
        onDismiss = { showReport = false },
        onSubmit = { reason, details ->
            runCatching {
                HistoryFeedbackReporter.submit(context, HistoryModel(resultPath, "Generated result", Instant.now().toString(), effectiveMediaType, resultPath), reason, details)
            }.onSuccess {
                showReport = false
                Toast.makeText(context, "Report submitted", Toast.LENGTH_SHORT).show()
            }
        },
    )
}

@Composable
private fun resultButtonColors() = ButtonDefaults.buttonColors(
    containerColor = Color(0xFFD4FF3B), contentColor = Color(0xFF081020),
)
