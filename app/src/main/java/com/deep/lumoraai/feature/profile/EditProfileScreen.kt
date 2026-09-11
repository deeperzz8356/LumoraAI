package com.deep.lumoraai.feature.profile

import android.net.Uri
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addPrimaryButton
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.dp
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    val profileRepository = remember { ProfileRepository() }
    var savedProfile by remember(user?.uid) { mutableStateOf(ProfilePreferences.load(context, user)) }
    var avatarUri by remember(savedProfile.avatarUri) { mutableStateOf(savedProfile.avatarUri) }
    var isSaving by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            avatarUri = ProfilePreferences.copyAvatarToPrivateStorage(context, uri) ?: uri.toString()
        }
    }

    LumoraXmlScreen(modifier = modifier) { binding ->
        binding.setupTopBar(
            titleText = "Edit Profile",
            subtitleText = "Update your creator details",
            onBack = onBack,
            actionIconRes = R.drawable.ic_lumora_check,
            onAction = {}
        )
        binding.resetContent()
        fun px(value: Int) = binding.root.dp(value)

        val avatar = ImageView(context).apply {
            setBackgroundResource(R.drawable.bg_profile_avatar)
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (avatarUri.isNullOrBlank()) setImageResource(R.drawable.user_avatar) else setImageURI(Uri.parse(avatarUri))
            setOnClickListener { imagePicker.launch("image/*") }
            isClickable = true
            isFocusable = true
        }
        binding.content.addView(avatar, LinearLayout.LayoutParams(px(104), px(104)).apply {
            setMargins(0, px(8), 0, px(18))
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        })

        binding.content.addSectionTitle("Profile")
        val fullName = binding.content.addField("Full name", savedProfile.fullName)
        val username = binding.content.addField("Username", savedProfile.username)
        val email = binding.content.addField("Email", savedProfile.email, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val bio = binding.content.addField("Bio", savedProfile.bio, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        val location = binding.content.addField("Location", savedProfile.location)

        fun currentDraft() = EditableProfile(
            fullName = fullName.text.toString().trim(),
            username = username.text.toString().trim().removePrefix("@"),
            email = email.text.toString().trim(),
            bio = bio.text.toString().trim(),
            location = location.text.toString().trim(),
            avatarUri = avatarUri,
        )

        fun saveProfile() {
            if (isSaving) return
            val draft = currentDraft()
            if (draft.fullName.isBlank()) {
                Toast.makeText(context, context.getString(R.string.full_name_required), Toast.LENGTH_LONG).show()
                return
            }
            if (!ProfilePreferences.isValidUsername(draft.username)) {
                Toast.makeText(context, context.getString(R.string.username_invalid), Toast.LENGTH_LONG).show()
                return
            }
            isSaving = true
            scope.launch {
                try {
                    val backendResult = profileRepository.updateCurrentUserProfile(draft)
                    val conflict = backendResult.exceptionOrNull() as? IllegalArgumentException
                    if (conflict != null) {
                        Toast.makeText(context, context.getString(R.string.username_taken), Toast.LENGTH_LONG).show()
                        isSaving = false
                        return@launch
                    }
                    backendResult.onFailure { error ->
                        android.util.Log.w("ProfileUpdate", "Backend update failed but continuing with local save", error)
                    }
                    ProfilePreferences.save(context, user, draft)
                    savedProfile = draft
                    if (user != null && !user.isAnonymous) {
                        runCatching {
                            val request = UserProfileChangeRequest.Builder()
                                .setDisplayName(draft.fullName)
                                .setPhotoUri(draft.avatarUri?.let(Uri::parse))
                                .build()
                            user.updateProfile(request).await()
                        }
                    }
                    Toast.makeText(context, context.getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                    onBack()
                } catch (error: Exception) {
                    Toast.makeText(context, context.getString(R.string.profile_save_failed), Toast.LENGTH_LONG).show()
                    android.util.Log.e("ProfileSave", "Failed to save profile", error)
                } finally {
                    isSaving = false
                }
            }
        }

        binding.content.addPrimaryButton(if (isSaving) "SAVING..." else "SAVE PROFILE", enabled = !isSaving, onClick = ::saveProfile)
        binding.actionButton.setOnClickListener { saveProfile() }
    }
}

private fun LinearLayout.addField(label: String, value: String, inputType: Int = InputType.TYPE_CLASS_TEXT): EditText {
    val field = EditText(context).apply {
        hint = label
        setText(value)
        this.inputType = inputType
        setTextColor(context.getColor(R.color.white))
        setHintTextColor(0xFF94A0B8.toInt())
        textSize = 15f
        setSingleLine(inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE == 0)
        setBackgroundResource(R.drawable.bg_generation_panel)
        setPadding(dp(14), dp(10), dp(14), dp(10))
    }
    addView(field, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(if (label == "Bio") 96 else 52)).apply {
        setMargins(0, dp(10), 0, 0)
    })
    return field
}
