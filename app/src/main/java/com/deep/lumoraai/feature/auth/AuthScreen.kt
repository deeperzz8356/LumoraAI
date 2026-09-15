package com.deep.lumoraai.feature.auth

import android.view.LayoutInflater
import android.view.View
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.databinding.AuthScreenBinding
import com.deep.lumoraai.core.view.applySystemBarPadding

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String, Boolean) -> Unit,
    onGuestSignIn: () -> Unit,
    onEmailOptionClick: (Boolean) -> Unit,
    onBack: () -> Unit,
    allowGuestSignIn: Boolean = true,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context -> AuthScreenBinding.inflate(LayoutInflater.from(context)).apply { content.applySystemBarPadding() }.root },
        update = { root ->
            bindAuth(
                AuthScreenBinding.bind(root), uiState, allowGuestSignIn,
                onGoogleSignIn, onEmailSignIn, onGuestSignIn, onEmailOptionClick, onBack
            )
        },
        modifier = modifier.fillMaxSize()
    )
}

private fun bindAuth(
    binding: AuthScreenBinding,
    state: AuthUiState,
    allowGuest: Boolean,
    onGoogle: () -> Unit,
    onEmailSubmit: (String, String, Boolean) -> Unit,
    onGuest: () -> Unit,
    onEmailOption: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = binding.root.context
    val emailState = state as? AuthUiState.EmailForm
    val showEmail = emailState != null
    val loading = state is AuthUiState.Loading

    binding.mainActions.visibility = if (!showEmail && !loading) View.VISIBLE else View.GONE
    binding.emailForm.visibility = if (showEmail) View.VISIBLE else View.GONE
    binding.loading.visibility = if (loading) View.VISIBLE else View.GONE
    binding.errorText.visibility = if (state is AuthUiState.Error) View.VISIBLE else View.GONE
    binding.errorText.text = (state as? AuthUiState.Error)?.message.orEmpty()

    if (showEmail) {
        val signUp = emailState.isSignUp
        if (binding.passwordToggle.tag == null) {
            binding.passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off)
            binding.passwordToggle.contentDescription = context.getString(R.string.auth_show_password)
            binding.passwordToggle.tag = false
        }
        binding.passwordToggle.setOnClickListener {
            val isShowing = binding.passwordToggle.tag as? Boolean ?: false
            val shouldShow = !isShowing
            binding.passwordInput.transformationMethod = if (shouldShow) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            binding.passwordInput.setSelection(binding.passwordInput.text.length)
            binding.passwordToggle.setImageResource(if (shouldShow) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
            binding.passwordToggle.contentDescription = context.getString(
                if (shouldShow) R.string.auth_hide_password else R.string.auth_show_password
            )
            binding.passwordToggle.tag = shouldShow
        }
        binding.title.setText(if (signUp) R.string.auth_create_account else R.string.auth_welcome_back)
        binding.titleSecondary.visibility = View.GONE
        binding.subtitle.setText(if (signUp) R.string.auth_signup_description else R.string.auth_signin_description)
        binding.submitButton.setText(if (signUp) R.string.auth_create else R.string.auth_sign_in)
        binding.accountPrompt.setText(if (signUp) R.string.auth_have_account else R.string.auth_no_account)
        binding.accountAction.setText(if (signUp) R.string.auth_sign_in else R.string.auth_sign_up)
        binding.submitButton.setOnClickListener {
            onEmailSubmit(binding.emailInput.text.toString(), binding.passwordInput.text.toString(), signUp)
        }
        binding.accountSwitch.setOnClickListener { onEmailOption(!signUp) }
    } else {
        binding.title.setText(R.string.auth_unlock)
        binding.titleSecondary.visibility = View.VISIBLE
        binding.titleSecondary.setText(R.string.auth_ai_creation)
        binding.subtitle.setText(R.string.auth_tagline)
        binding.accountPrompt.setText(R.string.auth_no_account)
        binding.accountAction.setText(R.string.auth_sign_up)
        binding.accountSwitch.setOnClickListener { onEmailOption(true) }
    }

    binding.googleButton.setOnClickListener { onGoogle() }
    binding.emailOptionButton.setOnClickListener { onEmailOption(false) }
    binding.backButton.setOnClickListener { onBack() }
    binding.guestButton.visibility = if (!showEmail) View.VISIBLE else View.GONE
    binding.guestButton.isEnabled = allowGuest
    binding.guestButton.setText(if (allowGuest) R.string.auth_continue_guest else R.string.auth_trial_finished)
    binding.guestButton.setOnClickListener { if (allowGuest) onGuest() else onEmailOption(false) }
}
