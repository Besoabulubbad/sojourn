package com.abulubad.sojourn.feature.results

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [resultsSnapshots]. */
class ResultsScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(resultsSnapshots)
}
