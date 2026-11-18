package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.GeoArea
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.HostText
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Photo
import com.abulubad.sojourn.core.model.PhotoId
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import com.abulubad.sojourn.core.model.PriceInclusion
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.network.dto.CityDto
import com.abulubad.sojourn.core.network.dto.DateRangeDto
import com.abulubad.sojourn.core.network.dto.HostTextDto
import com.abulubad.sojourn.core.network.dto.PricingDto
import com.abulubad.sojourn.core.network.dto.StayDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.parseOrNull

/**
 * DTO → model. Amenity, inclusion and check-in ids the app does not know are dropped rather than failing the
 * catalogue.
 */
internal class CatalogMapping(private val photoBase: String) {

    fun stay(dto: StayDto): Stay = Stay(
        id = StayId(dto.id),
        cityId = CityId(dto.cityId),
        areaKey = dto.areaKey,
        title = hostText(dto.title),
        description = hostText(dto.description),
        facts = with(dto.facts) { StayFacts(bedrooms, beds, bathrooms, sleeps) },
        photos = dto.photos.mapIndexed { index, file -> Photo(PhotoId("${dto.id}-$index"), photoBase + file) },
        amenities = dto.amenities.mapNotNull { id -> Amenity.entries.firstOrNull { it.id == id } }.toSet(),
        policies = policies(dto),
        rating = dto.rating?.let { Rating(it.score, it.count) },
        location = GeoArea(GeoPoint(dto.area.latitude, dto.area.longitude), dto.area.radiusMetres),
        fromPrice = fromPrice(dto.pricing),
        walkMinutesToTransit = dto.walkMinutesToTransit,
        longStayDiscountPercent = dto.pricing.longStayDiscountPercent,
        instantBook = dto.instantBook,
        inclusions = dto.inclusions.mapNotNull { id -> PriceInclusion.entries.firstOrNull { it.id == id } }.toSet(),
        // By language, as the database returns them, so a stay reads back equal to the one written.
        descriptionTranslations = dto.descriptionTranslations.map(::hostText).sortedBy { it.languageTag },
    )

    /** Reviews newest first; one the app cannot read (a bad month) is dropped rather than failing the stay. */
    fun reviews(dto: StayDto): List<Review> = dto.reviews.mapNotNull { review ->
        YearMonth.parseOrNull(review.month)?.let { Review(review.author, it, hostText(review.text)) }
    }.sortedByDescending { it.month }

    fun city(dto: CityDto, stays: List<StayDto>): City {
        val cheapest = stays.minBy { monthlyEstimate(it.pricing) }
        return City(
            id = CityId(dto.id),
            countryCode = dto.countryCode,
            location = GeoPoint(dto.latitude, dto.longitude),
            stayCount = stays.size,
            fromPrice = PriceDisplay.From(
                Money(monthlyEstimate(cheapest.pricing), currency(cheapest.pricing.currency)),
                PriceBasis.Month,
                exclusions(cheapest.pricing),
            ),
            photo = Photo(PhotoId("city-${dto.id}"), photoBase + dto.photo),
        )
    }

    fun range(dto: DateRangeDto): DateRange = DateRange(LocalDate.parse(dto.start), LocalDate.parse(dto.end))

    /** A month at the nightly rate, after the long-stay discount a month qualifies for. */
    private fun monthlyEstimate(pricing: PricingDto): Long {
        val month = pricing.nightly * NIGHTS_PER_MONTH
        val percent = pricing.longStayDiscountPercent?.takeIf { pricing.longStayMinNights <= NIGHTS_PER_MONTH } ?: 0
        return month - month * percent / PERCENT
    }

    private fun fromPrice(pricing: PricingDto) =
        PriceDisplay.From(Money(pricing.nightly, currency(pricing.currency)), PriceBasis.Night, exclusions(pricing))

    private fun exclusions(pricing: PricingDto): List<PriceExclusion> = buildList {
        if (pricing.cleaningFee > 0) add(PriceExclusion.CleaningFee)
        if (pricing.cityTaxPerNight > 0) add(PriceExclusion.CityTax)
    }

    private fun policies(dto: StayDto): List<Policy> = with(dto.policies) {
        listOfNotNull(
            Policy.FreeCancellationBefore(freeCancellationDaysBefore),
            Policy.Deposit(Money(deposit, currency(dto.pricing.currency)), depositRefundable),
            CheckInMethod.entries.firstOrNull { it.id == checkInMethod }
                ?.let { Policy.CheckIn(LocalTime.parse(checkInFrom), it) },
            Policy.CheckOut(LocalTime.parse(checkOutBy)),
            Policy.MinimumStay(minimumNights),
        )
    }

    private fun hostText(dto: HostTextDto) = HostText(dto.text, dto.lang)

    companion object {
        const val NIGHTS_PER_MONTH = 30
        const val PERCENT = 100

        fun currency(code: String): Currency = Currency.entries.first { it.code == code }
    }
}
