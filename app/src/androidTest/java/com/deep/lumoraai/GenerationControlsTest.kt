package com.deep.lumoraai

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.view.ViewCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deep.lumoraai.feature.imagetoimage.ImageToImageScreen
import com.deep.lumoraai.feature.imagetoimage.ImageToImageUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenerationControlsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun ratioTargetsAreLargeAndSummaryTogglesPanel() {
        compose.setContent {
            ImageToImageScreen(
                uiState = ImageToImageUiState(),
                onBack = {}, onNavigate = {}, onImagesSelected = {},
                onSourceRemoved = {}, onPromptChanged = {}, onNegativePromptChanged = {},
                onAspectRatioChanged = {}, onImprovePrompt = {}, onStyleSelected = {},
                onSimilarityChanged = {}, onGenerationsChanged = {}, onGenerate = {},
                onEditResult = {}, onDismissError = {},
            )
        }
        onView(withId(R.id.summaryRow)).perform(click())
        onView(withId(R.id.selectorPanel)).check { view, error ->
            if (error != null) throw error
            assertEquals(View.VISIBLE, view.visibility)
        }
        onView(withId(R.id.ratioRow)).check { view, error ->
            if (error != null) throw error
            val row = view as ViewGroup
            assertTrue(row.childCount > 0)
            for (index in 0 until row.childCount) {
                val target = row.getChildAt(index)
                assertTrue(target.height >= 56 * target.resources.displayMetrics.density - 1)
                assertTrue(target.width >= 112 * target.resources.displayMetrics.density - 1)
                assertTrue(target.isClickable)
            }
        }
        onView(withId(R.id.summaryRow)).check { view, error ->
            if (error != null) throw error
            assertEquals(view.context.getString(R.string.generation_controls_expanded), ViewCompat.getStateDescription(view))
        }.perform(click())
        onView(withId(R.id.selectorPanel)).check { view, error ->
            if (error != null) throw error
            assertEquals(View.GONE, view.visibility)
        }
    }
}
