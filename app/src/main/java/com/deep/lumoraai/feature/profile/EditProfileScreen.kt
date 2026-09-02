package com.deep.lumoraai.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppToolbar

// Theme colors matching Home/Profile pages
private val EditBackground = Color(0xFF081020)
private val EditCard = Color(0xFF10192D)
private val EditStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fullName = remember { mutableStateOf("Alex Thorne") }
    val username = remember { mutableStateOf("alexthorne_creatives") }
    val email = remember { mutableStateOf("alex@lumora.ai") }
    val bio = remember { mutableStateOf("Concept Artist | Video Director") }
    val location = remember { mutableStateOf("San Francisco, CA") }
    val website = remember { mutableStateOf("alexthorne.com") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EditBackground,
        topBar = {
            AppToolbar(
                title = "Edit Profile",
                action = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EditBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Picture Section
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(100.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Lime, CircleShape)
                        .background(Color.Black)
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.user_avatar),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = Purple,
                    border = BorderStroke(2.dp, EditBackground)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Full Name
            Text("Full Name", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = fullName.value,
                onValueChange = { fullName.value = it },
                placeholder = "Your full name"
            )

            // Username
            Text("Username", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = username.value,
                onValueChange = { username.value = it },
                placeholder = "@username"
            )

            // Email
            Text("Email Address", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = email.value,
                onValueChange = { email.value = it },
                placeholder = "email@example.com",
                keyboardType = KeyboardType.Email
            )

            // Bio
            Text("Bio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = bio.value,
                onValueChange = { bio.value = it },
                placeholder = "Tell us about yourself",
                maxLines = 3
            )

            // Location
            Text("Location", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = location.value,
                onValueChange = { location.value = it },
                placeholder = "City, Country"
            )

            // Website
            Text("Website", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = website.value,
                onValueChange = { website.value = it },
                placeholder = "yourwebsite.com"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Surface(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CardShape,
                color = Lime
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Save Changes",
                        color = EditBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Cancel Button
            Surface(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CardShape,
                color = EditCard,
                border = BorderStroke(1.dp, EditStroke.copy(alpha = 0.72f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Cancel",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EditProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (maxLines > 1) 100.dp else 56.dp),
        placeholder = {
            Text(placeholder, color = Muted.copy(alpha = 0.6f), fontSize = 14.sp)
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = EditCard,
            unfocusedContainerColor = EditCard,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Lime,
            unfocusedIndicatorColor = EditStroke,
            cursorColor = Lime
        ),
        shape = CardShape,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        maxLines = maxLines,
        singleLine = maxLines == 1,
        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
            fontSize = 14.sp,
            color = Color.White
        )
    )
}
