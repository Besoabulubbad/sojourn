package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.domain.DomainFixtures.engine
import com.abulubad.sojourn.core.domain.DomainFixtures.family
import com.abulubad.sojourn.core.domain.DomainFixtures.flat
import com.abulubad.sojourn.core.domain.DomainFixtures.london
import com.abulubad.sojourn.core.domain.DomainFixtures.march
import com.abulubad.sojourn.core.domain.DomainFixtures.studio
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.HostText
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.QuoteProblem
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.testing.FixedAppClock
import com.abulubad.sojourn.core.testing.data.FakeBookingRepository
import com.abulubad.sojourn.core.testing.data.FakePricingRepository
import com.abulubad.sojourn.core.testing.data.FakeSavedRepository
import com.abulubad.sojourn.core.testing.data.FakeStayRepository
import com.abulubad.sojourn.core.testing.runSojournTest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Details, quotes, bookings and saved stays. The clock reads 1 March 2026. */
class StayAndBookingUseCasesTest {

    private val clock = FixedAppClock()
    private val stays = FakeStayRepository(remote = london, cached = london)
    private val pricing = FakePricingRepository(engine())
    private val bookings = FakeBookingRepository()
    private val saved = FakeSavedRepository()

    // --- Details ---

    @Test
    fun `details show the cached stay then the refreshed one with blocked dates and a hidden rating`() =
        runSojournTest {
            stays.blocked.value = mapOf(family.id to listOf(march))
            val details = GetStayDetails(stays, saved)(family.id).first { !it.refreshing }
            val data = assertNotNull(details.data)
            assertEquals(family, data.stay)
            assertEquals(listOf(march), data.blocked)
            assertNull(data.rating, "two reviews: hidden (N-04)")
        }

    @Test
    fun `details carry the reviews only while the rating is shown`() = runSojournTest {
        val review = Review("Tom", YearMonth(2026, 9), HostText("Quiet and clean.", "en"))
        stays.reviews.value = mapOf(studio.id to listOf(review), family.id to listOf(review))
        val shown = assertNotNull(GetStayDetails(stays, saved)(studio.id).first { !it.refreshing }.data)
        assertEquals(listOf(review), shown.reviews)
        val hidden = assertNotNull(GetStayDetails(stays, saved)(family.id).first { !it.refreshing }.data)
        assertEquals(emptyList(), hidden.reviews)
    }

    @Test
    fun `a stay gone from the catalogue ends as not found`() = runSojournTest {
        stays.remote = london - flat
        val details = GetStayDetails(stays, saved)(flat.id).first { !it.refreshing }
        assertIs<AppError.NotFound>(details.error)
    }

    // --- Quotes ---

    @Test
    fun `rules the device can check fail before the network is asked`() = runSojournTest {
        val quote = GetQuote(stays, pricing, clock)
        val past = DateRange(LocalDate(2026, 2, 1), LocalDate(2026, 2, 10))
        assertEquals(AppError.Validation("dates_in_past"), quote(studio.id, past, Guests(1)).errorOrNull())
        assertEquals(AppError.Validation("too_many_guests"), quote(studio.id, march, Guests(3)).errorOrNull())
        stays.blocked.value = mapOf(studio.id to listOf(DateRange(LocalDate(2026, 3, 20), LocalDate(2026, 3, 22))))
        assertEquals(AppError.Validation("unavailable"), quote(studio.id, march, Guests(1)).errorOrNull())
        assertEquals(0, pricing.quoteCalls)
    }

    @Test
    fun `a valid request is priced and stored for every surface - P-02`() = runSojournTest {
        val quote = assertNotNull(GetQuote(stays, pricing, clock)(flat.id, march, Guests(2)).getOrNull())
        assertEquals(quote, ObserveQuote(pricing)(quote.id).first())
    }

    @Test
    fun `a stay not cached yet goes straight to the API`() = runSojournTest {
        stays.cache.value = emptyList()
        assertTrue(GetQuote(stays, pricing, clock)(flat.id, march, Guests(1)).isSuccess)
        assertEquals(1, pricing.quoteCalls)
    }

    // --- Booking ---

    @Test
    fun `booking charges the total the guest saw - P-02`() = runSojournTest {
        val quote = assertNotNull(pricing.quote(flat.id, march, Guests(1)).getOrNull())
        bookings.issued[quote.id] = quote
        val booking = assertNotNull(CreateBooking(pricing, bookings)(quote.id).getOrNull())
        assertEquals(quote.total, booking.total)
        assertEquals(quote.id, booking.quoteId)
    }

