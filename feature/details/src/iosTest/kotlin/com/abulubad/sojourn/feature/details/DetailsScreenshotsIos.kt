package com.abulubad.sojourn.feature.details

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [detailsSnapshots]. */
class DetailsScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(detailsSnapshots)
}
