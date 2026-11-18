package com.abulubad.sojourn.feature.saved

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [savedSnapshots]. */
class SavedScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(savedSnapshots)
}
