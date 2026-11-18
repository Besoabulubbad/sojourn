package com.abulubad.sojourn.feature.search

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.designsystem.preview.SojournSamplePhotos
import com.abulubad.sojourn.core.designsystem.preview.WithSojournSamplePhotos
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Photo
import com.abulubad.sojourn.core.model.PhotoId
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.navigation.SearchKey
import com.abulubad.sojourn.core.navigation.SearchStep
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate

/**
 * The search flow at each step and state, for previews and screenshots: the card's four cities on Where, 12 to 27
 * March on When (the first days already past, struck through), the flexible tab, and Who after "Any time".
 * Lisbon's photo fails, to show that slot state.
 */
object SearchSamples {

    private val today = LocalDate(2027, 3, 5)

    private fun city(id: String, country: String, stays: Int, pounds: Long, photo: String) = City(
        id = CityId(id),
        countryCode = country,
        location = GeoPoint(0.0, 0.0),
        stayCount = stays,
        fromPrice = PriceDisplay.From(
            Money.of(pounds, Currency.GBP),
            PriceBasis.Month,
            listOf(PriceExclusion.Utilities),
        ),
        photo = Photo(PhotoId("city-$id"), photo),
    )

    val cities: List<City> = listOf(
        city("london", "GB", 214, 1_950, SojournSamplePhotos.LONDON),
        city("paris", "FR", 96, 1_720, SojournSamplePhotos.PARIS),
        city("algiers", "DZ", 41, 690, SojournSamplePhotos.ALGIERS),
        city("lisbon", "PT", 58, 1_240, SojournSamplePhotos.FAILED),
    )

    private val start = initialState(SearchKey(), today)

    /** Where, the cities listed. */
    val where = start.copy(cities = ScreenState.Content(cities))

    /** Where on first run: the cities loading. */
    val whereLoading = start

    /** Where offline with cached cities: the banner, then the list. */
    val whereOfflineCached = where.copy(cities = ScreenState.Offline(cities))

    /** Where offline with nothing cached: the offline state with a retry. */
    val whereOfflineEmpty = start.copy(cities = ScreenState.Offline(null))

    /** When, Dates: 12 to 27 March chosen, 15 nights in the footer. */
    val whenDates = where.copy(
        step = SearchStep.When,
        cityId = cities.first().id,
        dates = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 3, 27)),
    )

    /** When, Dates, before a range: "Next" waits for one. */
    val whenNoDates = whenDates.copy(dates = null)

    /** When, Flexible: a month, starting in March. */
    val whenFlexible = whenDates.copy(mode = WhenMode.Flexible, dates = null, length = StayLength.Month)

    /** Who after "Any time": what that means for prices, above "Search". */
    val whoAnyTime = whenDates.copy(step = SearchStep.Who, dates = null, anyTime = true, guests = Guests(adults = 2))

    @Composable
    fun Screen(state: SearchUiState) = WithSojournSamplePhotos { SearchScreen(state, onIntent = {}) }
}
