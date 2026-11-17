package com.abulubad.sojourn.feature.explore

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [exploreSnapshots]. */
class ExploreScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(exploreSnapshots)
}
