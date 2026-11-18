package com.abulubad.sojourn.feature.checkout

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.adults
import com.abulubad.sojourn.core.localization.resources.checkout_dates_value
import com.abulubad.sojourn.core.localization.resources.children
import com.abulubad.sojourn.core.localization.resources.list_series
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** "12–27 Mar 2027 · 15 nights". */
@Composable
internal fun datesText(dates: DateRange): String {
    val formatter = rememberSojournFormatter()
    return stringResource(
        Res.string.checkout_dates_value,
        formatter.dateRange(dates),
        pluralStringResource(Res.plurals.nights, dates.nights, formatter.count(dates.nights)),
    )
}

/** "2 adults", or "2 adults, 1 child". */
@Composable
internal fun guestsText(guests: Guests): String {
    val formatter = rememberSojournFormatter()
    val adults = pluralStringResource(Res.plurals.adults, guests.adults, formatter.count(guests.adults))
    if (guests.children == 0) return adults
    val children = pluralStringResource(Res.plurals.children, guests.children, formatter.count(guests.children))
    return stringResource(Res.string.list_series, adults, children)
}
