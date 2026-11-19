package com.abulubad.sojourn.core.designsystem.components.daterange

import androidx.compose.ui.unit.dp

/** Measurements from the DateRangePicker card. */
object SojournDateRangePickerDefaults {
    /** The check-in and check-out discs, and the range band, are this tall; the cell itself is `touch-min`. */
    val DayHeight = 38.dp

    /** Weekday header row height. */
    val WeekdayHeight = 28.dp

    internal const val DAYS_PER_WEEK = 7
    internal const val HALF = 0.5f
}
