package com.deep.lumoraai.feature.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.feature.language.model.LanguageModel

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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1026),
                        Color(0xFF070714)
                    )
                )
            )
    ) {
        when (uiState) {
            LanguageUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF7E50EF)
                )
            }
            is LanguageUiState.Success -> {
                LanguageContent(
                    state = uiState,
                    onLanguageSelected = onLanguageSelected,
                    onSearchQueryChanged = onSearchQueryChanged,
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
    onSearchQueryChanged: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LanguageHeaderSection(state.searchQuery, onSearchQueryChanged)
        LanguageList(
            languages = state.languages,
            selectedLanguageCode = state.selectedLanguageCode,
            query = state.searchQuery,
            onLanguageSelected = onLanguageSelected,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LanguageDoneButton(onDone = onDone)
    }
}

@Composable
fun LanguageHeaderSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    LanguageHeader()
    Spacer(modifier = Modifier.height(24.dp))
    LanguageSearchBar(query = query, onQueryChange = onQueryChange)
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "Languages",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
fun LanguageHeader() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text("✦", color = Color(0xFFADF021), fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart))
        Text("✦", color = Color(0xFFADF021), fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterEnd))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row {
                Text("Select ", color = Color(0xFFA855F7), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Language", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.size(80.dp, 3.dp).background(Color(0xFFA855F7)))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Choose Your Preferred Language for a\nPersonalized Experience.",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LanguageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Search Languages", color = Color(0xFF94A3B8)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF94A3B8)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.6f),
            unfocusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.6f),
            focusedBorderColor = Color(0xFFA855F7).copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun LanguageList(
    languages: List<LanguageModel>,
    selectedLanguageCode: String,
    query: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = remember(languages, query) {
        languages.filter { it.name.contains(query, ignoreCase = true) }
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filtered) { language ->
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
    val border = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161838))
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguageFlagEmoji(emoji = language.flagEmoji)
        Text(
            text = language.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        LanguageRadioButton(isSelected = isSelected)
    }
}

@Composable
fun LanguageFlagEmoji(emoji: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 24.sp)
    }
}

@Composable
fun LanguageRadioButton(isSelected: Boolean) {
    val outer = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .size(20.dp)
            .border(2.dp, outer, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFA855F7))
            )
        }
    }
}

@Composable
fun LanguageDoneButton(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onDone,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E50EF))
    ) {
        Text(
            text = "Done",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