    @Test
    fun `an expired quote with the same price is booked under a fresh quote`() = runSojournTest {
        val shown = assertNotNull(pricing.quote(flat.id, march, Guests(1)).getOrNull())
        // The API forgot the shown quote; pricing the same request again gives a new id and the same total.
        pricing.quoteFor = { _, dates, guests ->
            val fresh = DomainFixtures.quote(flat, dates, guests).copy(id = QuoteId("q-fresh"))
            bookings.issued[fresh.id] = fresh
            AppResult.Success(fresh)
        }
        val booking = assertNotNull(CreateBooking(pricing, bookings)(shown.id).getOrNull())
        assertEquals(shown.total, booking.total)
        assertEquals(QuoteId("q-fresh"), booking.quoteId)
    }

    @Test
    fun `an expired quote whose price changed is not booked`() = runSojournTest {
        val shown = assertNotNull(pricing.quote(flat.id, march, Guests(1)).getOrNull())
        pricing.quoteFor = { _, dates, guests ->
            AppResult.Success(DomainFixtures.quote(flat, dates, guests, nightly = Money.of(130, Currency.GBP)))
        }
        val result = CreateBooking(pricing, bookings)(shown.id)
        assertEquals(AppError.Validation("price_changed"), result.errorOrNull())
        assertTrue(bookings.bookings.value.isEmpty())
    }

    @Test
    fun `bookings follow the calendar with active first then upcoming then past`() = runSojournTest {
        val past = DateRange(LocalDate(2026, 1, 5), LocalDate(2026, 1, 12))
        val now = DateRange(LocalDate(2026, 2, 25), LocalDate(2026, 3, 5))
        val later = DateRange(LocalDate(2026, 6, 1), LocalDate(2026, 6, 8))
        listOf(past to studio, now to flat, later to family, march to studio).forEach { (dates, stay) ->
            val quote = DomainFixtures.quote(stay, dates, Guests(1))
            bookings.issued[quote.id] = quote
            bookings.book(quote.id)
        }
        val items = ListBookings(bookings, stays, clock)().first()
        assertEquals(listOf(now, march, later, past), items.map { it.booking.dates })
        assertEquals(
            listOf(BookingStatus.Active, BookingStatus.Upcoming, BookingStatus.Upcoming, BookingStatus.Completed),
            items.map { it.booking.status },
        )
        assertEquals(flat, items.first().stay)
    }

    @Test
    fun `a booking comes with its stay and is null until the device has it`() = runSojournTest {
        val quote = DomainFixtures.quote(flat, march, Guests(1))
        bookings.issued[quote.id] = quote
        val booking = assertNotNull(bookings.book(quote.id).getOrNull())
        val item = assertNotNull(GetBooking(bookings, stays)(booking.id).first())
        assertEquals(booking, item.booking)
        assertEquals(flat, item.stay)
        assertNull(GetBooking(bookings, stays)(BookingId("b-unknown")).first())
    }

    @Test
    fun `a failed quote is said with the stay's own numbers`() {
        val minimum = StayRules.problemFor(AppError.Validation(StayRules.Problem.MINIMUM_NIGHTS), studio)
        assertEquals(QuoteProblem.MinimumNights(StayRules.minimumNights(studio)), minimum)
        val guests = StayRules.problemFor(AppError.Validation(StayRules.Problem.TOO_MANY_GUESTS), studio)
        assertEquals(QuoteProblem.TooManyGuests(studio.facts.sleeps), guests)
        assertEquals(QuoteProblem.Failed(offline = true), StayRules.problemFor(AppError.Offline, studio))
        assertEquals(QuoteProblem.Failed(offline = false), StayRules.problemFor(AppError.Remote(500), null))
    }

    // --- Saved ---

    @Test
    fun `saved stays come newest first with monthly from prices and gone stays left out`() = runSojournTest {
        val toggle = ToggleSaved(saved)
        assertTrue(toggle(studio.id))
        assertTrue(toggle(StayId("gone")))
        assertTrue(toggle(flat.id))
        val cards = ObserveSaved(saved, stays)().first()
        assertEquals(listOf(flat.id, studio.id), cards.map { it.stay.id })
        assertTrue(cards.all { it.saved && (it.price as? PriceDisplay.From)?.per == PriceBasis.Month })
        assertFalse(toggle(flat.id))
        assertNotEquals(cards, ObserveSaved(saved, stays)().first())
    }
}
