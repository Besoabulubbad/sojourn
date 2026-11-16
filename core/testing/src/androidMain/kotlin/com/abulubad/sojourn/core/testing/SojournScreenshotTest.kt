package com.abulubad.sojourn.core.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Base class for screenshot tests (ADR 0010). Parameterized over the variants: every test method runs once per
 * [SojournVariant] and captures one image, `<name>_<variant.id>.png`, under the module's
 * `src/androidHostTest/screenshots`.
 *
 * Content is set through a compose test rule and captured from its node after the rule has waited for idle, the
 * pattern Roborazzi documents for Compose.
 *
 * Robolectric renders on the **API 36** runtime with native graphics and a fixed device so images are
 * deterministic. Robolectric 4.18's API 37 runtime intermittently dropped Compose frames before capture (blank
 * #FAFAFA images, reproduced with a plain coloured box); unit tests are unaffected. Revisit on the next
 * Robolectric release (docs/guides/testing.md).
 *
 * Subclasses declare `class XxxScreenshotTest(variant: SojournVariant) : SojournScreenshotTest(variant)`.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SojournScreenshotTest.SDK], qualifiers = SojournScreenshotTest.DEVICE)
abstract class SojournScreenshotTest(private val variant: SojournVariant) {

    @get:Rule
    val composeRule = createComposeRule()

    /** Renders [content] through [SojournPreview] in this test's variant and records or verifies its image. */
    protected fun snapshot(name: String, content: @Composable () -> Unit) {
        composeRule.setContent {
            Box(Modifier.testTag(SNAPSHOT_TAG)) {
                SojournPreview(variant) { content() }
            }
        }
        composeRule.onNodeWithTag(SNAPSHOT_TAG).captureRoboImage(filePath = "${name}_${variant.id}.png")
    }

    companion object {
        const val SDK = 36
        const val DEVICE = "w412dp-h915dp-xxhdpi"
        private const val SNAPSHOT_TAG = "sojourn-snapshot"

        /** The four shipped variants (EN/AR × light/dark). The 16-cell matrix arrives with components (Phase 3). */
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun variants(): List<Array<Any>> = SojournVariants.Four.map { arrayOf(it) }
    }
}
