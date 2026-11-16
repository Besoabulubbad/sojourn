package com.abulubad.sojourn.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Check-in to check-out. [end] is the departure day, so the number of nights is the day difference. */
data class DateRange(val start: LocalDate, val end: LocalDate) {
    init {
        require(end > start) { "Check-out ($end) must be after check-in ($start)" }
    }

    val nights: Int get() = start.daysUntil(end)

    operator fun contains(date: LocalDate): Boolean = date >= start && date < end
}

/**
 * Length of stay as a first-class input (brand book). Shortcuts on Explore and the search flow; [minNights]
 * is what the shortcut searches for.
 */
enum class StayLength(val minNights: Int) {
    Weekend(2),
    Week(7),
    Month(28),
    Longer(90),
    ;

    companion object {
        /** The shortcut a chosen range falls into, for the long-stay price notes. */
        fun forNights(nights: Int): StayLength = entries.lastOrNull { nights >= it.minNights } ?: Weekend
    }
}
