package com.deep.lumoraai.feature.compress

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CompressRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: CompressViewModel = viewModel()
) {
    CompressScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onNavigate = onNavigate,
        onFileSelected = viewModel::loadFile,
        onCompress = viewModel::compress,
        onDownload = viewModel::saveResultToDownloads,
        onReset = viewModel::reset,
    )
}
