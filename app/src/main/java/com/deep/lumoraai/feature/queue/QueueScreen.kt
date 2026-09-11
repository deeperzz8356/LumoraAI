package com.deep.lumoraai.feature.queue

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addPrimaryButton
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.core.navigation.Screen

@Composable
fun QueueScreen(
    uiState: QueueUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(
        selectedTab = "queue",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        binding.setupTopBar(
            titleText = binding.root.context.getString(R.string.ui_queue),
            subtitleText = "Live status of your creative jobs",
            onBack = null,
            actionIconRes = R.drawable.ic_lumora_history,
            onAction = { onNavigate(Screen.History.route) }
        )
        binding.resetContent()
        binding.content.addTextCard("Active Jobs", "Real-time status of your creative generations.")
        when (uiState) {
            QueueUiState.Loading -> binding.content.addTextCard("Loading", "Checking current jobs.")
            QueueUiState.Empty -> {
                binding.content.addTextCard("All clear", "No active generations are running right now.")
                binding.content.addPrimaryButton("START CREATING") { onNavigate(Screen.CreateHub.route) }
            }
            is QueueUiState.Error -> binding.content.addTextCard("Error", uiState.message)
            is QueueUiState.Success -> {
                binding.content.addSectionTitle("In Progress")
                uiState.items.forEach { job ->
                    val progress = job.progressPercent?.let { "${(it * 100).toInt().coerceIn(0, 100)}%" }
                    binding.content.addActionRow(
                        title = job.title,
                        subtitle = listOfNotNull(job.badgeText, job.statusText, progress, job.subtitle)
                            .filter { it.isNotBlank() }
                            .joinToString("  |  "),
                        icon = if (job.mediaType.equals("VIDEO", true)) R.drawable.ic_lumora_video else R.drawable.ic_lumora_image,
                        onClick = if (job.isCompleted) ({ onNavigate(Screen.History.route) }) else null
                    )
                }
            }
        }
    }
}
