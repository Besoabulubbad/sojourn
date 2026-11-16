package com.abulubad.sojourn.core.testing

import androidx.compose.runtime.Composable

/**
 * One visual state to screenshot, written once in a module's `commonTest` and rendered by both platform runners
 * (ADR 0010): `SojournScreenshotSuite` on Android (Robolectric) and `verifySnapshots` on iOS (simulator).
 *
 * Images are `<name>_<variant>.png` under `<module>/screenshots/<androidHostTest|iosSimulatorArm64>/`. Every case
 * runs in the four shipped variants on both platforms; [fullMatrix] adds 200 % font and 320 dp width on Android for
 * a component's or screen's default state.
 */
class SnapshotCase(
    val name: String,
    val fullMatrix: Boolean = false,
    val content: @Composable () -> Unit,
) {
    override fun toString(): String = name
}
