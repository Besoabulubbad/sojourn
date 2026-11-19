package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBreakdown
import com.abulubad.sojourn.core.model.PriceLine
import com.abulubad.sojourn.core.model.PriceLineKind
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.model.Wifi
import com.abulubad.sojourn.core.network.dto.CatalogDto
import com.abulubad.sojourn.core.network.dto.StayDto

/**
 * What the "server" knows: the catalogue, the quotes it has issued and the bookings made from them. Shared by
 * [com.abulubad.sojourn.core.network.local.LocalSojournApi] and [com.abulubad.sojourn.core.network.ktor.KtorSojournApi]
 * so both price a stay the same way. Quote pricing is deterministic: same stay, dates and guests, same quote id.
 */
internal class CatalogEngine(private val catalog: CatalogDto, private val clock: AppClock) {

    private val mapping = CatalogMapping(catalog.photoBase)
    private val issued = mutableMapOf<QuoteId, Quote>()
    private val booked = mutableMapOf<BookingId, Booking>()
    private var bookingCount = 0

    fun cities(): AppResult<List<City>> = AppResult.Success(
        catalog.cities.map { city -> mapping.city(city, catalog.stays.filter { it.cityId == city.id }) },
    )

    fun stays(cityId: CityId?): AppResult<List<Stay>> = AppResult.Success(
        catalog.stays.filter { cityId == null || it.cityId == cityId.value }.map(mapping::stay),
    )

    fun stay(id: StayId): AppResult<Stay> =
        find(id)?.let { AppResult.Success(mapping.stay(it)) } ?: AppResult.Failure(AppError.NotFound(id.value))

    fun blockedDates(id: StayId): AppResult<List<DateRange>> =
        find(id)?.let { dto -> AppResult.Success(dto.blocked.map(mapping::range)) }
            ?: AppResult.Failure(AppError.NotFound(id.value))

    fun reviews(id: StayId): AppResult<List<Review>> =
        find(id)?.let { dto -> AppResult.Success(mapping.reviews(dto)) }
            ?: AppResult.Failure(AppError.NotFound(id.value))

