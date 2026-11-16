package com.abulubad.sojourn.core.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage

/** The iOS virtual device: the same 412 × 915 dp at 3× as the Android screenshots, so images line up. */
private const val DEVICE_DENSITY = 3f
private const val DEVICE_WIDTH_DP = 412f
private const val DEVICE_HEIGHT_DP = 915f
private const val SNAPSHOT_TAG = "sojourn-snapshot"

/**
 * The iOS screenshot runner (ADR 0010). A module declares one test in `iosTest`:
 *
 * ```
 * class DesignSystemScreenshotsIos {
 *     @Test fun screenshots() = verifySnapshots(designSystemSnapshots)
 * }
 * ```
 *
 * Renders every case in the four shipped variants with Compose's Skiko test runtime on the simulator and records or
 * verifies `<name>_<variant>.png` under `screenshots/iosSimulatorArm64/` (Roborazzi's experimental iOS support; iOS
 * needs an explicit file name per capture). Every case runs even if an earlier one fails, and the failure lists them
 * all.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
fun verifySnapshots(cases: List<SnapshotCase>) {
    val failures = mutableListOf<String>()
    cases.forEach { case ->
        SojournVariants.Four.forEach { variant ->
            val fileName = "${case.name}_${variant.id}.png"
            try {
                runSkikoComposeUiTest(
                    size = Size(DEVICE_WIDTH_DP * DEVICE_DENSITY, DEVICE_HEIGHT_DP * DEVICE_DENSITY),
                    density = Density(DEVICE_DENSITY),
                ) {
                    setContent {
                        Box(Modifier.testTag(SNAPSHOT_TAG)) {
                            SojournPreview(variant) { case.content() }
                        }
                    }
                    onNodeWithTag(SNAPSHOT_TAG).captureRoboImage(this, filePath = fileName)
                }
            } catch (e: AssertionError) {
                failures += "$fileName: ${e.message}"
            }
        }
    }
    check(failures.isEmpty()) { "${failures.size} iOS screenshot(s) differ:\n" + failures.joinToString("\n") }
}
