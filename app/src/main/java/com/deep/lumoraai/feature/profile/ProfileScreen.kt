package com.deep.lumoraai.feature.profile

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.data.model.HistoryModel
import com.deep.lumoraai.databinding.ProfileItemCreationBinding
import com.deep.lumoraai.databinding.ProfileItemPrefBinding
import com.deep.lumoraai.databinding.ProfileMediaViewerBinding
import com.deep.lumoraai.databinding.ProfileScreenBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF081020),
        bottomBar = { BottomNavigationBar(emptyList(), "profile", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF081020))
                .padding(padding)
        ) {
            AndroidView(
                factory = { ProfileScreenBinding.inflate(LayoutInflater.from(it)).root },
                update = { root ->
                    bindProfile(
                        binding = ProfileScreenBinding.bind(root),
                        uiState = uiState,
                        onSignOut = onSignOut,
                        onDeleteAccount = onDeleteAccount,
                        onNavigate = onNavigate,
                        onBack = onBack,
                        unreadCount = unreadCount,
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun bindProfile(
    binding: ProfileScreenBinding,
    uiState: ProfileUiState,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    unreadCount: Int,
) {
    binding.backButton.setOnClickListener { onBack() }
    binding.unreadDot.visibility = if (unreadCount > 0) View.VISIBLE else View.GONE
    binding.notificationButton.setOnClickListener { onNavigate(Screen.Notifications.route) }

    when (uiState) {
        ProfileUiState.Loading -> bindLoading(binding)
        ProfileUiState.Empty -> bindEmpty(binding)
        is ProfileUiState.Error -> bindError(binding, uiState.message)
        is ProfileUiState.Success -> bindSuccess(binding, uiState, onSignOut, onDeleteAccount, onNavigate)
    }
}

private fun bindLoading(binding: ProfileScreenBinding) {
    binding.name.text = binding.root.context.getString(R.string.loading)
    binding.subtitle.text = ""
    binding.plan.text = ""
    binding.credits.text = "0"
    binding.generationCount.text = "0"
    binding.creationsGrid.removeAllViews()
    binding.preferencesList.removeAllViews()
}

private fun bindEmpty(binding: ProfileScreenBinding) {
    binding.name.text = binding.root.context.getString(R.string.ui_profile_2)
    binding.subtitle.text = binding.root.context.getString(R.string.ui_no_content)
}

private fun bindError(binding: ProfileScreenBinding, message: String) {
    binding.name.text = binding.root.context.getString(R.string.ui_profile_2)
    binding.subtitle.text = message
}

private fun bindSuccess(
    binding: ProfileScreenBinding,
    state: ProfileUiState.Success,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val context = binding.root.context
    val user = FirebaseAuth.getInstance().currentUser
    val savedProfile = ProfilePreferences.load(context, user)
    val displayName = savedProfile.fullName.ifBlank { state.items.getOrNull(0).orEmpty() }
    val displaySubtitle = "@${savedProfile.username.ifBlank { state.items.getOrNull(1).orEmpty().removePrefix("@") }}"
    val plan = state.items.getOrNull(2).orEmpty()
    binding.name.text = displayName.ifBlank { context.getString(R.string.ui_lumora_creator) }
    binding.subtitle.text = displaySubtitle.ifBlank { plan }
    binding.plan.text = plan
    binding.plan.visibility = if (plan.isBlank()) View.GONE else View.VISIBLE
    if (savedProfile.avatarUri.isNullOrBlank()) {
        binding.avatar.setImageResource(R.drawable.user_avatar)
    } else {
        binding.avatar.setImageURI(Uri.parse(savedProfile.avatarUri))
    }
    binding.credits.text = state.credits.toString()
    binding.creditsCard.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.generationCount.text = state.generations.size.toString()
    binding.creationCountCard.setOnClickListener { onNavigate(Screen.History.route) }
    binding.subscriptionCard.setOnClickListener { onNavigate(Screen.Subscription.route) }
    binding.editProfileButton.setOnClickListener {
        onNavigate(if (user == null || user.isAnonymous) Screen.Auth.route else Screen.EditProfile.route)
    }
    binding.shareButton.setOnClickListener {
        val text = "${displayName.ifBlank { "Lumora Creator" }} on LumoraAI"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.ui_share)))
    }
    bindCreations(binding, state.generations, onNavigate)
    bindPreferences(binding, state.isGuest, onSignOut, onDeleteAccount, onNavigate)
}

private fun bindCreations(
    binding: ProfileScreenBinding,
    generations: List<HistoryModel>,
    onNavigate: (String) -> Unit,
) {
    binding.viewAllCreations.setOnClickListener { onNavigate(Screen.History.route) }
    binding.creationsGrid.removeAllViews()
    if (generations.isEmpty()) {
        val context = binding.root.context
        val empty = android.widget.TextView(context).apply {
            text = context.getString(R.string.ui_start_your_first_creation) + "\n" + context.getString(R.string.ui_generate_an_image_or_video_from_home)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setPadding(dp(this, 14), dp(this, 14), dp(this, 14), dp(this, 14))
            setBackgroundResource(R.drawable.bg_common_card)
            setOnClickListener { onNavigate(Screen.TextToImage.route) }
        }
        binding.creationsGrid.addView(empty, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(binding.root, 76)))
        return
    }
    generations.take(4).forEachIndexed { index, item ->
        val card = ProfileItemCreationBinding.inflate(LayoutInflater.from(binding.root.context), binding.creationsGrid, false)
        val isVideo = item.type.equals("VIDEO", ignoreCase = true)
        bindMediaThumb(card.mediaImage, item, if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy)
        card.playBadge.visibility = if (isVideo) View.VISIBLE else View.GONE
        card.root.setOnClickListener {
            if (!item.mediaUrl.isNullOrBlank() && File(item.mediaUrl.orEmpty()).exists()) {
                showProfileMediaDialog(binding.root, item)
            }
        }
        binding.creationsGrid.addView(card.root, gridParams(index, binding.root, 116, 10))
    }
}

