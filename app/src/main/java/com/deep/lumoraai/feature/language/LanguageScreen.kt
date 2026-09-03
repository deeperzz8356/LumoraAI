package com.deep.lumoraai.feature.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.feature.language.model.LanguageModel
import androidx.compose.ui.res.stringResource
import com.deep.lumoraai.R

private val LanguageBackground = Color(0xFF081020)
private val LanguageTopBar = Color(0xFF0D1426)
private val LanguageRow = Color(0xFF23253C)
private val LanguageAccent = Color(0xFF6DE7EA)
private val LanguageDone = Color(0xFFD8FF2F)

@Composable
fun LanguageScreen(
    uiState: LanguageUiState,
    onLanguageSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LanguageBackground)
    ) {
        when (uiState) {
            LanguageUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = LanguageAccent
                )
            }
            is LanguageUiState.Success -> {
                LanguageContent(
                    state = uiState,
                    onLanguageSelected = onLanguageSelected,
                    onDone = onDone
                )
            }
        }
    }
}

@Composable
fun LanguageContent(
    state: LanguageUiState.Success,
    onLanguageSelected: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        LanguageTopBar(onDone = onDone)
        LanguageList(
            languages = state.languages,
            selectedLanguageCode = state.selectedLanguageCode,
            onLanguageSelected = onLanguageSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LanguageTopBar(onDone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(LanguageTopBar)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.language),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onDone,
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = LanguageDone),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
        ) {
            Text(
                text = stringResource(R.string.done),
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LanguageList(
    languages: List<LanguageModel>,
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(languages) { language ->
            LanguageItem(
                language = language,
                isSelected = language.code == selectedLanguageCode,
                onClick = { onLanguageSelected(language.code) }
            )
        }
    }
}

@Composable
fun LanguageItem(
    language: LanguageModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LanguageRow)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguageRadioButton(isSelected = isSelected)
        Spacer(modifier = Modifier.width(14.dp))
        LanguageFlagEmoji(emoji = language.flagEmoji)
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = language.name,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LanguageFlagEmoji(emoji: String) {
    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 24.sp)
    }
}

@Composable
fun LanguageRadioButton(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .border(1.5.dp, LanguageAccent.copy(alpha = if (isSelected) 1f else 0.65f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
