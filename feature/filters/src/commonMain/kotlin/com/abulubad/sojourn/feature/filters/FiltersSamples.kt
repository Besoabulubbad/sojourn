package com.abulubad.sojourn.feature.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.domain.FilterPreview
import com.abulubad.sojourn.core.domain.PriceSpread
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Filters
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.SearchQuery
import kotlinx.datetime.LocalDate

/** The filter sheet in each state, for previews, screenshots and the showcase. */
object FiltersSamples {

    private val london = CityId("london")
    private val totalsHistogram = listOf(0, 1, 2, 2, 3, 3, 2, 1, 1, 1, 1, 0, 1, 0)
    private val monthlyHistogram = listOf(1, 1, 2, 3, 1, 2, 1, 0, 1, 0, 0, 1, 0, 1)

    /** 12 to 27 March: 15 nights, as on the card. */
    private val dated = SearchQuery(
        cityId = london,
        dates = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27)),
        guests = Guests(adults = 2),
    )

    private fun pounds(min: Long, max: Long) = MoneyRange(Money.of(min, Currency.GBP), Money.of(max, Currency.GBP))

    /** Where the stays' totals sit, shaped like the card's histogram. */
    private val totals = PriceSpread(pounds(600, 3_400), totalsHistogram)

    /** The card: a total-price range, a studio with a workspace, 14 stays. */
    val card = FiltersUiState(
        query = dated,
        draft = Filters(
            priceRange = pounds(900, 2_400),
            basis = PriceBasis.Stay,
            amenities = setOf(Amenity.Workspace),
            bedrooms = BedroomChoice.Studio,
        ),
        preview = FilterPreview(count = 14, basis = PriceBasis.Stay, prices = totals),
        prices = totals,
        counting = false,
    )

    private val monthly = PriceSpread(pounds(1_800, 5_200), monthlyHistogram)

    /** Without dates: monthly "from" prices, nothing set, every stay in the city. */
    val undated = FiltersUiState(
        query = SearchQuery(cityId = london),
        draft = Filters.None,
        preview = FilterPreview(count = 14, basis = PriceBasis.Month, prices = monthly),
        prices = monthly,
        counting = false,
    )

    /** Nothing left: "No stays match", disabled; the slider keeps the last prices seen. */
    val noMatch = card.copy(
        draft = card.draft.copy(
            bedrooms = BedroomChoice.ThreeOrMore,
            minBeds = 4,
            minBathrooms = 3,
            amenities = setOf(Amenity.Workspace, Amenity.Parking, Amenity.PetFriendly),
            freeCancellation = true,
            instantBook = true,
        ),
        preview = FilterPreview(count = 0, basis = PriceBasis.Stay, prices = null),
    )

    /** Before the first count: no slider yet, "Show stays". */
    val counting = FiltersUiState(query = dated, draft = Filters.None)

    /** The sheet over the screen's background, as the overlay scene draws it over Results. */
    @Composable
    fun Screen(state: FiltersUiState, modifier: Modifier = Modifier) {
        Box(modifier.fillMaxSize().background(SojournTheme.colors.bg)) {
            FiltersScreen(state = state, onIntent = {})
        }
    }
}
