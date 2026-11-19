package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.HostText
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceExclusion
import com.abulubad.sojourn.core.model.PriceInclusion
import com.abulubad.sojourn.core.model.PriceLineKind
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.testing.FixedAppClock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalogEngineTest {

    private val engine = CatalogEngine(TestCatalog.catalog, FixedAppClock())
    private val studio = StayId("london-01")

    private fun range(start: String, end: String) = DateRange(LocalDate.parse(start), LocalDate.parse(end))

    @Test
    fun `a stay maps to the model with photo URLs and known amenities only and numbered policies`() {
        val stay = assertNotNull(engine.stay(studio).getOrNull())
        assertEquals("catalog/photos/a.jpg", stay.photos.first().url)
        assertEquals(setOf(Amenity.Wifi, Amenity.Washer), stay.amenities)
        assertTrue(stay.policies.first() is Policy.FreeCancellationBefore)
        assertEquals(Money.of(98, Currency.GBP), stay.fromPrice.amount)
        assertEquals(PriceBasis.Night, stay.fromPrice.per)
        assertEquals(listOf(PriceExclusion.CleaningFee), stay.fromPrice.excludes)
        assertTrue(stay.instantBook)
        assertEquals(setOf(PriceInclusion.Wifi, PriceInclusion.Utilities), stay.inclusions)
        assertEquals(listOf(HostText("استوديو هادئ.", "ar")), stay.descriptionTranslations)
    }

    @Test
    fun `reviews come newest first and an unreadable one is dropped`() {
        val reviews = assertNotNull(engine.reviews(studio).getOrNull())
        assertEquals(listOf("Léa", "Tom"), reviews.map { it.author })
        assertEquals(YearMonth(2026, 9), reviews.first().month)
        assertIs<AppError.NotFound>(engine.reviews(StayId("missing")).errorOrNull())
    }

    @Test
    fun `cities count their stays and quote a monthly from price`() {
        val london = assertNotNull(engine.cities().getOrNull()).first { it.id == CityId("london") }
        assertEquals(1, london.stayCount)
        // £98 × 30 nights = £2,940, less the 10 % long-stay discount a month qualifies for.
        assertEquals(Money.of(2_646, Currency.GBP), london.fromPrice.amount)
        assertEquals(PriceBasis.Month, london.fromPrice.per)
    }

    @Test
    fun `a short stay is nights plus cleaning and with no discount`() {
        val quote = assertNotNull(engine.quote(studio, range("2027-03-12", "2027-03-27"), Guests(2)).getOrNull())
        assertEquals(listOf(PriceLineKind.Nights, PriceLineKind.CleaningFee), quote.breakdown.lines.map { it.kind })
        assertEquals(Money.of(98 * 15 + 60, Currency.GBP), quote.total)
    }

    @Test
    fun `from 28 nights the long-stay discount applies`() {
        val quote = assertNotNull(engine.quote(studio, range("2027-03-01", "2027-03-29"), Guests(1)).getOrNull())
        val discount = quote.breakdown.lines.first { it.kind == PriceLineKind.LongStayDiscount }
        assertEquals(-Money.of(98 * 28 / 10, Currency.GBP, minorRemainder = 40), discount.amount)
    }

    @Test
    fun `city tax is charged per night where the city levies it`() {
        val quote = engine.quote(StayId("paris-01"), range("2027-03-01", "2027-03-05"), Guests(1)).getOrNull()
        val tax = assertNotNull(quote).breakdown.lines.first { it.kind == PriceLineKind.CityTax }
        assertEquals(Money(325 * 4, Currency.EUR), tax.amount)
    }

    @Test
    fun `the same stay and dates and guests give the same quote id`() {
        val dates = range("2027-03-12", "2027-03-27")
        val first = engine.quote(studio, dates, Guests(2)).getOrNull()?.id
        assertEquals(first, engine.quote(studio, dates, Guests(2)).getOrNull()?.id)
    }

    @Test
    fun `quotes enforce minimum nights and capacity and availability`() {
        assertEquals(
            AppError.Validation("minimum_nights"),
            engine.quote(studio, range("2027-03-12", "2027-03-13"), Guests(1)).errorOrNull(),
        )
        assertEquals(
            AppError.Validation("too_many_guests"),
            engine.quote(studio, range("2027-03-12", "2027-03-20"), Guests(adults = 2, children = 1)).errorOrNull(),
        )
        assertEquals(
            AppError.Validation("unavailable"),
            engine.quote(studio, range("2027-01-15", "2027-01-25"), Guests(1)).errorOrNull(),
        )
    }

    @Test
    fun `an unknown stay or quote is not found`() {
        assertIs<AppError.NotFound>(engine.stay(StayId("nowhere")).errorOrNull())
        assertIs<AppError.NotFound>(engine.book(QuoteId("q-none")).errorOrNull())
    }

    @Test
    fun `booking an issued quote keeps its total and gets a readable reference`() {
        val quote = assertNotNull(engine.quote(studio, range("2027-03-12", "2027-03-27"), Guests(2)).getOrNull())
        val booking = assertIs<AppResult.Success<*>>(engine.book(quote.id)).value
            as com.abulubad.sojourn.core.model.Booking
        assertEquals(quote.total, booking.total)
        assertTrue(Regex("SJ-[A-Z2-9]{6}").matches(booking.reference))
    }

    @Test
    fun `booked nights cannot be quoted or booked again until the booking is cancelled`() {
        val march = range("2027-03-12", "2027-03-27")
        val quote = assertNotNull(engine.quote(studio, march, Guests(2)).getOrNull())
        val booking = assertNotNull(engine.book(quote.id).getOrNull())
        assertTrue(Regex("[0-9]{6}").matches(assertNotNull(booking.keyCode)))
        // A made-up building with no street or number, so it never points at a real home.
        assertTrue(Regex("Flat [0-9]{1,2}, [A-Z][a-z]+ House").matches(assertNotNull(booking.address)))
        val wifi = assertNotNull(booking.wifi)
        assertEquals("Sojourn-${booking.reference.takeLast(4)}", wifi.network)
        assertTrue(Regex("[a-z]+-[a-z]+-[0-9]{2}").matches(wifi.password))
        val overlapping = range("2027-03-20", "2027-03-30")
        assertEquals(AppError.Validation("unavailable"), engine.quote(studio, overlapping, Guests(1)).errorOrNull())
        assertEquals(AppError.Validation("unavailable"), engine.book(quote.id).errorOrNull())
        engine.cancel(booking.id)
        assertNotNull(engine.quote(studio, overlapping, Guests(1)).getOrNull())
    }
}
