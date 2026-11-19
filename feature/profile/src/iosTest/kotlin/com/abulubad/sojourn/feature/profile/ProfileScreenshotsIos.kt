package com.abulubad.sojourn.feature.profile

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [profileSnapshots]. */
class ProfileScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(profileSnapshots)
}
