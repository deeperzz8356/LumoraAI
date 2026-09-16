package com.deep.lumoraai

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.action.ViewActions.click
import com.deep.lumoraai.feature.imagetoimage.ImageToImageSource
import android.view.ViewGroup
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.deep.lumoraai.feature.imagetoimage.ImageToImageScreen
import com.deep.lumoraai.feature.imagetoimage.ImageToImageUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageToImageUploadTest {
    @get:Rule val compose = createComposeRule()

    private val launches = mutableListOf<Intent>()
    private val registry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            launches += contract.createIntent(
                InstrumentationRegistry.getInstrumentation().targetContext, input
            )
        }
    }

    @Test fun uploadIconOpensImagePicker() = assertPickerOpens(0)
    @Test fun uploadLabelOpensImagePicker() = assertPickerOpens(1)
    @Test fun uploadBackgroundOpensImagePicker() = assertPickerOpens(null)
    @Test fun templateUploadIconOpensImagePicker() = assertPickerOpens(0, "Create a cinematic portrait")
    @Test fun templateUploadLabelOpensImagePicker() = assertPickerOpens(1, "Create a cinematic portrait")

    private fun setScreen(state: () -> ImageToImageUiState) {
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = registry
        }
        compose.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                ImageToImageScreen(
                    uiState = state(),
                    onBack = {}, onNavigate = {}, onImagesSelected = {},
                    onSourceRemoved = {}, onPromptChanged = {}, onNegativePromptChanged = {},
                    onAspectRatioChanged = {}, onImprovePrompt = {}, onStyleSelected = {},
                    onSimilarityChanged = {}, onGenerationsChanged = {}, onGenerate = {},
                    onEditResult = {}, onDismissError = {},
                )
            }
        }
    }

    @Test fun selectedSourcesUseOneAddActionAndRespectLimitAndBusyState() {
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val sources = (1..3).map { ImageToImageSource("$it", Uri.parse("content://test/$it"), bitmap, "") }
        val state = mutableStateOf(ImageToImageUiState(sourceImages = sources.take(1)))
        setScreen { state.value }
        onView(withId(R.id.singleUploadPanel)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.sourceRow)).check { view, _ -> assertEquals(2, (view as ViewGroup).childCount) }
        compose.runOnIdle { state.value = state.value.copy(sourceImages = sources) }
        onView(withId(R.id.sourceRow)).check { view, _ -> assertEquals(3, (view as ViewGroup).childCount) }
        compose.runOnIdle { state.value = state.value.copy(sourceImages = emptyList(), isLoadingSources = true) }
        onView(withId(R.id.singleUploadPanel)).perform(click())
        compose.runOnIdle { assertEquals(0, launches.size) }
        onView(withId(R.id.sourceRow)).check { view, _ -> assertEquals(0, (view as ViewGroup).childCount) }
    }

    private fun assertPickerOpens(childIndex: Int?, prompt: String = "") {
        setScreen { ImageToImageUiState(prompt = prompt) }
        compose.waitForIdle()
        onView(withId(R.id.singleUploadPanel)).perform(GeneralClickAction(
            Tap.SINGLE,
            { row ->
                val tile = row as ViewGroup
                val target = if (childIndex == null) tile else
                    tile.findViewById<ViewGroup>(R.id.uploadEmpty).getChildAt(childIndex)
                val location = IntArray(2)
                target.getLocationOnScreen(location)
                floatArrayOf(
                    location[0] + if (childIndex == null) 4f else target.width / 2f,
                    location[1] + if (childIndex == null) 4f else target.height / 2f,
                )
            },
            Press.FINGER,
        ))
        compose.runOnIdle {
            assertEquals("Tapping upload must launch the photo picker", 1, launches.size)
            assertEquals(Intent.ACTION_GET_CONTENT, launches.single().action)
            assertEquals("image/*", launches.single().type)
            assertEquals(true, launches.single().getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
        }
    }
}
