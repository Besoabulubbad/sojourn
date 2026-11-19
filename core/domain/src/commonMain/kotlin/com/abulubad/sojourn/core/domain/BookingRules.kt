package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.Stay
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlin.math.min
import kotlin.time.Instant

/** What cancelling gives back (ScreenBookings card: free until the deadline; after it, the first 7 nights are kept). */
sealed interface Refund {
    /** Before the free-cancellation deadline: everything paid. */
    data class Full(val amount: Money) : Refund

    /** After it: [amount] back, the first [keptNights] nights kept. */
    data class Partial(val amount: Money, val keptNights: Int) : Refund

    /** A stay of [KEPT_NIGHTS] nights or fewer, after the deadline: nothing back. */
    data object None : Refund
}

/** The rules a booking follows once made: its status on a day, when the key code shows, and what cancelling returns. */
object BookingRules {

    /** After the free-cancellation deadline this many nights, from the first, are not refunded. */
    const val KEPT_NIGHTS = 7

    /** How far ahead of arrival the next stay leads Explore (ScreenBookings card). */
    const val ARRIVING_DAYS = 2

    /** Status follows the calendar: upcoming before check-in, active until check-out, completed after. */
    fun statusOn(booking: Booking, today: LocalDate): BookingStatus = when {
        booking.status == BookingStatus.Cancelled -> BookingStatus.Cancelled
        today < booking.dates.start -> BookingStatus.Upcoming
        today < booking.dates.end -> BookingStatus.Active
        else -> BookingStatus.Completed
    }

    /** Days until check-in: 0 on arrival day. */
    fun daysUntil(booking: Booking, today: LocalDate): Int = today.daysUntil(booking.dates.start)

    /** The key code shows from arrival day until check-out, never earlier (ScreenBookings card). */
    fun showsKeyCode(booking: Booking, today: LocalDate): Boolean = statusOn(booking, today) == BookingStatus.Active

    /** The hour, on the phone's clock, of the evening before check-in when the arrival reminder shows. */
    const val REMINDER_HOUR = 18

    /**
     * When the arrival reminder shows: [REMINDER_HOUR]:00 on the day before check-in, in [timeZone] (the phone's).
     * Null when that moment has passed by [now], or the booking is cancelled: a reminder never shows late.
     */
    fun reminderAt(booking: Booking, now: Instant, timeZone: TimeZone): Instant? {
        if (booking.status == BookingStatus.Cancelled) return null
        val at = LocalDateTime(booking.dates.start.minus(DatePeriod(days = 1)), LocalTime(REMINDER_HOUR, 0))
            .toInstant(timeZone)
        return at.takeIf { it > now }
    }

    /** From [ARRIVING_DAYS] before check-in until check-out, the stay leads Explore. */
    fun isArriving(booking: Booking, today: LocalDate): Boolean = when (statusOn(booking, today)) {
        BookingStatus.Active -> true
        BookingStatus.Upcoming -> daysUntil(booking, today) <= ARRIVING_DAYS
        else -> false
    }

    /** True while the guest can still cancel: before check-in and not cancelled already. */
    fun canCancel(booking: Booking, today: LocalDate): Boolean = statusOn(booking, today) == BookingStatus.Upcoming

    /** The last day cancelling is free: the stay's own date, or so many days before check-in; null if never free. */
    fun freeCancellationUntil(booking: Booking, stay: Stay?): LocalDate? = stay?.policies?.firstNotNullOfOrNull {
        when (it) {
            is Policy.FreeCancellation -> it.until
            is Policy.FreeCancellationBefore -> booking.dates.start.minus(DatePeriod(days = it.daysBeforeCheckIn))
            else -> null
        }
    }

    /** What cancelling on [today] gives back. Nights kept are charged pro rata of the total, fees included. */
    fun refund(booking: Booking, stay: Stay?, today: LocalDate): Refund {
        val deadline = freeCancellationUntil(booking, stay)
        if (deadline != null && today <= deadline) return Refund.Full(booking.total)
        val nights = booking.dates.nights
        val kept = min(KEPT_NIGHTS, nights)
        if (kept >= nights) return Refund.None
        val back = Money(booking.total.minor * (nights - kept) / nights, booking.total.currency)
        return Refund.Partial(back, kept)
    }
}
