package com.deep.lumoraai

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import com.deep.lumoraai.feature.result.ReportResultDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReportResultDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun requiresReasonAndRetainsDraftAcrossRestoration() {
        var submitted: Pair<String, String>? = null
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            MaterialTheme {
                ReportResultDialog({}, { reason, details ->
                    submitted = reason to details
                    Result.success(Unit)
                })
            }
        }
        compose.onNodeWithText("Submit report").assertIsNotEnabled()
        compose.onNodeWithText("Incorrect result").performClick()
        compose.onNodeWithText("Details (optional)").performTextInput("Missing subject")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Incorrect result").assertIsSelected()
        compose.onNodeWithText("Submit report").performClick()
        compose.runOnIdle { assertEquals("Incorrect result" to "Missing subject", submitted) }
    }

    @Test fun failureKeepsFormAvailableForRetry() {
        compose.setContent {
            MaterialTheme { ReportResultDialog({}, { _, _ -> Result.failure(IllegalStateException()) }) }
        }
        compose.onNodeWithText("Poor quality").performClick()
        compose.onNodeWithText("Submit report").performClick()
        compose.onNodeWithText("Could not submit report. Please try again.").assertExists()
        compose.onNodeWithText("Submit report").assertIsEnabled()
    }
}
