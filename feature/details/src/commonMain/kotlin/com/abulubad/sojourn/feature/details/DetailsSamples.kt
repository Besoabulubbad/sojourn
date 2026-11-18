package com.abulubad.sojourn.feature.details

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.preview.SojournSamplePhotos
import com.abulubad.sojourn.core.designsystem.preview.WithSojournSamplePhotos
import com.abulubad.sojourn.core.domain.StayDetails
import com.abulubad.sojourn.core.model.Amenity
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
import com.abulubad.sojourn.core.model.PriceInclusion
import com.abulubad.sojourn.core.model.PriceLine
import com.abulubad.sojourn.core.model.PriceLineKind
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.QuoteProblem
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.ui.ScreenState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth

/** Details in each state the card and ScreenStates show, for previews, screenshots and the showcase. */
object DetailsSamples {

    private val today = LocalDate(2026, 2, 20)
    private val march = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))
    private fun pounds(major: Long) = Money.of(major, Currency.GBP)

    /** The card's stay: a canal studio in Paddington, written in English, with an Arabic translation. */
    val stay = Stay(
        id = StayId("canal-studio"),
        cityId = CityId("london"),
        areaKey = "area_paddington",
        title = HostText("Bright studio by the canal", "en"),
        description = HostText(
            "A calm, light-filled studio two minutes from the canal. There is a proper desk by the window, a full " +
                "kitchen and a washer, so it works for a month as well as a weekend.",
            "en",
        ),
        facts = StayFacts(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2),
        photos = listOf(
            SojournSamplePhotos.LIVING,
            SojournSamplePhotos.KITCHEN,
            SojournSamplePhotos.BEDROOM,
            SojournSamplePhotos.APARTMENT,
        ).mapIndexed { index, url -> Photo(PhotoId("canal-$index"), url) },
        amenities = setOf(
            Amenity.Wifi,
            Amenity.Workspace,
            Amenity.Washer,
            Amenity.Kitchen,
            Amenity.Heating,
            Amenity.Lift,
        ),
        policies = listOf(
            Policy.FreeCancellation(LocalDate(2026, 2, 26)),
            Policy.Deposit(pounds(250)),
            Policy.CheckIn(LocalTime(15, 0), CheckInMethod.SelfCheckIn),
            Policy.MinimumStay(2),
        ),
        rating = Rating(4.8, 32),
        location = GeoArea(GeoPoint(51.5190, -0.1750), radiusMetres = 350),
        fromPrice = PriceDisplay.From(pounds(92), PriceBasis.Night, listOf(PriceExclusion.CleaningFee)),
        walkMinutesToTransit = 8,
        longStayDiscountPercent = 10,
        inclusions = setOf(PriceInclusion.Wifi, PriceInclusion.Utilities, PriceInclusion.WeeklyCleaning),
        descriptionTranslations = listOf(
            HostText(
                "استوديو هادئ مليء بالضوء على بعد دقيقتين من القناة. فيه مكتب حقيقي بجانب النافذة ومطبخ كامل " +
                    "وغسالة، فيناسب إقامة شهر كما يناسب عطلة نهاية أسبوع.",
                "ar",
            ),
        ),
    )

    /** 15 nights in March: £92 × 15 + £3 cleaning = £1,383, the brand book's total and the card's. */
    val quote = Quote(
        id = QuoteId("q-canal-march"),
        stayId = stay.id,
        dates = march,
        guests = Guests(adults = 2),
        breakdown = PriceBreakdown.of(
            listOf(
                PriceLine(PriceLineKind.Nights, pounds(1_380), quantity = 15, unitAmount = pounds(92)),
                PriceLine(PriceLineKind.CleaningFee, pounds(3)),
            ),
        ),
    )

    private val reviews = listOf(
        Review(
            "Hannah",
            YearMonth(2026, 1),
            HostText("Spotless and quiet. The desk by the window made a month of work easy.", "en"),
        ),
        Review("Léa", YearMonth(2025, 12), HostText("Très propre, très calme, et la literie est excellente.", "fr")),
        Review("سارة", YearMonth(2025, 11), HostText("شقة نظيفة وهادئة، والمضيف متعاون جدًا.", "ar")),
    )

    private val details = StayDetails(stay, stay.rating, blocked = emptyList(), saved = true, reviews = reviews)
    private val dated = SearchQuery(cityId = stay.cityId, dates = march, guests = Guests(adults = 2))

    /** Came from a dated search: "£1,383 total" and "Reserve". */
    val withDates = DetailsUiState(dated, today, ScreenState.Content(details), quote = quote)

    /** No dates: "from … a month" and "Choose dates". */
    val withoutDates = DetailsUiState(SearchQuery(cityId = stay.cityId), today, ScreenState.Content(details))

    /** The description in the reader's language (shown in Arabic). */
    val translated = withoutDates.copy(translated = true)

    val loading = DetailsUiState(dated, today)
    val gone = DetailsUiState(dated, today, ScreenState.Error(AppError.NotFound(stay.id.value)))
    val offline = withDates.copy(details = ScreenState.Offline(details))

    /** "Choose dates" with a one-night selection the minimum stay refuses. */
    val choosingDates = withoutDates.copy(
        sheet = DetailsSheet.Dates(
            month = YearMonth(2026, 3),
            selection = DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 13)),
            problem = QuoteProblem.MinimumNights(2),
        ),
    )

    /** "Price details" for the March quote. */
    val priceDetails = withDates.copy(sheet = DetailsSheet.PriceDetails)

    /** The full-screen gallery on the second photo. */
    val gallery = GalleryUiState(index = 1, photos = ScreenState.Content(stay.photos))

    /** The screen scrolled to [fraction] of its length (0 the top, 1 the end), as a guest reading on would see it. */
    @Composable
    fun Screen(state: DetailsUiState, fraction: Float = 0f) = WithSojournSamplePhotos {
        val scroll = rememberScrollState()
        LaunchedEffect(scroll.maxValue) { scroll.scrollTo((scroll.maxValue * fraction).toInt()) }
        DetailsScreen(state, onIntent = {}, scroll = scroll)
    }

    @Composable
    fun Gallery(state: GalleryUiState = gallery) = WithSojournSamplePhotos { GalleryScreen(state, onIntent = {}) }
}
