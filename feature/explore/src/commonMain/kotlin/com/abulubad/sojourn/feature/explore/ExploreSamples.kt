package com.abulubad.sojourn.feature.explore

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.common.AppError
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
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate

/** Explore in each of its states, for previews and screenshots. Lisbon's photo fails, to show that slot state. */
object ExploreSamples {

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
        city("lisbon", "PT", 41, 1_480, SojournSamplePhotos.FAILED),
    )

    private val lastSearch = SearchQuery(
        cityId = CityId("london"),
        dates = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27)),
        guests = Guests(adults = 2),
    )

    /** Returning guest: cities and the last search. */
    val content = ExploreUiState(
        cities = ScreenState.Content(cities),
        resume = ExploreResume.LastSearch(lastSearch, cities.first().photo),
    )

    /** First run: no "Continue" row, cities loading. */
    val firstRun = ExploreUiState(cities = ScreenState.Loading)

    /** Offline with cached cities: the banner, then the screen as usual. */
    val offlineCached = content.copy(cities = ScreenState.Offline(cities))

    /** Offline with nothing cached: search still works; the cities section offers a retry. */
    val offlineEmpty = ExploreUiState(cities = ScreenState.Offline(null))

    /** The cities could not be loaded. */
    val error = ExploreUiState(cities = ScreenState.Error(AppError.Remote(503)))

    @Composable
    fun Screen(state: ExploreUiState) = WithSojournSamplePhotos { ExploreScreen(state, onIntent = {}) }
}
