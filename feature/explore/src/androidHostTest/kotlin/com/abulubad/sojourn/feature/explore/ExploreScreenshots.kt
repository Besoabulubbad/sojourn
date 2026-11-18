package com.abulubad.sojourn.feature.explore

import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.testing.SnapshotCase
import com.abulubad.sojourn.core.testing.SojournScreenshotSuite
import org.robolectric.ParameterizedRobolectricTestRunner

/** Android runner for [exploreSnapshots]. */
class ExploreScreenshots(case: SnapshotCase, variant: SojournVariant) : SojournScreenshotSuite(case, variant) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}[{1}]")
        fun parameters(): List<Array<Any>> = screenshotParameters(exploreSnapshots)
    }
}
