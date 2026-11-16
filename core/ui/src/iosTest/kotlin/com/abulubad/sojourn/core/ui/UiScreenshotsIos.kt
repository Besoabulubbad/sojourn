package com.abulubad.sojourn.core.ui

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [uiSnapshots]. */
class UiScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(uiSnapshots)
}
