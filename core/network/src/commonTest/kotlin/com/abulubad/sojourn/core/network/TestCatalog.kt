package com.abulubad.sojourn.core.network

import com.abulubad.sojourn.core.network.dto.AreaDto
import com.abulubad.sojourn.core.network.dto.CatalogDto
import com.abulubad.sojourn.core.network.dto.CityDto
import com.abulubad.sojourn.core.network.dto.DateRangeDto
import com.abulubad.sojourn.core.network.dto.FactsDto
import com.abulubad.sojourn.core.network.dto.HostTextDto
import com.abulubad.sojourn.core.network.dto.PoliciesDto
import com.abulubad.sojourn.core.network.dto.PricingDto
import com.abulubad.sojourn.core.network.dto.RatingDto
import com.abulubad.sojourn.core.network.dto.ReviewDto
import com.abulubad.sojourn.core.network.dto.StayDto

/** A two-stay catalogue with numbers chosen to make the pricing easy to check by hand. */
internal object TestCatalog {

    val studio = StayDto(
        id = "london-01",
        cityId = "london",
        areaKey = "area_paddington",
        title = HostTextDto("Bright studio by the canal", "en"),
        description = HostTextDto("A calm studio.", "en"),
        facts = FactsDto(bedrooms = 0, beds = 1, bathrooms = 1, sleeps = 2),
        photos = listOf("a.jpg", "b.jpg"),
        amenities = listOf("wifi", "washer", "teleporter"),
        policies = PoliciesDto(
            freeCancellationDaysBefore = 14,
            deposit = 25_000,
            checkInFrom = "15:00",
            checkInMethod = "self_check_in",
            checkOutBy = "11:00",
            minimumNights = 2,
        ),
        rating = RatingDto(4.8, 32),
        area = AreaDto(51.5154, -0.1755, 350),
        pricing = PricingDto(currency = "GBP", nightly = 9_800, cleaningFee = 6_000, longStayDiscountPercent = 10),
        walkMinutesToTransit = 8,
        instantBook = true,
        blocked = listOf(DateRangeDto("2027-01-10", "2027-01-20")),
        descriptionTranslations = listOf(HostTextDto("استوديو هادئ.", "ar")),
        inclusions = listOf("wifi", "utilities", "jacuzzi"),
        reviews = listOf(
            ReviewDto("Tom", "2026-07", HostTextDto("Quiet and clean.", "en")),
            ReviewDto("Léa", "2026-09", HostTextDto("Très calme.", "fr")),
            ReviewDto("Ghost", "last summer", HostTextDto("Unreadable month.", "en")),
        ),
    )

    val flat = studio.copy(
        id = "paris-01",
        cityId = "paris",
        areaKey = "area_le_marais",
        facts = FactsDto(bedrooms = 1, beds = 1, bathrooms = 1, sleeps = 3),
        pricing = PricingDto(currency = "EUR", nightly = 11_000, cleaningFee = 5_000, cityTaxPerNight = 325),
        rating = null,
        blocked = emptyList(),
    )

    val catalog = CatalogDto(
        version = 1,
        photoBase = "catalog/photos/",
        cities = listOf(
            CityDto("london", "GB", "GBP", 51.5074, -0.1278, "london.jpg"),
            CityDto("paris", "FR", "EUR", 48.8566, 2.3522, "paris.jpg"),
        ),
        stays = listOf(studio, flat),
    )
}
