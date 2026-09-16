package com.deep.lumoraai

import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.deep.lumoraai.feature.templates.TemplateSectionScreen
import com.deep.lumoraai.feature.templates.TemplatesUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TemplateSectionStateTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loadingErrorRetryAndEmptyHaveDistinctStates() {
        val state = mutableStateOf<TemplatesUiState>(TemplatesUiState.Loading)
        var retries = 0
        compose.setContent {
            TemplateSectionScreen(state.value, "image", "featured", {}, {}, onRetry = { retries++ })
        }
        onView(withId(R.id.loading)).check(matches(isDisplayed()))
        onView(withId(R.id.messageState)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        compose.runOnIdle { state.value = TemplatesUiState.Error("Unable to load templates") }
        onView(withId(R.id.loading)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.messageState)).check(matches(withText("Unable to load templates")))
        onView(withId(R.id.retryButton)).perform(click())
        compose.runOnIdle { assertEquals(1, retries); state.value = TemplatesUiState.Empty }
        onView(withId(R.id.retryButton)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.messageState)).check(matches(isDisplayed()))
    }
}
