package com.abulubad.sojourn.core.designsystem.components.daterange

import com.abulubad.sojourn.core.model.DateRange
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** What a tap on a day leads to: a check-in waiting for its check-out, or a finished range. */
internal sealed interface DateSelectionStep {
    data class CheckIn(val date: LocalDate) : DateSelectionStep
    data class Complete(val range: DateRange) : DateSelectionStep
}

/**
 * The picker's rules, kept pure so they are unit-tested:
 *
 * - The first tap sets check-in. A tap on or before check-in moves check-in there.
 * - A later tap completes the range if it is at least [minNights] long and every night in it is available;
 *   otherwise a range too short is ignored, and one across a booked night starts again from the tapped day.
 */
internal fun nextSelection(
    checkIn: LocalDate?,
    tapped: LocalDate,
    minNights: Int,
    isAvailable: (LocalDate) -> Boolean,
): DateSelectionStep {
    if (checkIn == null || tapped <= checkIn) return DateSelectionStep.CheckIn(tapped)
    val range = DateRange(checkIn, tapped)
    if (range.nights < minNights) return DateSelectionStep.CheckIn(checkIn)
    val nights = generateSequence(checkIn) { it.plus(DatePeriod(days = 1)) }.takeWhile { it < tapped }
    return if (nights.all(isAvailable)) DateSelectionStep.Complete(range) else DateSelectionStep.CheckIn(tapped)
}
