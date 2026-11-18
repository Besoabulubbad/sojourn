package com.abulubad.sojourn.feature.checkout

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [checkoutSnapshots]. */
class CheckoutScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(checkoutSnapshots)
}
