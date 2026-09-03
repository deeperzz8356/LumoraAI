package com.deep.lumoraai.feature.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    val profileRepository = remember { ProfileRepository() }
    val savedProfile = remember(user?.uid) { ProfilePreferences.load(context, user) }
    var fullName by remember(savedProfile) { mutableStateOf(savedProfile.fullName) }
    var username by remember(savedProfile) { mutableStateOf(savedProfile.username) }
    var email by remember(savedProfile) { mutableStateOf(savedProfile.email) }
    var bio by remember(savedProfile) { mutableStateOf(savedProfile.bio) }
    var location by remember(savedProfile) { mutableStateOf(savedProfile.location) }
    var avatarUri by remember(savedProfile) { mutableStateOf(savedProfile.avatarUri) }
    var pendingAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val draftProfile = EditableProfile(
        fullName = fullName.trim(),
        username = username.trim().removePrefix("@"),
        email = email.trim(),
        bio = bio.trim(),
        location = location.trim(),
        avatarUri = avatarUri,
    )
    val originalProfile = savedProfile.copy(
        fullName = savedProfile.fullName.trim(),
        username = savedProfile.username.trim().removePrefix("@"),
        email = savedProfile.email.trim(),
        bio = savedProfile.bio.trim(),
        location = savedProfile.location.trim(),
    )
    val hasChanges = draftProfile != originalProfile
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        pendingAvatarUri = uri
    }
    fun saveProfile() {
        val cleanName = fullName.trim()
        if (!hasChanges || isSaving) return
        if (cleanName.isBlank()) {
            Toast.makeText(context, "Full name is required.", Toast.LENGTH_LONG).show()
            return
        }
        if (!ProfilePreferences.isValidUsername(username)) {
            Toast.makeText(context, "Username must be 3-30 characters and use letters, numbers, dots, or underscores.", Toast.LENGTH_LONG).show()
            return
        }
        isSaving = true
        scope.launch {
            try {
                // Save locally first
                ProfilePreferences.save(context, user, draftProfile)
                
                // Try to update backend (but don't fail if this doesn't work)
                profileRepository.updateCurrentUserProfile(draftProfile).fold(
                    onSuccess = { 
                        // Backend update successful
                    },
                    onFailure = { error ->
                        // Log error but don't show it to user since local save worked
                        android.util.Log.w("ProfileUpdate", "Backend update failed but local save succeeded", error)
                    }
                )
                
                // Update Firebase user profile if possible
                if (user != null && !user.isAnonymous) {
                    runCatching {
                        val request = UserProfileChangeRequest.Builder()
                            .setDisplayName(draftProfile.fullName)
                            .setPhotoUri(draftProfile.avatarUri?.let(Uri::parse))
                            .build()
                        user.updateProfile(request).await()
                    }
                }
                
                Toast.makeText(context, "Profile saved successfully.", Toast.LENGTH_SHORT).show()
                onBack()
            } catch (error: Exception) {
                Toast.makeText(context, "Could not save profile. Please try again.", Toast.LENGTH_LONG).show()
                android.util.Log.e("ProfileSave", "Failed to save profile", error)
            } finally {
                isSaving = false
            }
        }
    }

    pendingAvatarUri?.let { cropUri ->
        var cropZoom by remember(cropUri) { mutableStateOf(1f) }
        var cropOffset by remember(cropUri) { mutableStateOf(Offset.Zero) }
        var cropViewport by remember(cropUri) { mutableStateOf(IntSize.Zero) }
        AlertDialog(
            onDismissRequest = { pendingAvatarUri = null },
            containerColor = EditCard,
            title = {
                Text("Crop profile image", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                            .onSizeChanged { cropViewport = it }
                            .pointerInput(cropUri) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    cropZoom = (cropZoom * zoom).coerceIn(1f, 5f)
                                    cropOffset += pan
                                    val limit = cropViewport.width * 0.45f * cropZoom
                                    cropOffset = Offset(
                                        x = cropOffset.x.coerceIn(-limit, limit),
                                        y = cropOffset.y.coerceIn(-limit, limit)
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = cropUri,
                            contentDescription = "1:1 profile crop preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = cropZoom
                                    scaleY = cropZoom
                                    translationX = cropOffset.x
                                    translationY = cropOffset.y
                                }
                        )
                    }
                    Slider(
                        value = cropZoom,
                        onValueChange = { cropZoom = it.coerceIn(1f, 5f) },
                        valueRange = 1f..5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Lime,
                            activeTrackColor = Lime,
                            inactiveTrackColor = Muted.copy(alpha = 0.35f)
                        )
                    )
                    Text("Pinch, drag, or zoom. This square 1:1 crop will be used for your profile photo.", color = Muted, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val copied = ProfilePreferences.copyAvatarToPrivateStorage(
                            context = context,
                            sourceUri = cropUri,
                            viewportSize = cropViewport.width,
                            zoom = cropZoom,
                            offsetX = cropOffset.x,
                            offsetY = cropOffset.y,
                        )
                        if (copied != null) {
                            avatarUri = copied
                        } else {
                            Toast.makeText(context, "Could not load the selected image.", Toast.LENGTH_LONG).show()
                        }
                        pendingAvatarUri = null
                    }
                ) {
                    Text("Use Crop", color = Lime, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAvatarUri = null }) {
                    Text("Cancel", color = Muted)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EditBackground,
        topBar = {
            AppToolbar(
                title = stringResource(com.deep.lumoraai.R.string.ui_edit_profile),
                onBackClick = onBack,
                action = {
                    IconButton(onClick = { saveProfile() }, enabled = hasChanges && !isSaving) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save profile",
                            tint = if (hasChanges && !isSaving) Lime else Color.White.copy(alpha = 0.35f)
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
                    .clickable { imagePicker.launch("image/*") }
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Lime, CircleShape)
                        .background(Color.Black)
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_profile),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.user_avatar),
                            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_profile),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_change_photo),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Full Name
            Text(stringResource(com.deep.lumoraai.R.string.ui_full_name), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = stringResource(com.deep.lumoraai.R.string.ui_your_full_name)
            )

            // Username
            Text(stringResource(com.deep.lumoraai.R.string.ui_username), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = stringResource(com.deep.lumoraai.R.string.ui_username_2)
            )

            // Email
            Text(stringResource(com.deep.lumoraai.R.string.ui_email_address), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(com.deep.lumoraai.R.string.ui_email_example_com),
                keyboardType = KeyboardType.Email
            )

            // Bio
            Text(stringResource(com.deep.lumoraai.R.string.ui_bio), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = bio,
                onValueChange = { bio = it },
                placeholder = stringResource(com.deep.lumoraai.R.string.ui_tell_us_about_yourself),
                maxLines = 3
            )

            // Location
            Text(stringResource(com.deep.lumoraai.R.string.ui_location), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            EditProfileTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = stringResource(com.deep.lumoraai.R.string.ui_city_country)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Save Button
            Surface(
                onClick = { saveProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CardShape,
                color = if (hasChanges && !isSaving) Lime else Lime.copy(alpha = 0.35f),
                enabled = hasChanges && !isSaving
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        if (isSaving) "Saving..." else "Save Changes",
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
