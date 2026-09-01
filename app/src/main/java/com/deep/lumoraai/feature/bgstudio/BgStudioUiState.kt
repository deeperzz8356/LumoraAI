package com.deep.lumoraai.feature.bgstudio

import android.graphics.Bitmap

enum class BgStudioMode(val label: String) {
    Remove("Remove"),
    Replace("Replace"),
}

sealed interface BgStudioStatus {
    data object Idle : BgStudioStatus
    data object LoadingImage : BgStudioStatus
    data object Generating : BgStudioStatus
    data object TrialExpired : BgStudioStatus
    data class Error(val message: String) : BgStudioStatus
}

data class BgStudioUiState(
    val mode: BgStudioMode = BgStudioMode.Replace,
    val prompt: String = "",
    val sourceBitmap: Bitmap? = null,
    val status: BgStudioStatus = BgStudioStatus.Idle,
)
