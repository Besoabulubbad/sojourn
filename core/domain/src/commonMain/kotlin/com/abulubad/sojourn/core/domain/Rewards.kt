package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** One completed stay and the points it earned. */
data class EarnedPoints(val item: BookingItem, val points: Int)

/** The guest's rewards (ScreenProfile card): the balance, and the stays that earned it, latest first. */
data class Rewards(val points: Int, val earned: List<EarnedPoints>) {
    companion object {
        val None = Rewards(0, emptyList())
    }
}

/**
 * The demo's rewards rule, the same everywhere so the balance never depends on the screen: a stay earns points once
 * it is completed (never while upcoming or after a cancellation), one per whole pound or euro paid, or per
 * [DZD_PER_POINT] dinars, so a night earns about the same in every city.
 */
object RewardsRules {

    const val DZD_PER_POINT = 100

    /** The points [booking] earned; 0 until it is completed. */
    fun pointsFor(booking: Booking): Int {
        if (booking.status != BookingStatus.Completed) return 0
        val perPoint = if (booking.total.currency == Currency.DZD) DZD_PER_POINT else 1
        return (booking.total.major / perPoint).toInt()
    }
}

/** The guest's points from their completed stays on this device, following the calendar like [ListBookings]. */
class ObserveRewards(private val listBookings: ListBookings) {
    operator fun invoke(): Flow<Rewards> = listBookings().map { items ->
        val earned = items
            .map { EarnedPoints(it, RewardsRules.pointsFor(it.booking)) }
            .filter { it.points > 0 }
            .sortedByDescending { it.item.booking.dates.end }
        Rewards(earned.sumOf { it.points }, earned)
    }
}
