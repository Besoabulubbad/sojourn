package com.abulubad.sojourn.feature.profile

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.EarnedPoints
import com.abulubad.sojourn.core.domain.Rewards
import com.abulubad.sojourn.core.domain.RewardsRules
import com.abulubad.sojourn.core.localization.AppLocale
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.BookingStatus
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
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.model.ThemeMode
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** The Profile tab and its pages in each state the ScreenProfile and ScreenStates cards show. */
@Suppress("MagicNumber") // Sample data: the stays, dates and totals behind the points.
object ProfileSamples {

    private fun stay(id: String, city: String, area: String, title: String, currency: Currency) = Stay(
        id = StayId(id),
        cityId = CityId(city),
        areaKey = area,
        title = HostText(title, "en"),
        description = HostText(title, "en"),
        facts = StayFacts(bedrooms = 1, beds = 1, bathrooms = 1, sleeps = 2),
        photos = listOf(Photo(PhotoId("$id-1"), "sample:$id")),
        amenities = setOf(Amenity.Wifi),
        policies = emptyList(),
        rating = null,
        location = GeoArea(GeoPoint(51.5, -0.1), radiusMetres = 350),
        fromPrice = PriceDisplay.From(Money.of(90, currency), PriceBasis.Night, listOf(PriceExclusion.CleaningFee)),
    )

    private fun completed(id: String, stay: Stay, dates: DateRange, total: Money): EarnedPoints {
        val booking = Booking(
            id = BookingId(id),
            reference = "SJ-${id.uppercase()}",
            stayId = stay.id,
            quoteId = QuoteId("q-$id"),
            dates = dates,
            guests = Guests(adults = 2),
            total = total,
            status = BookingStatus.Completed,
            createdAt = Instant.parse("2026-01-02T10:00:00Z"),
        )
        return EarnedPoints(BookingItem(booking, stay), RewardsRules.pointsFor(booking))
    }

    private val earned = listOf(
        completed(
            "x7pd2c",
            stay("flat", "london", "area_south_bank", "Riverside two-bed for longer stays", Currency.GBP),
            DateRange(LocalDate(2026, 10, 4), LocalDate(2026, 10, 11)),
            Money.of(940, Currency.GBP),
        ),
        completed(
            "m3qa8z",
            stay("marais", "paris", "area_le_marais", "Quiet courtyard flat in the Marais", Currency.EUR),
            DateRange(LocalDate(2026, 6, 1), LocalDate(2026, 6, 4)),
            Money.of(300, Currency.EUR),
        ),
    )

    private val rewards = Rewards(earned.sumOf { it.points }, earned)

    val profile = ProfileUiState(points = rewards.points, locale = AppLocale.EN_GB, themeMode = ThemeMode.System)
    val language = LanguageUiState(AppLocale.EN_GB)
    val appearance = AppearanceUiState(ThemeMode.System)
    val rewardsContent = RewardsUiState(ScreenState.Content(rewards))
    val rewardsEmpty = RewardsUiState(ScreenState.Empty)

    @Composable
    fun Profile(state: ProfileUiState = profile) = ProfileScreen(state, onIntent = {})

    @Composable
    fun Language() = LanguageScreen(language, onIntent = {})

    @Composable
    fun Appearance() = AppearanceScreen(appearance, onIntent = {})

    @Composable
    fun Rewards(state: RewardsUiState) = RewardsScreen(state, onIntent = {})

    @Composable
    fun Help() = HelpScreen(onBack = {})
}
