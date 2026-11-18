package com.abulubad.sojourn.feature.map

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [mapSnapshots]. */
class MapScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(mapSnapshots)
}
