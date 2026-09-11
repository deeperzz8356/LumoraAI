package com.deep.lumoraai.feature.result

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

@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(
        selectedTab = "result",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        val context = binding.root.context
        binding.setupTopBar(
            titleText = context.getString(R.string.ui_result_2),
            subtitleText = context.getString(R.string.ui_result),
            onBack = onBack,
        )
        binding.resetContent()
        when (uiState) {
            ResultUiState.Loading -> binding.content.addTextCard(context.getString(R.string.loading), "Please wait.")
            ResultUiState.Empty -> binding.content.addTextCard(context.getString(R.string.ui_result), "No fake data yet.")
            is ResultUiState.Error -> binding.content.addTextCard("Error", uiState.message)
            is ResultUiState.Success -> {
                binding.content.addSectionTitle(context.getString(R.string.ui_result))
                uiState.items.forEach { item ->
                    binding.content.addActionRow(
                        title = item,
                        subtitle = "This card shows compile-time placeholder data.",
                        icon = R.drawable.ic_lumora_image
                    )
                }
                binding.content.addPrimaryButton("Continue", onClick = onNext)
            }
        }
    }
}
