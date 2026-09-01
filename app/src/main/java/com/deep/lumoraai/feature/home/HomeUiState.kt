package com.deep.lumoraai.feature.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val userName: String,
        val credits: Int = 0,
        val creationCount: Int = 0,
        val planLabel: String = "Free",
        val recentItems: List<HomeRecentItem> = emptyList(),
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Empty : HomeUiState
}

data class HomeRecentItem(
    val id: String,
    val title: String,
    val timeLabel: String,
    val mediaType: String,
    val mediaUrl: String? = null,
    val fallbackImageRes: Int,
)
