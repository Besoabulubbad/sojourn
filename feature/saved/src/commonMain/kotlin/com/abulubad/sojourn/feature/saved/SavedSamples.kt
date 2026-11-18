package com.abulubad.sojourn.feature.saved

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHostState
import com.abulubad.sojourn.core.designsystem.preview.SojournSamplePhotos
import com.abulubad.sojourn.core.designsystem.preview.WithSojournSamplePhotos
import com.abulubad.sojourn.core.domain.SavedStay
import com.abulubad.sojourn.core.domain.StayResult
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_undo
import com.abulubad.sojourn.core.localization.resources.saved_removed
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
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
import com.abulubad.sojourn.core.model.SavedEntry
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/** The Saved tab in each state the ScreenSaved and ScreenStates cards show. */
@Suppress("MagicNumber") // Sample data: the card's stays and prices.
object SavedSamples {

    private val march = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))
    private val savedAt = Instant.parse("2026-02-20T10:00:00Z")
    private fun pounds(major: Long) = Money.of(major, Currency.GBP)

    private fun stay(id: String, area: String, title: String, photo: String, bedrooms: Int) = Stay(
        id = StayId(id),
        cityId = CityId("london"),
        areaKey = area,
        title = HostText(title, "en"),
        description = HostText(title, "en"),
        facts = StayFacts(bedrooms = bedrooms, beds = 1, bathrooms = 1, sleeps = 2),
        photos = listOf(Photo(PhotoId("$id-1"), photo)),
        amenities = setOf(Amenity.Wifi, Amenity.Workspace),
        policies = emptyList(),
        rating = Rating(4.8, 32),
        location = GeoArea(GeoPoint(51.5180, -0.1800), radiusMetres = 350),
        fromPrice = PriceDisplay.From(pounds(2_484), PriceBasis.Month, listOf(PriceExclusion.CleaningFee)),
        longStayDiscountPercent = 10,
    )

    /** Saved from the card's March search: its total. */
    private val canal = stay("canal", "area_paddington", "Bright studio by the canal", SojournSamplePhotos.LIVING, 0)

    /** Saved from a search without dates: its monthly "from" price. */
    private val loft = stay("loft", "area_shoreditch", "Loft with a desk and a view", SojournSamplePhotos.APARTMENT, 1)

    val stays = SavedUiState(
        ScreenState.Content(
            listOf(
                SavedStay(
                    StayResult(canal, PriceDisplay.Total(QuoteId("q-canal"), pounds(1_383), 15), canal.rating, true),
                    SavedEntry(canal.id, march, Guests(adults = 2), savedAt),
                ),
                SavedStay(
                    StayResult(loft, loft.fromPrice, loft.rating, true),
                    SavedEntry(loft.id, dates = null, guests = Guests(), savedAt = savedAt),
                ),
            ),
        ),
    )

    val empty = SavedUiState(ScreenState.Empty)
    val loading = SavedUiState()

    /** The tab, with "Removed from Saved · Undo" showing when [removing]. */
    @Composable
    fun Screen(state: SavedUiState, removing: Boolean = false) = WithSojournSamplePhotos {
        val snackbar = remember { SojournSnackbarHostState() }
        val message = stringResource(Res.string.saved_removed)
        val undo = stringResource(Res.string.action_undo)
        if (removing) LaunchedEffect(snackbar) { snackbar.show(message, undo) }
        SavedScreen(state, onIntent = {}, snackbar = snackbar)
    }
}
