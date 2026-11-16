package com.abulubad.sojourn.core.designsystem.components.internal

import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.Currency
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
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** The listings the Listing samples show: the cards' "Bright studio by the canal" and an Arabic-titled flat. */
@Suppress("MagicNumber") // Sample data: the numbers are the cards' own.
internal object SampleListings {

    private fun photo(id: String, url: String) = Photo(PhotoId(id), url)

    val canalPhotos = listOf(
        photo("canal-1", SamplePhotos.LIVING),
        photo("canal-2", SamplePhotos.LOFT_BEDROOM),
        photo("canal-3", SamplePhotos.KITCHEN),
        photo("canal-4", SamplePhotos.BREAKFAST_TABLE),
        photo("canal-5", SamplePhotos.WINDOW_NOOK),
    )

    val hydraPhotos = listOf(
        photo("hydra-1", SamplePhotos.APARTMENT),
        photo("hydra-2", SamplePhotos.BEDROOM),
        photo("hydra-3", SamplePhotos.DINING_VIEW),
        photo("hydra-4", SamplePhotos.DINING_ROOM),
        photo("hydra-5", SamplePhotos.READING_CORNER),
    )

    val canalPolicies = listOf(
        Policy.FreeCancellation(LocalDate(2027, 2, 26)),
        Policy.Deposit(Money.of(250, Currency.GBP)),
        Policy.CheckIn(LocalTime(15, 0), CheckInMethod.SelfCheckIn),
    )

    val canalStudio = Stay(
        id = StayId("sample-canal-studio"),
        cityId = CityId("london"),
        areaKey = "area_paddington",
        title = HostText("Bright studio by the canal", "en"),
        description = HostText("A calm, light-filled studio two minutes from the canal.", "en"),
        facts = StayFacts(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2),
        photos = canalPhotos,
        amenities = setOf(Amenity.Wifi, Amenity.Washer, Amenity.Workspace),
        policies = canalPolicies,
        rating = Rating(4.8, 32),
        location = GeoArea(GeoPoint(51.5190, -0.1750), radiusMetres = 350),
        fromPrice = PriceDisplay.From(Money.of(92, Currency.GBP), PriceBasis.Night, listOf(PriceExclusion.CleaningFee)),
        longStayDiscountPercent = 10,
    )

    /** The card's dated total: £1,383 for 15 nights. */
    val canalTotal = PriceDisplay.Total(QuoteId("sample-quote"), Money.of(1383, Currency.GBP), nights = 15)

    val hydraFlat = canalStudio.copy(
        id = StayId("sample-hydra-flat"),
        cityId = CityId("algiers"),
        areaKey = "area_hydra",
        title = HostText("شقة مشرقة في حيدرة", "ar"),
        description = HostText("شقة هادئة مليئة بالضوء على بعد دقائق من الحديقة.", "ar"),
        facts = StayFacts(bedrooms = 2, beds = 3, bathrooms = 2, sleeps = 5),
        photos = hydraPhotos,
        rating = Rating(4.6, 12),
        fromPrice = PriceDisplay.From(Money.of(9000, Currency.DZD), PriceBasis.Night, listOf(PriceExclusion.Utilities)),
        longStayDiscountPercent = null,
    )

    val london = City(
        id = CityId("london"),
        countryCode = "GB",
        location = GeoPoint(51.5074, -0.1278),
        stayCount = 214,
        fromPrice = PriceDisplay.From(Money.of(1950, Currency.GBP), PriceBasis.Month, listOf(PriceExclusion.Utilities)),
        photo = photo("city-london", SamplePhotos.LONDON),
    )

    val paris = City(
        id = CityId("paris"),
        countryCode = "FR",
        location = GeoPoint(48.8566, 2.3522),
        stayCount = 96,
        fromPrice = PriceDisplay.From(Money.of(1720, Currency.GBP), PriceBasis.Month, listOf(PriceExclusion.Utilities)),
        photo = photo("city-paris", SamplePhotos.PARIS),
    )
}