private fun showProfileMediaDialog(anchor: View, item: HistoryModel) {
    val context = anchor.context
    val viewer = ProfileMediaViewerBinding.inflate(LayoutInflater.from(context))
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val file = File(item.mediaUrl.orEmpty())
    viewer.mediaFrame.layoutParams = viewer.mediaFrame.layoutParams.apply {
        height = (context.resources.displayMetrics.heightPixels * 0.62f).toInt().coerceAtMost(dp(anchor, 420))
    }
    viewer.mediaTitle.text = item.title.ifBlank { if (isVideo) "Video" else "Image" }
    viewer.missingText.visibility = if (file.exists()) View.GONE else View.VISIBLE
    viewer.mediaImage.visibility = if (file.exists() && !isVideo) View.VISIBLE else View.GONE
    viewer.mediaVideo.visibility = if (file.exists() && isVideo) View.VISIBLE else View.GONE
    if (file.exists() && isVideo) {
        viewer.mediaVideo.setVideoURI(Uri.fromFile(file))
        viewer.mediaVideo.setOnPreparedListener { player ->
            player.isLooping = true
            viewer.mediaVideo.start()
        }
    } else if (file.exists()) {
        viewer.mediaImage.setImageURI(Uri.fromFile(file))
    }
    val dialog = AlertDialog.Builder(context)
        .setView(viewer.root)
        .setNegativeButton(R.string.ui_cancel, null)
        .show()
    dialog.setOnDismissListener { viewer.mediaVideo.stopPlayback() }
}

