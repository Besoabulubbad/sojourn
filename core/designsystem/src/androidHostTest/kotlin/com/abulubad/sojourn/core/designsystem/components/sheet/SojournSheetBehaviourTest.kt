package com.abulubad.sojourn.core.designsystem.components.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** A modal SojournSheet enters at its initial stop, moves between stops, and closes through its close button. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournSheetBehaviourTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `the close button animates the sheet out and then dismisses it`() {
        var dismissed = 0
        lateinit var state: SojournSheetState
        composeRule.setContent {
            SojournPreview(SojournVariants.EnLight) {
                state = rememberSojournSheetState(SojournSheetDetent.Half)
                Box(Modifier.height(LAYER_HEIGHT)) {
                    SojournSheet(state, title = TITLE, onDismiss = { dismissed++ }) {}
                }
            }
        }
        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
        assertEquals(SojournSheetDetent.Half, state.current)

        composeRule.onNodeWithContentDescription(CLOSE).performClick()
        composeRule.waitForIdle()
        assertEquals(null, state.current)
        assertEquals(1, dismissed)
    }

    @Test
    fun `the handle expands the sheet for a screen reader`() {
        lateinit var state: SojournSheetState
        composeRule.setContent {
            SojournPreview(SojournVariants.EnLight) {
                state = rememberSojournSheetState(
                    SojournSheetDetent.Half,
                    setOf(SojournSheetDetent.Half, SojournSheetDetent.Full),
                )
                Box(Modifier.height(LAYER_HEIGHT)) {
                    SojournSheet(state, title = TITLE, onDismiss = {}) {}
                }
            }
        }
        composeRule.onNodeWithContentDescription(HANDLE).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        assertEquals(SojournSheetDetent.Full, state.current)
    }

    private companion object {
        const val TITLE = "Filters"
        const val CLOSE = "Close"
        const val HANDLE = "Drag handle"
        val LAYER_HEIGHT = 600.dp
    }
}
