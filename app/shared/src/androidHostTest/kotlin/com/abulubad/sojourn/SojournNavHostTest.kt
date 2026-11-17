package com.abulubad.sojourn

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The navigation host composes with the tabs that exist. Decorating a stack resolves every key in it, so a tab
 * whose root has no entry crashes at start-up ("Unknown screen"); this catches it before a device does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SojournNavHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `the host starts on the Explore root`() {
        composeRule.setContent { SojournPreview(SojournVariants.EnLight) { SojournNavHost() } }
        composeRule.onNodeWithText("Sojourn").assertExists()
    }
}
