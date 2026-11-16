package com.abulubad.sojourn.core.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The Android screenshot runner (ADR 0010). A module declares one subclass in `androidHostTest`:
 *
 * ```
 * class DesignSystemScreenshots(case: SnapshotCase, variant: SojournVariant) : SojournScreenshotSuite(case, variant) {
 *     companion object {
 *         @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}[{1}]")
 *         fun parameters() = screenshotParameters(designSystemSnapshots)
 *     }
 * }
 * ```
 *
 * Each case × variant is its own test, rendered through `SojournPreview` with a compose test rule and captured from
 * its node once the rule is idle. Robolectric renders on the **API 36** runtime with native graphics and a fixed
 * `w412dp-h915dp-xxhdpi` device: Robolectric 4.18's API 37 runtime intermittently dropped Compose frames before
 * capture (blank `#FAFAFA` images, reproduced with a plain coloured box).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SojournScreenshotSuite.SDK], qualifiers = SojournScreenshotSuite.DEVICE)
abstract class SojournScreenshotSuite(private val case: SnapshotCase, private val variant: SojournVariant) {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun screenshot() {
        composeRule.setContent {
            Box(Modifier.testTag(SNAPSHOT_TAG)) {
                SojournPreview(variant) { case.content() }
            }
        }
        composeRule.onNodeWithTag(SNAPSHOT_TAG).captureRoboImage(filePath = "${case.name}_${variant.id}.png")
    }

    companion object {
        const val SDK = 36
        const val DEVICE = "w412dp-h915dp-xxhdpi"
        private const val SNAPSHOT_TAG = "sojourn-snapshot"

        /** Every case in the four shipped variants; cases marked `fullMatrix` in all 16 cells. */
        fun screenshotParameters(cases: List<SnapshotCase>): List<Array<Any>> = cases.flatMap { case ->
            val variants = if (case.fullMatrix) SojournVariants.Matrix else SojournVariants.Four
            variants.map { arrayOf<Any>(case, it) }
        }
    }
}
