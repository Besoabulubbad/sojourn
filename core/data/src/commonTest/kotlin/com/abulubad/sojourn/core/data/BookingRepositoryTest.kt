package com.abulubad.sojourn.core.data

import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.errorOrNull
import com.abulubad.sojourn.core.common.getOrNull
import com.abulubad.sojourn.core.data.DataTestEnv.Companion.quarter
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.network.local.FakeNetworkConditions.Mode
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BookingRepositoryTest {

    @Test
    fun `booking a quote stores a booking with the quote total - P-02`() = runDataTest {
        val quote = assertNotNull(pricing.quote(catalogueStays().first().id, quarter(), Guests(2)).getOrNull())
        val booking = assertNotNull(bookings.book(quote.id).getOrNull())
        assertEquals(quote.total, booking.total)
        assertEquals(quote.id, booking.quoteId)
        assertEquals(listOf(booking), bookings.observeBookings().first())
        assertEquals(booking, bookings.observeBooking(booking.id).first())
    }

    @Test
    fun `bookings are listed latest check-in first`() = runDataTest {
        val stay = catalogueStays().first().id
        val early = assertNotNull(pricing.quote(stay, quarter(), Guests(1)).getOrNull())
        val later = assertNotNull(pricing.quote(stay, quarter(quarter().end), Guests(1)).getOrNull())
        bookings.book(early.id)
        bookings.book(later.id)
        assertEquals(listOf(later.id, early.id), bookings.observeBookings().first().map { it.quoteId })
    }

    @Test
    fun `a quote the API does not know is not booked`() = runDataTest {
        assertIs<AppError.NotFound>(bookings.book(QuoteId("expired")).errorOrNull())
        assertTrue(bookings.observeBookings().first().isEmpty())
    }

    @Test
    fun `booking offline fails and stores nothing but earlier bookings stay readable`() = runDataTest {
        val stay = catalogueStays().first().id
        val first = assertNotNull(pricing.quote(stay, quarter(), Guests(1)).getOrNull())
        bookings.book(first.id)
        val second = assertNotNull(pricing.quote(stay, quarter(quarter().end), Guests(1)).getOrNull())
        conditions.mode = Mode.Offline
        assertEquals(AppError.Offline, bookings.book(second.id).errorOrNull())
        assertEquals(listOf(first.id), bookings.observeBookings().first().map { it.quoteId })
    }
}
