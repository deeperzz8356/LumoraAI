package com.deep.lumoraai.feature.templates

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.LocalCreditBalance
import com.deep.lumoraai.data.repository.AppPreferencesRepository
import com.deep.lumoraai.data.repository.GenerationRepository
import com.deep.lumoraai.feature.templates.model.TemplateAction
import com.deep.lumoraai.feature.templates.model.TemplateListItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.json.JSONObject

class TemplatesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val generationRepository = GenerationRepository()

    private val appPreferences =
        AppPreferencesRepository.getInstance(application)

    var uiState: TemplatesUiState by mutableStateOf(
        loadTemplates()
    )
        private set

    init {
        loadCredits()
    }

    private fun loadTemplates(): TemplatesUiState.Success {

        return try {

            val jsonText =
                getApplication<Application>()
                    .assets
                    .open("templates/templates.json")
                    .bufferedReader()
                    .use { it.readText() }

            val root =
                JSONObject(jsonText)

            val categories =
                root.getJSONArray("categories")

            fun getTemplates(
                categoryId: String
            ): List<TemplateListItem> {

                for (i in 0 until categories.length()) {

                    val category =
                        categories.getJSONObject(i)

                    if (category.getString("id") == categoryId) {

                        val templates =
                            category.getJSONArray("templates")

                        val result =
                            mutableListOf<TemplateListItem>()

                        for (j in 0 until templates.length()) {

                            val template =
                                templates.getJSONObject(j)

                            val action =
                                when (
                                    template.getString("action")
                                ) {

                                    "TEXT_TO_IMAGE" ->
                                        TemplateAction.TEXT_TO_IMAGE

                                    "TEXT_TO_VIDEO" ->
                                        TemplateAction.TEXT_TO_VIDEO

                                    "PROMO_VIDEO" ->
                                        TemplateAction.PROMO_VIDEO

                                    "LOGO_CREATION" ->
                                        TemplateAction.LOGO_CREATION

                                    "CREATE_AVATAR" ->
                                        TemplateAction.CREATE_AVATAR

                                    else ->
                                        continue
                                }

                            result.add(
                                TemplateListItem(
                                    id = template.getString("id"),
                                    title = template.getString("title"),
                                    subtitle = template.getString("subtitle"),
                                    prompt = template.getString("prompt"),
                                    assetFileName =
                                        template.getString(
                                            "assetFileName"
                                        ),
                                    action = action
                                )
                            )
                        }

                        return result
                    }
                }

                return emptyList()
            }

            TemplatesUiState.Success(
                imageTemplates =
                    getTemplates("image"),

                videoTemplates =
                    getTemplates("video"),

                promoVideoTemplates =
                    getTemplates("promo_video"),

                logoCreationTemplates =
                    getTemplates("logo_creation"),

                avatarTemplates =
                    getTemplates("avatar")
            )

        } catch (e: Exception) {

            TemplatesUiState.Success(
                imageTemplates = emptyList(),
                videoTemplates = emptyList(),
                promoVideoTemplates = emptyList(),
                logoCreationTemplates = emptyList(),
                avatarTemplates = emptyList()
            )
        }
    }

    private fun loadCredits() {

        val user =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?: return

        viewModelScope.launch {

            val credits =
                if (
                    appPreferences
                        .isDeveloperModeEnabled()
                ) {

                    GenerationGate
                        .DEVELOPER_MODE_CREDITS_DISPLAY

                } else {

                    LocalCreditBalance.maxWith(
                        getApplication(),
                        generationRepository
                            .getCredits()
                            .getOrNull()
                    )
                }

            val current =
                uiState

            if (
                current is TemplatesUiState.Success
            ) {

                uiState =
                    current.copy(
                        credits = credits
                    )
            }
        }
    }
}