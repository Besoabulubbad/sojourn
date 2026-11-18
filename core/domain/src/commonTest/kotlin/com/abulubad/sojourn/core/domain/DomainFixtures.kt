package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.GeoArea
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
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
import com.abulubad.sojourn.core.testing.StayFixtures
import kotlinx.datetime.LocalDate

/**
 * Three London stays that differ in every way a search can tell apart, and a pricing function that behaves like the
 * API's engine (nightly rate × nights, minimum stay, capacity).
 */
object DomainFixtures {

    /** £92 a night, 10 % long-stay discount, sleeps 2, rating 4.8 from 27 reviews. */
    val studio: Stay = StayFixtures.canalStudio

    /**
     * £150 a night, no discount, sleeps 5, two bedrooms, instant book, 5-night minimum, rating hidden (2 reviews),
     * a few hundred metres from the city centre. Its free cancellation closed with the studio's, before 1 March.
     */
    val family: Stay = studio.copy(
        id = StayId("stay-family"),
        facts = StayFacts(bedrooms = 2, beds = 3, bathrooms = 2, sleeps = 5),
        amenities = setOf(Amenity.Wifi, Amenity.Balcony),
        rating = Rating(4.6, 2),
        fromPrice = nightly(150),
        longStayDiscountPercent = null,
        instantBook = true,
        policies = studio.policies + Policy.MinimumStay(5),
        location = GeoArea(GeoPoint(51.5080, -0.1290), radiusMetres = 300),
    )

    /** £120 a night, sleeps 3, one bedroom, rating 4.2 from 40 reviews, free cancellation up to a week out, far out. */
    val flat: Stay = studio.copy(
        id = StayId("stay-flat"),
        facts = StayFacts(bedrooms = 1, beds = 1, bathrooms = 1, sleeps = 3),
        amenities = setOf(Amenity.Wifi, Amenity.Workspace),
        rating = Rating(4.2, 40),
        fromPrice = nightly(120),
        longStayDiscountPercent = null,
        policies = listOf(Policy.FreeCancellationBefore(daysBeforeCheckIn = 7)),
        location = GeoArea(GeoPoint(51.5500, -0.2000), radiusMetres = 400),
    )

    val london: List<Stay> = listOf(studio, family, flat)

    private fun nightly(pounds: Long) =
        PriceDisplay.From(Money.of(pounds, Currency.GBP), PriceBasis.Night, listOf(PriceExclusion.CleaningFee))

    val march = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))

    /** Prices like the engine: one Nights line, `minimum_nights` and `too_many_guests` as validation errors. */
    fun engine(stays: List<Stay> = london): (StayId, DateRange, Guests) -> AppResult<Quote> = { id, dates, guests ->
        val stay = stays.find { it.id == id }
        val minimum = stay?.policies?.filterIsInstance<Policy.MinimumStay>()?.maxOfOrNull { it.nights } ?: 1
        when {
            stay == null -> AppResult.Failure(AppError.NotFound(id.value))
            dates.nights < minimum -> AppResult.Failure(AppError.Validation("minimum_nights"))
            guests.total > stay.facts.sleeps -> AppResult.Failure(AppError.Validation("too_many_guests"))
            else -> AppResult.Success(quote(stay, dates, guests))
        }
    }

    fun quote(stay: Stay, dates: DateRange, guests: Guests, nightly: Money = stay.fromPrice.amount): Quote = Quote(
        id = QuoteId("q-${stay.id.value}-${dates.start}-${dates.end}-${guests.adults}-${nightly.minor}"),
        stayId = stay.id,
        dates = dates,
        guests = guests,
        breakdown = PriceBreakdown.of(
            listOf(PriceLine(PriceLineKind.Nights, nightly * dates.nights, dates.nights, nightly)),
        ),
    )
}
