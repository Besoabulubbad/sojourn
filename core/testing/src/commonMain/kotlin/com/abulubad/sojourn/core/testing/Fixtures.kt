package com.abulubad.sojourn.core.testing

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.City
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
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceBreakdown
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import com.abulubad.sojourn.core.model.PriceLine
import com.abulubad.sojourn.core.model.PriceLineKind
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/** A clock that never moves: tests and screenshots are reproducible. */
class FixedAppClock(
    private val instant: Instant = Instant.parse("2026-03-01T09:00:00Z"),
    override val timeZone: TimeZone = TimeZone.UTC,
) : AppClock {
    override fun now(): Instant = instant
}

/** Sample data shared by unit, screenshot and UI tests. Numbers match the design system's previews. */
object StayFixtures {

    val london = City(
        id = CityId("london"),
        countryCode = "GB",
        location = GeoPoint(51.5074, -0.1278),
        stayCount = 14,
        fromPrice = PriceDisplay.From(Money.of(2100, Currency.GBP), PriceBasis.Month, listOf(PriceExclusion.Utilities)),
    )

    val canalStudio = Stay(
        id = StayId("stay-canal-studio"),
        cityId = london.id,
        areaKey = "area_paddington",
        title = HostText("Bright studio by the canal", "en"),
        description = HostText("A calm, light-filled studio two minutes from the canal.", "en"),
        facts = StayFacts(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2),
        photos = listOf(Photo(PhotoId("p1"), "https://example.invalid/stay/canal-1.jpg")),
        amenities = setOf(Amenity.Wifi, Amenity.Washer, Amenity.Workspace, Amenity.Lift),
        policies = listOf(
            Policy.FreeCancellation(LocalDate(2026, 2, 26)),
            Policy.Deposit(Money.of(500, Currency.GBP)),
            Policy.CheckIn(LocalTime(15, 0), CheckInMethod.SelfCheckIn),
        ),
        rating = Rating(4.8, 27),
        location = GeoArea(GeoPoint(51.5190, -0.1750), radiusMetres = 350),
        fromPrice = PriceDisplay.From(Money.of(92, Currency.GBP), PriceBasis.Night, listOf(PriceExclusion.CleaningFee)),
        walkMinutesToTransit = 8,
        longStayDiscountPercent = 10,
    )

    val arabicTitleStay = canalStudio.copy(
        id = StayId("stay-algiers-flat"),
        cityId = CityId("algiers"),
        areaKey = "area_hydra",
        title = HostText("شقة مشرقة في حيدرة", "ar"),
        description = HostText("شقة هادئة مليئة بالضوء على بعد دقائق من الحديقة.", "ar"),
        fromPrice = PriceDisplay.From(Money.of(9000, Currency.DZD), PriceBasis.Night, listOf(PriceExclusion.Utilities)),
        rating = Rating(4.5, 2),
    )
}

object QuoteFixtures {

    val marchFortnight = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))

    /** £92 × 15 nights + £3 cleaning = £1,383, the total the brand book uses everywhere. */
    val canalStudioFortnight = Quote(
        id = QuoteId("quote-canal-march"),
        stayId = StayFixtures.canalStudio.id,
        dates = marchFortnight,
        guests = Guests(adults = 2),
        breakdown = PriceBreakdown.of(
            listOf(
                PriceLine(
                    PriceLineKind.Nights,
                    Money.of(1380, Currency.GBP),
                    quantity = 15,
                    unitAmount = Money.of(92, Currency.GBP),
                ),
                PriceLine(PriceLineKind.CleaningFee, Money.of(3, Currency.GBP)),
            ),
        ),
    )
}
