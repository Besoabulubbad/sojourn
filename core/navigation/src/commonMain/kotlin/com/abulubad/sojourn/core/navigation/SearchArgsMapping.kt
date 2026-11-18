package com.abulubad.sojourn.core.navigation

import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.parseOrNull

/** A search as navigation arguments: ids, ISO dates and enum names only (ADR 0005). */
fun SearchQuery.toArgs(): SearchArgs = SearchArgs(
    cityId = cityId?.value,
    checkIn = dates?.start?.toString(),
    checkOut = dates?.end?.toString(),
    adults = guests.adults,
    children = guests.children,
    length = length?.name,
    startMonth = startMonth?.toString(),
)

/**
 * The search the arguments describe. A back stack restored from an older build can hold values this one rejects:
 * an unparseable or backwards date pair, an unknown length or month, or a guest count out of range is dropped (or
 * clamped) rather than crashing the screen.
 */
fun SearchArgs.toQuery(): SearchQuery {
    val start = checkIn?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val end = checkOut?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    return SearchQuery(
        cityId = cityId?.let(::CityId),
        dates = if (start != null && end != null && end > start) DateRange(start, end) else null,
        guests = Guests(adults.coerceAtLeast(1), children.coerceAtLeast(0)),
        length = StayLength.entries.find { it.name == length },
        startMonth = startMonth?.let(YearMonth::parseOrNull),
    )
}