private fun bindPreferences(
    binding: ProfileScreenBinding,
    isGuest: Boolean,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    binding.preferencesList.removeAllViews()
    val context = binding.root.context
    listOf(
        PrefSpec(context.getString(R.string.ui_account_settings), R.drawable.ic_lumora_settings, 0xFFFFFFFF.toInt()) { onNavigate(Screen.Settings.route) },
        PrefSpec(context.getString(R.string.ui_privacy_policy), R.drawable.ic_lumora_info, 0xFFFFFFFF.toInt()) {},
        PrefSpec(context.getString(R.string.ui_terms_of_service), R.drawable.ic_lumora_info, 0xFFFFFFFF.toInt()) {},
        PrefSpec(context.getString(R.string.ui_delete_account), R.drawable.ic_lumora_delete, 0xFFFF7A7A.toInt()) {
            confirmAccountAction(binding.root, "delete", isGuest, onSignOut, onDeleteAccount, onNavigate)
        },
        PrefSpec(
            if (isGuest) context.getString(R.string.ui_log_in) else context.getString(R.string.ui_sign_out),
            R.drawable.ic_lumora_logout,
            if (isGuest) 0xFFFFFFFF.toInt() else 0xFFFF7A7A.toInt()
        ) {
            confirmAccountAction(binding.root, if (isGuest) "login" else "signout", isGuest, onSignOut, onDeleteAccount, onNavigate)
        },
    ).forEach { spec ->
        val row = ProfileItemPrefBinding.inflate(LayoutInflater.from(context), binding.preferencesList, false)
        row.title.text = spec.title
        row.title.setTextColor(spec.color)
        row.iconGlyph.setImageResource(spec.iconRes)
        row.iconGlyph.setColorFilter(spec.color)
        row.root.setOnClickListener { spec.onClick() }
        binding.preferencesList.addView(row.root)
    }
}

private fun confirmAccountAction(
    anchor: View,
    action: String,
    isGuest: Boolean,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val context = anchor.context
    val isDelete = action == "delete"
    val isLogin = action == "login"
    AlertDialog.Builder(context)
        .setTitle(
            if (isDelete) context.getString(R.string.ui_delete_account_q)
            else if (isLogin) context.getString(R.string.ui_login_to_save_q)
            else context.getString(R.string.ui_sign_out_q)
        )
        .setMessage(
            if (isDelete) {
                if (isGuest) context.getString(R.string.ui_delete_guest_data_body) else context.getString(R.string.ui_delete_account_body)
            } else if (isLogin) {
                context.getString(R.string.ui_login_dialog_body)
            } else {
                context.getString(R.string.ui_sign_out_body)
            }
        )
        .setPositiveButton(
            if (isDelete) context.getString(R.string.ui_delete_permanently)
            else if (isLogin) context.getString(R.string.ui_log_in)
            else context.getString(R.string.ui_sign_out)
        ) { _, _ ->
            if (isDelete) onDeleteAccount() else if (isLogin) onNavigate(Screen.Auth.route) else onSignOut()
        }
        .setNegativeButton(R.string.ui_cancel, null)
        .show()
}

private fun bindMediaThumb(image: android.widget.ImageView, item: HistoryModel, fallbackRes: Int) {
    val path = item.mediaUrl.orEmpty()
    val file = File(path)
    when {
        path.isBlank() || !file.exists() -> image.setImageResource(fallbackRes)
        item.type.equals("VIDEO", ignoreCase = true) -> videoFrame(file)?.let { image.setImageBitmap(it) } ?: image.setImageResource(fallbackRes)
        else -> image.setImageURI(Uri.fromFile(file))
    }
    image.contentDescription = item.title
}

private fun gridParams(index: Int, view: View, heightDp: Int, gapDp: Int): GridLayout.LayoutParams =
    GridLayout.LayoutParams(
        GridLayout.spec(index / 2, 1),
        GridLayout.spec(index % 2, 1f)
    ).apply {
        width = 0
        height = dp(view, heightDp)
        setMargins(
            if (index % 2 == 0) 0 else dp(view, gapDp / 2),
            if (index < 2) 0 else dp(view, gapDp),
            if (index % 2 == 0) dp(view, gapDp / 2) else 0,
            0
        )
    }

private fun dp(view: View, value: Int): Int = (value * view.resources.displayMetrics.density).toInt()

private fun videoFrame(file: File): Bitmap? = runCatching {
    MediaMetadataRetriever().use { retriever ->
        retriever.setDataSource(file.absolutePath)
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }
}.getOrNull()

private data class PrefSpec(
    val title: String,
    val iconRes: Int,
    val color: Int,
    val onClick: () -> Unit,
)
