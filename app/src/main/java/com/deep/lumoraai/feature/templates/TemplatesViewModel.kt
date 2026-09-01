package com.deep.lumoraai.feature.templates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TemplatesViewModel : ViewModel() {
    var uiState: TemplatesUiState by mutableStateOf(
        TemplatesUiState.Success(
            imageTemplates = realImageTemplates,
            videoTemplates = realVideoTemplates,
        )
    )
        private set
}
