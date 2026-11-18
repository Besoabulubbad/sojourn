package com.abulubad.sojourn.feature.results

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.preview.SojournSamplePhotos
import com.abulubad.sojourn.core.designsystem.preview.WithSojournSamplePhotos
import com.abulubad.sojourn.core.domain.SearchResults
import com.abulubad.sojourn.core.domain.StayResult
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.GeoArea
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.HostText
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Photo
import com.abulubad.sojourn.core.model.PhotoId
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.SortOrder
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.model.StayLength
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate

/**
 * Results in each of its states, for previews and screenshots: three London stays for 12 to 27 March, two guests,
 * as the card shows them. The loft's second photo fails, to show that slot state.
 */
object ResultsSamples {

    private fun photos(id: String, vararg urls: String) =
        urls.mapIndexed { index, url -> Photo(PhotoId("$id-$index"), url) }

    private fun nightly(pounds: Long) =
        PriceDisplay.From(Money.of(pounds, Currency.GBP), PriceBasis.Night, listOf(PriceExclusion.CleaningFee))

    private val studio = Stay(
        id = StayId("canal-studio"),
        cityId = CityId("london"),
        areaKey = "area_paddington",
        title = HostText("Bright studio by the canal", "en"),
        description = HostText("A calm, light-filled studio by the canal.", "en"),
        facts = StayFacts(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2),
        photos = photos(
            "canal-studio",
            SojournSamplePhotos.LIVING,
            SojournSamplePhotos.KITCHEN,
            SojournSamplePhotos.BEDROOM,
        ),
        amenities = setOf(Amenity.Wifi, Amenity.Workspace),
        policies = emptyList(),
        rating = Rating(4.8, 32),
        location = GeoArea(GeoPoint(51.5190, -0.1750), radiusMetres = 350),
        fromPrice = nightly(92),
        longStayDiscountPercent = 10,
    )

    private val loft = studio.copy(
        id = StayId("desk-loft"),
        areaKey = "area_shoreditch",
        title = HostText("Loft with a desk and a view", "en"),
        facts = StayFacts(bedrooms = 1, beds = 1, bathrooms = 1, sleeps = 2),
        photos = photos("desk-loft", SojournSamplePhotos.APARTMENT, SojournSamplePhotos.FAILED),
        rating = Rating(4.7, 18),
        fromPrice = nightly(104),
        longStayDiscountPercent = null,
    )

    private val mews = studio.copy(
        id = StayId("garden-mews"),
        areaKey = "area_notting_hill",
        title = HostText("Garden mews house", "en"),
        facts = StayFacts(bedrooms = 2, beds = 2, bathrooms = 2, sleeps = 4),
        photos = photos("garden-mews", SojournSamplePhotos.BEDROOM, SojournSamplePhotos.LIVING),
        rating = Rating(5.0, 2),
        fromPrice = nightly(160),
        longStayDiscountPercent = null,
    )

    private val march = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))

    private fun total(stay: Stay, pounds: Long): StayResult {
        val price = PriceDisplay.Total(QuoteId("q-${stay.id.value}"), Money.of(pounds, Currency.GBP), march.nights)
        return StayResult(stay, price, stay.rating, saved = false)
    }

    private val dated = SearchResults(
        listOf(total(studio, 1_383).copy(saved = true), total(loft, 1_560), total(mews, 2_400)),
        PriceBasis.Stay,
    )

    private val datedQuery = SearchQuery(cityId = CityId("london"), dates = march, guests = Guests(adults = 2))

    /** Dates chosen: all-in totals, the studio saved, one quick filter on. */
    val content = ResultsUiState(
        query = datedQuery,
        filters = Filters(amenities = setOf(Amenity.Workspace)),
        results = ScreenState.Content(dated),
    )

    /** No dates, a month: "from … a month" with what each excludes. */
    val undated = ResultsUiState(
        query = SearchQuery(cityId = CityId("london"), length = StayLength.Month),
        results = ScreenState.Content(
            SearchResults(
                listOf(studio, loft, mews).map { stay ->
                    val month = Money(stay.fromPrice.amount.minor * MONTH, Currency.GBP)
                    val from = PriceDisplay.From(month, PriceBasis.Month, stay.fromPrice.excludes)
                    StayResult(stay, from, stay.rating, saved = false)
                },
                PriceBasis.Month,
            ),
        ),
    )

    /** First load: skeleton cards shaped like the list. */
    val loading = ResultsUiState(query = datedQuery)

    /** Offline with cached totals, two of them priced hours ago. */
    val offline = content.copy(
        results = ScreenState.Offline(dated),
        staleHours = mapOf(studio.id to 3L, loft.id to 26L),
    )

    /** Nothing matches the filters: clear them. */
    val emptyFiltered = content.copy(
        filters = Filters(bedrooms = BedroomChoice.Studio, freeCancellation = true),
        results = ScreenState.Empty,
    )

    /** The search could not run and nothing is cached. */
    val error = content.copy(results = ScreenState.Error(AppError.Remote(503)))

    /** The sort sheet over the list, distance chosen. */
    val sorting = content.copy(filters = content.filters.copy(sort = SortOrder.DistanceToCentre), sortOpen = true)

    private const val MONTH = 30L

    @Composable
    fun Screen(state: ResultsUiState) = WithSojournSamplePhotos { ResultsScreen(state, onIntent = {}) }
}
