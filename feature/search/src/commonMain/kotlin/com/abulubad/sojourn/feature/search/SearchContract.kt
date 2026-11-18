package com.abulubad.sojourn.feature.search

import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.navigation.SearchStep
import com.abulubad.sojourn.core.navigation.SojournNavKey
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.UiEffect
import com.abulubad.sojourn.core.ui.UiIntent
import com.abulubad.sojourn.core.ui.UiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

/**
 * The search flow's state: the [step] on screen and the search taking shape across Where, When and Who.
 *
 * When has two tabs ([mode]). *Dates* keeps a [dates] range and the [month] the calendar shows; *Flexible* keeps a
 * [length] and a [startMonth] from [startMonths]. Choosing "Any time" sets [anyTime]: the search then runs without
 * dates or length, and Who says what that means for prices.
 */
data class SearchUiState(
    val step: SearchStep,
    val today: LocalDate,
    val cities: ScreenState<List<City>> = ScreenState.Loading,
    val cityId: CityId? = null,
    val mode: WhenMode = WhenMode.Dates,
    val dates: DateRange? = null,
    val month: YearMonth = today.yearMonth,
    val length: StayLength = StayLength.Month,
    val startMonth: YearMonth = today.yearMonth,
    val startMonths: List<YearMonth> = listOf(today.yearMonth),
    val anyTime: Boolean = false,
    val guests: Guests = Guests(),
) : UiState {

    /** When's "Next" works once the tab has an answer: a full range, or always on Flexible. */
    val canContinueFromWhen: Boolean get() = mode == WhenMode.Flexible || dates != null

    /** The search the flow describes right now. */
    val query: SearchQuery
        get() = SearchQuery(
            cityId = cityId,
            dates = dates.takeIf { !anyTime && mode == WhenMode.Dates },
            guests = guests,
            length = length.takeIf { !anyTime && mode == WhenMode.Flexible },
            startMonth = startMonth.takeIf { !anyTime && mode == WhenMode.Flexible },
        )

    companion object {
        /** Ages 2 to 12 count as children; the catalogue's largest stays sleep under these limits. */
        val Adults: IntRange = 1..16
        val Children: IntRange = 0..10

        /** "Starting in" offers this month and the next three. */
        const val START_MONTHS = 4
    }
}

/** The When step's two tabs (SegmentedControl). */
enum class WhenMode { Dates, Flexible }

/** What the guest did in the search flow. */
sealed interface SearchIntent : UiIntent {
    data class CityTapped(val id: CityId) : SearchIntent
    data class ModeSelected(val mode: WhenMode) : SearchIntent
    data class MonthShown(val month: YearMonth) : SearchIntent
    data class DatesSelected(val dates: DateRange?) : SearchIntent
    data class LengthSelected(val length: StayLength) : SearchIntent
    data class StartMonthSelected(val month: YearMonth) : SearchIntent
    data object NextTapped : SearchIntent
    data object AnyTimeTapped : SearchIntent
    data class AdultsChanged(val adults: Int) : SearchIntent
    data class ChildrenChanged(val children: Int) : SearchIntent
    data object SearchTapped : SearchIntent
    data object BackTapped : SearchIntent
    data object RetryTapped : SearchIntent
}

/** The flow's one-off outputs: leave it, or leave it for the results. */
sealed interface SearchEffect : UiEffect {
    data object Close : SearchEffect
    data class ShowResults(val key: SojournNavKey) : SearchEffect
}
