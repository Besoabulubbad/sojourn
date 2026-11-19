package com.abulubad.sojourn.feature.checkout

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.preview.SojournSamplePhotos
import com.abulubad.sojourn.core.designsystem.preview.WithSojournSamplePhotos
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.StayDetails
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
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlin.time.Instant

/** Checkout and the confirmation in each state, for previews, screenshots and the showcase. */
@Suppress("MagicNumber") // Sample data: the numbers are the card's own.
object CheckoutSamples {

    private val today = LocalDate(2027, 2, 20)
    private val march = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 3, 27))
    private fun pounds(major: Long) = Money.of(major, Currency.GBP)

    /** The card's stay: a canal studio in Paddington for two, free to cancel until 26 February. */
    val stay = Stay(
        id = StayId("canal-studio"),
        cityId = CityId("london"),
        areaKey = "area_paddington",
        title = HostText("Bright studio by the canal", "en"),
        description = HostText("A calm, light-filled studio two minutes from the canal.", "en"),
        facts = StayFacts(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2),
        photos = listOf(SojournSamplePhotos.LIVING, SojournSamplePhotos.KITCHEN)
            .mapIndexed { index, url -> Photo(PhotoId("canal-$index"), url) },
        amenities = setOf(Amenity.Wifi, Amenity.Workspace, Amenity.Washer, Amenity.Kitchen),
        policies = listOf(
            Policy.FreeCancellation(LocalDate(2027, 2, 26)),
            Policy.Deposit(pounds(250)),
            Policy.CheckIn(LocalTime(15, 0), CheckInMethod.SelfCheckIn),
            Policy.MinimumStay(2),
        ),
        rating = Rating(4.8, 32),
        location = GeoArea(GeoPoint(51.5190, -0.1750), radiusMetres = 350),
        fromPrice = PriceDisplay.From(pounds(98), PriceBasis.Night, listOf(PriceExclusion.CleaningFee)),
        longStayDiscountPercent = 10,
    )

    /** The card's lines: £98 × nights, the 10% long-stay discount, £60 cleaning. */
    private fun quote(id: String, dates: DateRange, nightly: Long, guests: Guests = Guests(adults = 2)): Quote {
        val nights = pounds(nightly * dates.nights)
        return Quote(
            id = QuoteId(id),
            stayId = stay.id,
            dates = dates,
            guests = guests,
            breakdown = PriceBreakdown.of(
                listOf(
                    PriceLine(PriceLineKind.Nights, nights, quantity = dates.nights, unitAmount = pounds(nightly)),
                    PriceLine(PriceLineKind.LongStayDiscount, pounds(-(nightly * dates.nights) / 10)),
                    PriceLine(PriceLineKind.CleaningFee, pounds(60)),
                ),
            ),
        )
    }

    /** 12–27 March, 15 nights: £1,470 − £147 + £60 = £1,383, the card's total. */
    val quote = quote("q-canal-march", march, nightly = 98)

    private val details = StayDetails(stay, stay.rating, blocked = emptyList(), saved = false)

    /** Ready to pay "£1,383". */
    val ready = CheckoutUiState(today, quote, ScreenState.Content(details))

    /** Five more nights chosen here: the new total, and what it was. */
    val repriced = ready.copy(
        quote = quote("q-canal-april", DateRange(march.start, LocalDate(2027, 4, 1)), nightly = 98),
        previousTotal = quote.total,
    )

    /** The payment sheet is working: "Pay £1,383" holds its place. */
    val paying = ready.copy(paying = true)

    /** "Pay" tapped: the demo wallet over the page, asking again for the same total. */
    val wallet = ready.copy(wallet = true)

    /** The payment was declined: the reason above the button, the page as it was. */
    val paymentFailed = ready.copy(notice = CheckoutNotice.PaymentFailed)

    /** The total changed while paying (the quote had expired): the new breakdown, what it was, and pay again. */
    val priceChanged = quote("q-canal-march-2", march, nightly = 102).let { fresh ->
        ready.copy(quote = fresh, previousTotal = quote.total, notice = CheckoutNotice.PriceChanged(fresh.total))
    }

    val loading = CheckoutUiState(today)
    val gone = CheckoutUiState(today, details = ScreenState.Error(AppError.NotFound(quote.id.value)))
    val offline = ready.copy(details = ScreenState.Offline(details))

    /** Editing dates in place, from the quote's own. */
    val editingDates = ready.copy(sheet = CheckoutSheet.EditDates(YearMonth(2027, 3), selection = march))

    /** Editing guests: the studio sleeps two, so with two adults the children stepper cannot go up. */
    val editingGuests = ready.copy(sheet = CheckoutSheet.EditGuests(Guests(adults = 2)))

    /** "You're booked", with the reference the booking was given. */
    val confirmed = ConfirmationUiState(
        ScreenState.Content(
            BookingItem(
                Booking(
                    id = BookingId("b-1-canal-studio"),
                    reference = "SJ-4F7K2Q",
                    stayId = stay.id,
                    quoteId = quote.id,
                    dates = march,
                    guests = quote.guests,
                    total = quote.total,
                    status = BookingStatus.Upcoming,
                    createdAt = Instant.parse("2027-02-20T10:00:00Z"),
                ),
                stay,
            ),
        ),
    )

    /** The screen scrolled to [fraction] of its length (0 the top, 1 the end). */
    @Composable
    fun Screen(state: CheckoutUiState, fraction: Float = 0f) = WithSojournSamplePhotos {
        val scroll = rememberScrollState()
        LaunchedEffect(scroll.maxValue) { scroll.scrollTo((scroll.maxValue * fraction).toInt()) }
        CheckoutScreen(state, onIntent = {}, scroll = scroll)
    }

    @Composable
    fun Confirmation(state: ConfirmationUiState = confirmed) = WithSojournSamplePhotos {
        ConfirmationScreen(state, onIntent = {})
    }
}
