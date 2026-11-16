package com.abulubad.sojourn.core.designsystem.components.snackbar

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** SojournSnackbarHost shows what a screen asks for and reports the action (the timing is in commonTest). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournSnackbarHostBehaviourTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `a shown snackbar appears and its action is reported`() {
        var result: SojournSnackbarResult? = null
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            SojournPreview(SojournVariants.EnLight) {
                val state = remember { SojournSnackbarHostState() }
                SojournSnackbarHost(state)
                LaunchedEffect(Unit) { result = state.show(MESSAGE, actionLabel = ACTION) }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithText(MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(ACTION).performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        assertEquals(SojournSnackbarResult.ActionPerformed, result)
    }

    private companion object {
        const val MESSAGE = "Removed from Saved"
        const val ACTION = "Undo"
    }
}
