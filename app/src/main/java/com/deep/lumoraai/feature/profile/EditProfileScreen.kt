package com.deep.lumoraai.feature.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.PremiumBackground
import com.deep.lumoraai.core.components.PremiumLime
import com.deep.lumoraai.core.components.PremiumMuted
import com.deep.lumoraai.core.components.PremiumPage
import com.deep.lumoraai.core.components.PremiumSection
import com.deep.lumoraai.core.components.PremiumStroke
import com.deep.lumoraai.core.components.PremiumSurface
import com.deep.lumoraai.core.components.PremiumText
import com.deep.lumoraai.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    val repository = remember { ProfileRepository() }
    val saved = remember(user?.uid) { ProfilePreferences.load(context, user) }
    var fullName by remember { mutableStateOf(saved.fullName) }
    var username by remember { mutableStateOf(saved.username) }
    var email by remember { mutableStateOf(saved.email) }
    var bio by remember { mutableStateOf(saved.bio) }
    var location by remember { mutableStateOf(saved.location) }
    var avatarUri by remember(saved.avatarUri) { mutableStateOf(saved.avatarUri) }
    var isSaving by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) avatarUri = ProfilePreferences.copyAvatarToPrivateStorage(context, uri) ?: uri.toString()
    }

    fun saveProfile() {
        if (isSaving) return
        val draft = EditableProfile(
            fullName = fullName.trim(),
            username = username.trim().removePrefix("@"),
            email = email.trim(),
            bio = bio.trim(),
            location = location.trim(),
            avatarUri = avatarUri,
        )
        validationError = when {
            draft.fullName.isBlank() -> context.getString(R.string.full_name_required)
            !ProfilePreferences.isValidUsername(draft.username) -> context.getString(R.string.username_invalid)
            else -> null
        }
        if (validationError != null) return
        isSaving = true
        scope.launch {
            try {
                val result = repository.updateCurrentUserProfile(draft)
                if (result.exceptionOrNull() is IllegalArgumentException) {
                    validationError = context.getString(R.string.username_taken)
                    return@launch
                }
                result.onFailure { android.util.Log.w("ProfileUpdate", "Backend update failed; saving locally", it) }
                ProfilePreferences.save(context, user, draft)
                if (user != null && !user.isAnonymous) {
                    runCatching {
                        user.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(draft.fullName)
                                .setPhotoUri(draft.avatarUri?.let(Uri::parse))
                                .build(),
                        ).await()
                    }
                }
                Toast.makeText(context, context.getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                onBack()
            } catch (error: Exception) {
                validationError = context.getString(R.string.profile_save_failed)
                android.util.Log.e("ProfileSave", "Failed to save profile", error)
            } finally {
                isSaving = false
            }
        }
    }

    PremiumPage("Edit profile", "Creator identity", "Update how you appear across Lumora", onBack, modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = PremiumSurface,
            border = BorderStroke(1.dp, PremiumStroke.copy(alpha = 0.75f)),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(PremiumStroke)
                        .clickable { imagePicker.launch("image/*") }
                        .semantics { contentDescription = "Change profile picture"; role = Role.Button },
                ) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Profile picture",
                        placeholder = painterResource(R.drawable.user_avatar),
                        error = painterResource(R.drawable.user_avatar),
                        modifier = Modifier.fillMaxWidth().height(112.dp),
                    )
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).size(38.dp).clip(CircleShape).background(PremiumLime),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = PremiumBackground, modifier = Modifier.size(19.dp))
                    }
                }
                Text("Tap to change your portrait", color = PremiumMuted, fontSize = 12.sp)
            }
        }
        PremiumSection("Profile details", "Keep your creator identity recognizable") {
            ProfileField("Full name", fullName, { fullName = it }, Icons.Default.Person)
            ProfileField("Username", username, { username = it }, Icons.Default.Tag, prefix = "@")
            ProfileField("Email", email, { email = it }, Icons.Default.Person, keyboardType = KeyboardType.Email)
            ProfileField("Bio", bio, { bio = it }, Icons.AutoMirrored.Filled.ShortText, singleLine = false)
            ProfileField("Location", location, { location = it }, Icons.Default.LocationOn)
        }
        validationError?.let {
            Surface(color = Color(0x22FF7D88), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0x55FF7D88))) {
                Text(it, color = Color(0xFFFFA6AE), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(13.dp))
            }
        }
        Button(
            onClick = ::saveProfile,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PremiumLime, contentColor = PremiumBackground),
        ) {
            Icon(Icons.Default.Check, null, modifier = Modifier.size(19.dp))
            Text(if (isSaving) "Saving…" else "Save profile", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        prefix = prefix?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PremiumText,
            unfocusedTextColor = PremiumText,
            focusedLabelColor = PremiumLime,
            unfocusedLabelColor = PremiumMuted,
            focusedLeadingIconColor = PremiumLime,
            unfocusedLeadingIconColor = PremiumMuted,
            focusedBorderColor = PremiumLime,
            unfocusedBorderColor = PremiumStroke,
            cursorColor = PremiumLime,
        ),
    )
}
