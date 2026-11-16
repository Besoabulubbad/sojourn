package com.abulubad.sojourn.core.designsystem.components.daterange

import com.abulubad.sojourn.core.model.DateRange
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateRangeSelectionTest {

    private fun march(day: Int) = LocalDate(2027, 3, day)
    private val booked = setOf(march(20))
    private val isAvailable: (LocalDate) -> Boolean = { it !in booked }

    @Test
    fun `the first tap sets check-in`() {
        assertEquals(DateSelectionStep.CheckIn(march(12)), nextSelection(null, march(12), 1, isAvailable))
    }

    @Test
    fun `a later tap completes the range`() {
        assertEquals(
            DateSelectionStep.Complete(DateRange(march(12), march(19))),
            nextSelection(march(12), march(19), 1, isAvailable),
        )
    }

    @Test
    fun `a tap on or before check-in moves check-in`() {
        assertEquals(DateSelectionStep.CheckIn(march(10)), nextSelection(march(12), march(10), 1, isAvailable))
        assertEquals(DateSelectionStep.CheckIn(march(12)), nextSelection(march(12), march(12), 1, isAvailable))
    }

    @Test
    fun `a range shorter than the minimum is ignored`() {
        assertEquals(DateSelectionStep.CheckIn(march(12)), nextSelection(march(12), march(14), 3, isAvailable))
    }

    @Test
    fun `a range across a booked night starts again from the tapped day`() {
        assertEquals(DateSelectionStep.CheckIn(march(22)), nextSelection(march(12), march(22), 1, isAvailable))
    }
}
