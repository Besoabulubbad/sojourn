package com.abulubad.sojourn.feature.filters

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [filtersSnapshots]. */
class FiltersScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(filtersSnapshots)
}
