package com.abulubad.sojourn.core.model

/**
 * Text written by a host (titles, descriptions) in its own language. Rendered only through `HostText`, in its own
 * direction, never forced to the UI direction or silently translated (S-06). [languageTag] is BCP 47.
 */
data class HostText(val text: String, val languageTag: String) {
    init {
        require(languageTag.isNotBlank()) { "Host text declares its language" }
    }
}

/** A photo slot. Every slot renders placeholder, loaded and failed-with-retry states (brand book). */
data class Photo(val id: PhotoId, val url: String, val caption: HostText? = null)

/** The four facts on a card and the key-facts row. Zero bedrooms is a studio and is never shown as "0" (N-01). */
data class StayFacts(val bedrooms: Int, val beds: Int, val bathrooms: Int, val sleeps: Int) {
    init {
        require(bedrooms >= 0 && beds >= 1 && bathrooms >= 1 && sleeps >= 1) { "Facts are counts of real things" }
    }

    val isStudio: Boolean get() = bedrooms == 0
}

/** Amenities, localized by id (S-05). One icon per meaning (N-02) is resolved in the design system. */
enum class Amenity(val id: String) {
    Wifi("wifi"),
    Washer("washer"),
    Workspace("workspace"),
    Lift("lift"),
    Kitchen("kitchen"),
    AirConditioning("air_conditioning"),
    Heating("heating"),
    Parking("parking"),
    Balcony("balcony"),
    Dishwasher("dishwasher"),
    Tv("tv"),
    PetFriendly("pet_friendly"),
}

/** Guest rating. Components hide it under three reviews (N-04); the type keeps the data honest. */
data class Rating(val score: Double, val count: Int) {
    init {
        require(score in 0.0..MAX_SCORE) { "Score is on a five-point scale" }
        require(count >= 0) { "Count cannot be negative" }
    }

    companion object {
        const val MAX_SCORE = 5.0
    }
}

/** A point on the map. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/** Before booking, a stay's position is shown as an area, not a pin (brand book). */
data class GeoArea(val center: GeoPoint, val radiusMetres: Int) {
    init {
        require(radiusMetres > 0) { "An area has a positive radius" }
    }
}

/**
 * A listing. Names of cities, areas and amenities are keys, not strings: the app owns the words (S-05).
 * [fromPrice] is the no-dates price and must state its exclusions (P-03); dated totals come from a [Quote].
 */
data class Stay(
    val id: StayId,
    val cityId: CityId,
    val areaKey: String,
    val title: HostText,
    val description: HostText,
    val facts: StayFacts,
    val photos: List<Photo>,
    val amenities: Set<Amenity>,
    val policies: List<Policy>,
    val rating: Rating?,
    val location: GeoArea,
    val fromPrice: PriceDisplay.From,
    val walkMinutesToTransit: Int? = null,
    val longStayDiscountPercent: Int? = null,
) {
    init {
        require(photos.isNotEmpty()) { "A stay has at least one photo slot" }
        longStayDiscountPercent?.let { require(it in 1..MAX_DISCOUNT_PERCENT) { "Discount is a percentage" } }
    }

    private companion object {
        const val MAX_DISCOUNT_PERCENT = 90
    }
}

/** A city in the catalogue. Its name is localized by [id] (S-05); [photo] heads its card on Explore. */
data class City(
    val id: CityId,
    val countryCode: String,
    val location: GeoPoint,
    val stayCount: Int,
    val fromPrice: PriceDisplay.From,
    val photo: Photo,
)
