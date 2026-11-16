package com.abulubad.sojourn.core.designsystem.components.daterange

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.DateRange
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** The DateRangePicker card's example: March 2027, the 1st–4th booked, 12–27 chosen (15 nights). */
object SojournDateRangePickerSamples {

    val March2027 = YearMonth(2027, 3)
    val Chosen = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 3, 27))
    private val Booked = LocalDate(2027, 3, 1)..LocalDate(2027, 3, 4)

    fun availability(date: LocalDate): SojournDayState =
        if (date in Booked) SojournDayState.Unavailable else SojournDayState.Available

    @Composable
    fun March(modifier: Modifier = Modifier) {
        var month by remember { mutableStateOf(March2027) }
        var selection by remember { mutableStateOf<DateRange?>(Chosen) }
        SojournDateRangePicker(
            month = month,
            onMonthChange = { month = it },
            selection = selection,
            onSelect = { selection = it },
            availability = ::availability,
            modifier = modifier.padding(SojournSpacing.space4),
        )
    }
}
