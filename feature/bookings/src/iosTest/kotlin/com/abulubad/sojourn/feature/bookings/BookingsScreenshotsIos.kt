package com.abulubad.sojourn.feature.bookings

import com.abulubad.sojourn.core.testing.verifySnapshots
import kotlin.test.Test

/** iOS runner for [bookingsSnapshots]. */
class BookingsScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(bookingsSnapshots)
}
