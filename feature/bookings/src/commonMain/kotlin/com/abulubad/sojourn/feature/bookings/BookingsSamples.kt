package com.abulubad.sojourn.feature.bookings

import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.designsystem.preview.SojournSamplePhotos
import com.abulubad.sojourn.core.designsystem.preview.WithSojournSamplePhotos
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.CheckInMethod
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
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

/** The Bookings tab and a booking's page in each state the ScreenBookings and ScreenStates cards show. */
@Suppress("MagicNumber") // Sample data: the card's stay, dates and prices.
object BookingsSamples {

    /** Twelve days before the card's March stay. */
    private val today = LocalDate(2027, 2, 28)
    private val arrival = LocalDate(2027, 3, 12)
    private fun pounds(major: Long) = Money.of(major, Currency.GBP)

    private fun stay(id: String, area: String, title: String, photo: String) = Stay(
        id = StayId(id),
        cityId = CityId("london"),
        areaKey = area,
        title = HostText(title, "en"),
        description = HostText(title, "en"),
        facts = StayFacts(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2),
        photos = listOf(Photo(PhotoId("$id-1"), photo)),
        amenities = setOf(Amenity.Wifi, Amenity.Workspace),
        policies = listOf(
            Policy.FreeCancellation(LocalDate(2027, 2, 26)),
            Policy.Deposit(pounds(250)),
            Policy.CheckIn(LocalTime(15, 0), CheckInMethod.SelfCheckIn),
            Policy.CheckOut(LocalTime(11, 0)),
        ),
        rating = Rating(4.8, 32),
        location = GeoArea(GeoPoint(51.5180, -0.1800), radiusMetres = 350),
        fromPrice = PriceDisplay.From(pounds(2_484), PriceBasis.Month, listOf(PriceExclusion.CleaningFee)),
    )

    private val canal = stay("canal", "area_paddington", "Bright studio by the canal", SojournSamplePhotos.LIVING)
    private val loft = stay("loft", "area_shoreditch", "Loft with a desk and a view", SojournSamplePhotos.APARTMENT)
    private val flat = stay(
        "flat",
        "area_south_bank",
        "Riverside two-bed for longer stays",
        SojournSamplePhotos.KITCHEN,
    )

    private fun booking(id: String, stay: Stay, dates: DateRange, total: Long, status: BookingStatus) = BookingItem(
        Booking(
            id = BookingId(id),
            reference = "SJ-" + id.uppercase().padEnd(6, 'X').take(6),
            stayId = stay.id,
            quoteId = QuoteId("q-$id"),
            dates = dates,
            guests = Guests(adults = 2),
            total = pounds(total),
            status = status,
            createdAt = Instant.parse("2027-01-20T10:00:00Z"),
            keyCode = "482913",
        ),
        stay,
    )

    /** The card's booking: 12–27 March, 15 nights, £1,383. */
    private val march = booking(
        "4f7k2q",
        canal,
        DateRange(arrival, LocalDate(2027, 3, 27)),
        1_383,
        BookingStatus.Upcoming,
    )
    private val may = booking(
        "9hm3ra",
        loft,
        DateRange(LocalDate(2027, 5, 2), LocalDate(2027, 5, 30)),
        2_940,
        BookingStatus.Upcoming,
    )
    private val january = booking(
        "x7pd2c",
        flat,
        DateRange(LocalDate(2027, 1, 4), LocalDate(2027, 1, 18)),
        2_310,
        BookingStatus.Completed,
    )
    private val cancelled = booking(
        "k2vb8n",
        loft,
        DateRange(LocalDate(2027, 2, 6), LocalDate(2027, 2, 9)),
        640,
        BookingStatus.Cancelled,
    )

    private val all = ScreenState.Content(listOf(march, may, january, cancelled))

    val upcoming = BookingsUiState(today, BookingsTab.Upcoming, all)
    val past = upcoming.copy(tab = BookingsTab.Past)
    val empty = BookingsUiState(today, BookingsTab.Upcoming, ScreenState.Empty)

    /** The guide before arrival: the key code comes on the day. */
    val guideBefore = upcoming.copy(guide = march)

    /** Arrival day: the key code, copyable. */
    val guideOnArrival = BookingsUiState(
        today = arrival,
        bookings = ScreenState.Content(listOf(march.copy(booking = march.booking.copy(status = BookingStatus.Active)))),
        guide = march.copy(booking = march.booking.copy(status = BookingStatus.Active)),
    )

    val details = BookingDetailsUiState(today, ScreenState.Content(march))

    /** After the free-cancellation deadline (26 Feb): the first 7 nights are kept. */
    val cancelling = details.copy(sheet = BookingSheet.Cancel())
    val cancelledDetails = BookingDetailsUiState(today, ScreenState.Content(cancelled))

    @Composable
    fun Tab(state: BookingsUiState) = WithSojournSamplePhotos { BookingsScreen(state, onIntent = {}) }

    @Composable
    fun Details(state: BookingDetailsUiState) = WithSojournSamplePhotos { BookingDetailsScreen(state, onIntent = {}) }
}
