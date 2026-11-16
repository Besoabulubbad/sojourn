package com.abulubad.sojourn.core.ui

import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.testing.SnapshotCase
import com.abulubad.sojourn.core.testing.SojournScreenshotSuite
import org.robolectric.ParameterizedRobolectricTestRunner

/** Android runner for [uiSnapshots]. */
class UiScreenshots(case: SnapshotCase, variant: SojournVariant) : SojournScreenshotSuite(case, variant) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}[{1}]")
        fun parameters(): List<Array<Any>> = screenshotParameters(uiSnapshots)
    }
}
