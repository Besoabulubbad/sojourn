package com.abulubad.sojourn.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A modal sheet hides the page beneath from screen readers while it is open, and the page comes back once it closes
 * (ADR 0015: our sheet draws in the page's own layer, so it has to do what a dialog window would).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournScaffoldModalTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `the page leaves the accessibility tree while a modal sheet is open`() {
        composeRule.setContent {
            SojournPreview(SojournVariants.EnLight) {
                var open by remember { mutableStateOf(true) }
                Box(Modifier.fillMaxSize()) {
                    SojournScaffold { BasicText(PAGE) }
                    if (open) {
                        SojournSheet(rememberSojournSheetState(), title = SHEET, onDismiss = { open = false }) {}
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SHEET).assertIsDisplayed()
        composeRule.onNodeWithText(PAGE).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(CLOSE).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PAGE).assertIsDisplayed()
    }

    @Test
    fun `a non-modal sheet leaves the page readable`() {
        composeRule.setContent {
            SojournPreview(SojournVariants.EnLight) {
                Box(Modifier.fillMaxSize()) {
                    SojournScaffold { BasicText(PAGE) }
                    SojournSheet(rememberSojournSheetState(), title = SHEET, onDismiss = {}, modal = false) {}
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PAGE).assertIsDisplayed()
    }

    private companion object {
        const val PAGE = "The page beneath"
        const val SHEET = "A sheet"
        const val CLOSE = "Close"
    }
}
