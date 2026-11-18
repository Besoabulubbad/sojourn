package com.abulubad.sojourn.feature.details

import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.testing.SnapshotCase
import com.abulubad.sojourn.core.testing.SojournScreenshotSuite
import org.robolectric.ParameterizedRobolectricTestRunner

/** Android runner for [detailsSnapshots]. */
class DetailsScreenshots(case: SnapshotCase, variant: SojournVariant) : SojournScreenshotSuite(case, variant) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}[{1}]")
        fun parameters(): List<Array<Any>> = screenshotParameters(detailsSnapshots)
    }
}
