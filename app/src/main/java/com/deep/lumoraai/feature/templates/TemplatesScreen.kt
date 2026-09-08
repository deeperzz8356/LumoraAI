package com.deep.lumoraai.feature.templates

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.LumoraTopBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.promoVideoRoute
import com.deep.lumoraai.core.navigation.textToImageRoute
import com.deep.lumoraai.core.navigation.textToVideoRoute
import com.deep.lumoraai.feature.templates.components.FeatureCard
import com.deep.lumoraai.feature.templates.model.TemplateAction
import com.deep.lumoraai.feature.templates.model.TemplateCategory
import com.deep.lumoraai.feature.templates.model.TemplateListItem

private val TemplateBackground = Color(0xFF081020)

private val TemplatePanel = Color(0xFF111A2D)

private val TemplateSelected = Color(0xFF57647A)

private val Muted = Color(0xFF9BA6BA)

@Composable
fun TemplatesScreen(
    uiState: TemplatesUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    unreadCount: Int = 0,
    modifier: Modifier = Modifier,
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TemplateBackground,
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "templates",
                onSelected = onNavigate,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(TemplateBackground).padding(padding)) {
            when (uiState) {
                is TemplatesUiState.Loading -> AppLoadingScreen()

                is TemplatesUiState.Error -> AppErrorScreen(message = uiState.message)

                is TemplatesUiState.Empty ->
                    AppEmptyScreen(
                        title = stringResource(com.deep.lumoraai.R.string.ui_no_templates),
                        body =
                            stringResource(com.deep.lumoraai.R.string.ui_templates_will_appear_here),
                    )

                is TemplatesUiState.Success -> {

                    TemplatesContent(
                        uiState = uiState,
                        onNavigate = onNavigate,
                        unreadCount = unreadCount,
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatesContent(
    uiState: TemplatesUiState.Success,
    onNavigate: (String) -> Unit,
    unreadCount: Int,
) {

    var selectedCategory by remember { mutableStateOf(TemplateCategory.IMAGE) }

    val clipboard = LocalClipboardManager.current

    val context = LocalContext.current

    /*
     * Select list based on segment.
     */
    val templates =
        when (selectedCategory) {
            TemplateCategory.IMAGE -> uiState.imageTemplates

            TemplateCategory.VIDEO -> uiState.videoTemplates

            TemplateCategory.PROMO_VIDEO -> uiState.promoVideoTemplates

            TemplateCategory.LOGO_CREATION -> uiState.logoCreationTemplates

            TemplateCategory.AVATAR -> uiState.avatarTemplates
        }

    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        /*
         * Top bar
         */
        LumoraTopBar(
            credits = uiState.credits,
            title = stringResource(com.deep.lumoraai.R.string.ui_templates),
            onProfileClick = { onNavigate(Screen.Profile.route) },
            onCreditsClick = { onNavigate(Screen.Credits.route) },
            onNotificationsClick = { onNavigate(Screen.Notifications.route) },
            hasUnreadNotifications = unreadCount > 0,
        )

        /*
         * Category segments
         */
        TemplateCategoryTabs(
            selectedCategory = selectedCategory,
            onSelected = { selectedCategory = it },
        )

        /*
         * Cards
         */
        if (templates.isEmpty()) {

            EmptyCategory(category = selectedCategory)
        } else {

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                templates.forEach { item ->
                    FeatureCard(
                        item = item,
                        onCopy = {
                            clipboard.setText(AnnotatedString(item.prompt))

                            Toast.makeText(context, "Prompt copied", Toast.LENGTH_SHORT).show()
                        },
                        onClick = { navigateTemplate(item = item, onNavigate = onNavigate) },
                    )
                }
            }
        }
    }
}

/*
 * Five segment controls.
 */
@Composable
private fun TemplateCategoryTabs(
    selectedCategory: TemplateCategory,
    onSelected: (TemplateCategory) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryTab(
            label = "Image",
            icon = Icons.Default.Image,
            selected = selectedCategory == TemplateCategory.IMAGE,
        ) {
            onSelected(TemplateCategory.IMAGE)
        }

        CategoryTab(
            label = "Video",
            icon = Icons.Default.VideoLibrary,
            selected = selectedCategory == TemplateCategory.VIDEO,
        ) {
            onSelected(TemplateCategory.VIDEO)
        }

        CategoryTab(
            label = "Promo Video",
            icon = Icons.Default.VideoLibrary,
            selected = selectedCategory == TemplateCategory.PROMO_VIDEO,
        ) {
            onSelected(TemplateCategory.PROMO_VIDEO)
        }

        CategoryTab(
            label = "Logo Creation",
            icon = Icons.Default.Image,
            selected = selectedCategory == TemplateCategory.LOGO_CREATION,
        ) {
            onSelected(TemplateCategory.LOGO_CREATION)
        }

        CategoryTab(
            label = "Create Your Avatar",
            icon = Icons.Default.Person,
            selected = selectedCategory == TemplateCategory.AVATAR,
        ) {
            onSelected(TemplateCategory.AVATAR)
        }
    }
}

@Composable
private fun CategoryTab(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) TemplateSelected else TemplatePanel,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = if (selected) 0.95f else 0.65f),
                modifier = Modifier.size(15.dp),
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                color = Color.White.copy(alpha = if (selected) 1f else 0.78f),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}

/*
 * Navigation for templates whose routes
 * already exist.
 *
 * Voice Dubbing and Avatar are deliberately
 * left for the actual routes in your project.
 */
private fun navigateTemplate(item: TemplateListItem, onNavigate: (String) -> Unit) {
    when (item.action) {
        TemplateAction.TEXT_TO_IMAGE -> {
            onNavigate(textToImageRoute(item.prompt))
        }

        TemplateAction.TEXT_TO_VIDEO -> {
            onNavigate(textToVideoRoute(item.prompt))
        }

        TemplateAction.PROMO_VIDEO -> {
            onNavigate(promoVideoRoute(item.prompt))
        }

        TemplateAction.LOGO_CREATION -> {
            /*
             * Logo creation currently uses
             * the existing Text-to-Image generation route.
             */
            onNavigate(textToImageRoute(item.prompt))
        }

        TemplateAction.CREATE_AVATAR -> {
            /*
             * Avatar route will be connected
             * when an actual avatar route exists.
             */
            onNavigate(textToImageRoute(item.prompt))
        }
    }
}

@Composable
private fun EmptyCategory(category: TemplateCategory) {

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
                when (category) {
                    TemplateCategory.IMAGE -> "No image templates yet"

                    TemplateCategory.VIDEO -> "No video templates yet"

                    TemplateCategory.PROMO_VIDEO -> "No promo video templates yet"

                    TemplateCategory.LOGO_CREATION -> "No logo templates yet"

                    TemplateCategory.AVATAR -> "No avatar templates yet"
                },
            color = Muted,
            fontSize = 13.sp,
        )
    }
}
