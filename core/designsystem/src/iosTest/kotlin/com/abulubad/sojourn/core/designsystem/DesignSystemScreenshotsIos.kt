package com.abulubad.sojourn.core.designsystem

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [designSystemSnapshots]. */
class DesignSystemScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(designSystemSnapshots)
}
