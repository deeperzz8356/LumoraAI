package com.deep.lumoraai

import android.view.View
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.deep.lumoraai.feature.bgstudio.BgStudioScreen
import com.deep.lumoraai.feature.bgstudio.BgStudioUiState
import com.deep.lumoraai.feature.compress.CompressScreen
import com.deep.lumoraai.feature.compress.CompressUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class UploadLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun backgroundUploadFillsWorkspaceWidthAndSeventyPercentHeight() {
        compose.setContent {
            BgStudioScreen(BgStudioUiState(), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
        }
        compose.waitForIdle()
        onView(withId(R.id.singleUploadPanel)).check { panel, _ ->
            val content = panel.parent as View
            val viewport = content.parent as View
            val minimum = (220 * panel.resources.displayMetrics.density).toInt()
            assertEquals(content.width - content.paddingLeft - content.paddingRight, panel.width)
            assertEquals((viewport.height * 0.7f).toInt().coerceAtLeast(minimum), panel.height)
        }
        onView(withId(R.id.generateButton)).check(matches(isDisplayed()))
    }

    @Test fun compressUploadUsesHalfViewport() {
        compose.setContent {
            CompressScreen(CompressUiState(), {}, {}, {}, {}, {}, {})
        }
        compose.waitForIdle()
        onView(withId(R.id.uploadPanel)).check { panel, _ ->
            val viewport = panel.rootView.findViewById<View>(R.id.content).parent as View
            val minimum = (180 * panel.resources.displayMetrics.density).toInt()
            assertEquals((viewport.height * 0.5f).toInt().coerceAtLeast(minimum), panel.height)
        }
    }
}