    fun quote(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote> {
        val dto = find(stayId) ?: return AppResult.Failure(AppError.NotFound(stayId.value))
        val problem = when {
            dates.nights < dto.policies.minimumNights -> "minimum_nights"
            guests.total > dto.facts.sleeps -> "too_many_guests"
            dto.blocked.map(mapping::range).any { it.overlaps(dates) } -> "unavailable"
            isBooked(stayId, dates) -> "unavailable"
            else -> null
        }
        if (problem != null) return AppResult.Failure(AppError.Validation(problem))
        val quote = Quote(
            id = QuoteId("q-${dto.id}-${dates.start}-${dates.end}-${guests.adults}-${guests.children}"),
            stayId = stayId,
            dates = dates,
            guests = guests,
            breakdown = PriceBreakdown.of(lines(dto, dates.nights)),
        )
        issued[quote.id] = quote
        return AppResult.Success(quote)
    }

    fun book(quoteId: QuoteId): AppResult<Booking> {
        val quote = issued[quoteId] ?: return AppResult.Failure(AppError.NotFound(quoteId.value))
        if (isBooked(quote.stayId, quote.dates)) return AppResult.Failure(AppError.Validation("unavailable"))
        bookingCount++
        val reference = reference(quote)
        val booking = Booking(
            id = BookingId("b-$bookingCount-${quote.stayId.value}"),
            reference = reference,
            stayId = quote.stayId,
            quoteId = quote.id,
            dates = quote.dates,
            guests = quote.guests,
            total = quote.total,
            status = BookingStatus.Upcoming,
            createdAt = clock.now(),
            keyCode = keyCode(reference),
            address = address(reference),
            wifi = wifi(reference),
        )
        booked[booking.id] = booking
        return AppResult.Success(booking)
    }

    /**
     * Cancels a booking. The demo engine is rebuilt at each launch and forgets earlier bookings, so a booking it does
     * not hold is taken as cancelled too; a real API keeps them and answers `NotFound`.
     */
    fun cancel(id: BookingId): AppResult<Unit> {
        booked[id]?.let { booked[id] = it.copy(status = BookingStatus.Cancelled) }
        return AppResult.Success(Unit)
    }

    /** True when a booking this engine made, not cancelled, holds any of [dates] at [stayId]. */
    private fun isBooked(stayId: StayId, dates: DateRange): Boolean = booked.values.any {
        it.stayId == stayId && it.status != BookingStatus.Cancelled && it.dates.overlaps(dates)
    }

    /** Six digits for the door, stable for a reference. */
    private fun keyCode(reference: String): String =
        ((reference.hashCode().toLong() and HASH_MASK) % KEY_CODE_RANGE).toString().padStart(KEY_CODE_DIGITS, '0')

    /**
     * The flat and building, stable for a reference. The buildings are made up, and there is no street or number, so
     * a demo address never points at someone's real home; the area and city come from the stay.
     */
    private fun address(reference: String): String {
        val hash = reference.hashCode().toLong() and HASH_MASK
        val flat = hash % FLATS + 1
        return "Flat $flat, ${BUILDINGS[((hash / FLATS) % BUILDINGS.size).toInt()]} House"
    }

    /** The stay's Wi-Fi, stable for a reference: a network named after the reference and a three-part password. */
    private fun wifi(reference: String): Wifi {
        val hash = reference.reversed().hashCode().toLong() and HASH_MASK
        val word = { i: Int -> WIFI_WORDS[((hash shr (i * WORD_BITS)) % WIFI_WORDS.size).toInt()] }
        val digits = (hash % WIFI_DIGITS_RANGE).toString().padStart(WIFI_DIGITS, '0')
        return Wifi(
            network = "Sojourn-${reference.takeLast(WIFI_NAME_CHARS)}",
            password = "${word(0)}-${word(1)}-$digits",
        )
    }

    /** Nights × rate, the long-stay discount when it applies, then cleaning and city tax. Integer minor units. */
    private fun lines(dto: StayDto, nights: Int): List<PriceLine> {
        val pricing = dto.pricing
        val currency = CatalogMapping.currency(pricing.currency)
        val rate = Money(pricing.nightly, currency)
        val nightsTotal = rate * nights
        return buildList {
            add(PriceLine(PriceLineKind.Nights, nightsTotal, quantity = nights, unitAmount = rate))
            val percent = pricing.longStayDiscountPercent
            if (percent != null && nights >= pricing.longStayMinNights) {
                val discount = Money(-(nightsTotal.minor * percent / PERCENT), currency)
                add(PriceLine(PriceLineKind.LongStayDiscount, discount))
            }
            if (pricing.cleaningFee > 0) add(PriceLine(PriceLineKind.CleaningFee, Money(pricing.cleaningFee, currency)))
            if (pricing.cityTaxPerNight > 0) {
                add(PriceLine(PriceLineKind.CityTax, Money(pricing.cityTaxPerNight * nights, currency)))
            }
        }
    }

    /** "SJ-4F7K2Q": stable for a quote and booking count, readable, no ambiguous characters. */
    private fun reference(quote: Quote): String {
        var hash = (quote.id.value.hashCode().toLong() and HASH_MASK) + bookingCount
        val code = buildString {
            repeat(REFERENCE_LENGTH) {
                append(REFERENCE_ALPHABET[(hash % REFERENCE_ALPHABET.length).toInt()])
                hash /= REFERENCE_ALPHABET.length
            }
        }
        return "SJ-$code"
    }

    private fun find(id: StayId): StayDto? = catalog.stays.firstOrNull { it.id == id.value }

    private fun DateRange.overlaps(other: DateRange): Boolean = start < other.end && other.start < end

    private companion object {
        const val PERCENT = 100
        const val KEY_CODE_RANGE = 1_000_000L
        const val KEY_CODE_DIGITS = 6
        const val REFERENCE_LENGTH = 6
        const val HASH_MASK = 0x7FFFFFFFL
        const val FLATS = 24L
        val BUILDINGS = listOf("Linden", "Harbour", "Cedar", "Orchard", "Willow", "Juniper", "Meridian", "Larch")
        val WIFI_WORDS = listOf("maple", "harbour", "lantern", "meadow", "copper", "river", "garden", "amber")
        const val WORD_BITS = 4
        const val WIFI_DIGITS = 2
        const val WIFI_DIGITS_RANGE = 100L
        const val WIFI_NAME_CHARS = 4
        const val REFERENCE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
