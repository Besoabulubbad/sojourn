package com.abulubad.sojourn.feature.search

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [searchSnapshots]. */
class SearchScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(searchSnapshots)
}
