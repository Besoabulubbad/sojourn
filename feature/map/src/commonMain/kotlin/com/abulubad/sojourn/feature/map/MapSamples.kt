package com.abulubad.sojourn.feature.map

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.preview.SojournSamplePhotos
import com.abulubad.sojourn.core.designsystem.preview.WithSojournMapStandIn
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
import com.abulubad.sojourn.core.model.GeoBounds
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
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate

/** The map in each state the ScreenMap and ScreenStates cards show, for previews, screenshots and the showcase. */
@Suppress("MagicNumber") // Sample data: real London areas and the card's prices.
object MapSamples {

    private val london = GeoPoint(51.5074, -0.1278)
    private val march = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))
    private val query = SearchQuery(cityId = CityId("london"), dates = march, guests = Guests(adults = 2))
    private val photos = listOf(
        SojournSamplePhotos.LIVING,
        SojournSamplePhotos.KITCHEN,
        SojournSamplePhotos.BEDROOM,
        SojournSamplePhotos.APARTMENT,
    )

    private fun stay(id: String, area: String, title: String, at: GeoPoint, total: Long, rating: Rating?) = StayResult(
        stay = Stay(
            id = StayId(id),
            cityId = CityId("london"),
            areaKey = area,
            title = HostText(title, "en"),
            description = HostText(title, "en"),
            facts = StayFacts(bedrooms = 1, beds = 1, bathrooms = 1, sleeps = 2),
            photos = listOf(Photo(PhotoId("$id-1"), photos[id.length % photos.size])),
            amenities = setOf(Amenity.Wifi, Amenity.Workspace),
            policies = emptyList(),
            rating = rating,
            location = GeoArea(at, radiusMetres = 350),
            fromPrice = PriceDisplay.From(
                Money.of(92, Currency.GBP),
                PriceBasis.Night,
                listOf(PriceExclusion.CleaningFee),
            ),
        ),
        price = PriceDisplay.Total(QuoteId("q-$id"), Money.of(total, Currency.GBP), nights = 15),
        rating = rating,
        saved = false,
    )

    /** The card's stay, selected: £1,383 for 15 nights. */
    private val canal = stay(
        "canal",
        "area_paddington",
        "Bright studio by the canal",
        GeoPoint(51.5180, -0.1800),
        1_383,
        Rating(4.8, 32),
    )

    private val stays = listOf(
        canal,
        stay(
            "hill",
            "area_notting_hill",
            "Garden flat off Portobello",
            GeoPoint(51.5120, -0.2020),
            1_210,
            Rating(4.6, 18),
        ),
        stay(
            "camden",
            "area_camden",
            "Loft by the lock",
            GeoPoint(51.5410, -0.1450),
            1_560,
            Rating(4.7, 21),
        ),
        stay(
            "bank",
            "area_south_bank",
            "Riverside two-bed",
            GeoPoint(51.5050, -0.1150),
            1_095,
            null,
        ),
        stay(
            "cross",
            "area_kings_cross",
            "Compact studio by the station",
            GeoPoint(51.5300, -0.1230),
            1_740,
            Rating(5.0, 7),
        ),
        // Two in Shoreditch, close enough to share one pin at city zoom.
        stay(
            "ditch-a",
            "area_shoreditch",
            "Warehouse loft",
            GeoPoint(51.5262, -0.0790),
            1_655,
            Rating(4.9, 40),
        ),
        stay(
            "ditch-b",
            "area_shoreditch",
            "Quiet mews house",
            GeoPoint(51.5250, -0.0775),
            1_820,
            Rating(4.5, 11),
        ),
    )

    private val results = SearchResults(stays, PriceBasis.Stay)

    /** The card's frame: the canal studio selected and its card raised; Notting Hill seen before. */
    val selected = MapUiState(
        query = query,
        cityCenter = london,
        results = ScreenState.Content(results),
        selected = canal.stay.id,
        seen = setOf(StayId("hill")),
    )

    /** Browsing: no card, and the guest has moved the map, so "Search this area" shows. */
    val moved = selected.copy(selected = null, movedTo = GeoBounds(51.50, -0.21, 51.55, -0.07))

    /** "Search this area" over a part of town with no stays. */
    val areaEmpty = selected.copy(selected = null, area = GeoBounds(51.46, -0.30, 51.47, -0.29))

    /** No stay matches the filters: a note with "Clear filters". */
    val empty = MapUiState(
        query = query,
        cityCenter = london,
        filters = Filters(bedrooms = BedroomChoice.ThreeOrMore),
        results = ScreenState.Empty,
    )

    val loading = MapUiState(query = query, cityCenter = london)
    val offline = selected.copy(results = ScreenState.Offline(results), selected = null)
    val error = MapUiState(query = query, cityCenter = london, results = ScreenState.Error(AppError.Remote(503)))

    @Composable
    fun Screen(state: MapUiState) = WithSojournSamplePhotos {
        WithSojournMapStandIn { MapScreen(state, onIntent = {}) }
    }
}
