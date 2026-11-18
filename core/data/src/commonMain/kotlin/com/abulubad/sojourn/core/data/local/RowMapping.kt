package com.abulubad.sojourn.core.data.local

import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingId
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.City
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
import com.abulubad.sojourn.core.model.Rating
import com.abulubad.sojourn.core.model.Review
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.Stay
import com.abulubad.sojourn.core.model.StayFacts
import com.abulubad.sojourn.core.model.StayId
import com.abulubad.sojourn.core.model.StayLength
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.parseOrNull
import kotlin.time.Instant
import com.abulubad.sojourn.core.database.Blocked_range as BlockedRow
import com.abulubad.sojourn.core.database.Booking as BookingRow
import com.abulubad.sojourn.core.database.City as CityRow
import com.abulubad.sojourn.core.database.Description_translation as TranslationRow
import com.abulubad.sojourn.core.database.Photo as PhotoRow
import com.abulubad.sojourn.core.database.Policy as PolicyRow
import com.abulubad.sojourn.core.database.Quote as QuoteRow
import com.abulubad.sojourn.core.database.Quote_line as QuoteLineRow
import com.abulubad.sojourn.core.database.Review as ReviewRow
import com.abulubad.sojourn.core.database.Search_recent as SearchRow
import com.abulubad.sojourn.core.database.Stay as StayRow
import com.abulubad.sojourn.core.database.Stay_amenity as AmenityRow
import com.abulubad.sojourn.core.database.Stay_inclusion as InclusionRow

/*
 * Model ↔ row conversion, in one place. Enums are stored by their stable ids (never ordinals or Kotlin names), dates
 * as ISO-8601 text, money as minor units plus a currency code, booleans as 0/1. A row the model would reject (an id
 * this build does not know) is dropped rather than crashing a screen; the next refresh rewrites it.
 */

private const val SEPARATOR = ","

// --- City ---

internal fun City.toRow(): CityRow = CityRow(
    id = id.value,
    country_code = countryCode,
    latitude = location.latitude,
    longitude = location.longitude,
    stay_count = stayCount.toLong(),
    from_minor = fromPrice.amount.minor,
    from_currency = fromPrice.amount.currency.code,
    from_basis = fromPrice.per.id,
    from_excludes = fromPrice.excludes.joinToString(SEPARATOR) { it.id },
    photo_id = photo.id.value,
    photo_url = photo.url,
)

internal fun CityRow.toModel(): City? = City(
    id = CityId(id),
    countryCode = country_code,
    location = GeoPoint(latitude, longitude),
    stayCount = stay_count.toInt(),
    fromPrice = fromPrice(from_minor, from_currency, from_basis, from_excludes) ?: return null,
    photo = Photo(PhotoId(photo_id), photo_url),
)

// --- Stay ---

internal fun Stay.toRow(): StayRow = StayRow(
    id = id.value,
    city_id = cityId.value,
    area_key = areaKey,
    title_text = title.text,
    title_lang = title.languageTag,
    description_text = description.text,
    description_lang = description.languageTag,
    bedrooms = facts.bedrooms.toLong(),
    beds = facts.beds.toLong(),
    bathrooms = facts.bathrooms.toLong(),
    sleeps = facts.sleeps.toLong(),
    rating_score = rating?.score,
    rating_count = rating?.count?.toLong(),
    latitude = location.center.latitude,
    longitude = location.center.longitude,
    radius_metres = location.radiusMetres.toLong(),
    from_minor = fromPrice.amount.minor,
    from_currency = fromPrice.amount.currency.code,
    from_basis = fromPrice.per.id,
    from_excludes = fromPrice.excludes.joinToString(SEPARATOR) { it.id },
    walk_minutes = walkMinutesToTransit?.toLong(),
    long_stay_discount_percent = longStayDiscountPercent?.toLong(),
    instant_book = if (instantBook) 1 else 0,
)

internal fun Stay.photoRows(): List<PhotoRow> = photos.mapIndexed { position, photo ->
    PhotoRow(
        stay_id = id.value,
        position = position.toLong(),
        id = photo.id.value,
        url = photo.url,
        caption_text = photo.caption?.text,
        caption_lang = photo.caption?.languageTag,
    )
}

internal fun Stay.amenityRows(): List<AmenityRow> = amenities.map { AmenityRow(stay_id = id.value, amenity_id = it.id) }

internal fun Stay.inclusionRows(): List<InclusionRow> =
    inclusions.map { InclusionRow(stay_id = id.value, inclusion_id = it.id) }

internal fun Stay.translationRows(): List<TranslationRow> =
    descriptionTranslations.map { TranslationRow(stay_id = id.value, lang = it.languageTag, text = it.text) }

internal fun Review.toRow(stayId: StayId, position: Int) = ReviewRow(
    stay_id = stayId.value,
    position = position.toLong(),
    author = author,
    month = month.toString(),
    text = text.text,
    lang = text.languageTag,
)

/** A review row back to the model; null when its month cannot be read. */
internal fun ReviewRow.toModel(): Review? =
    YearMonth.parseOrNull(month)?.let { Review(author, it, HostText(text, lang)) }

internal fun Stay.policyRows(): List<PolicyRow> = policies.mapIndexed { position, policy ->
    policy.toRow(id.value, position.toLong())
}

/** Builds a stay from its row and children; null when they cannot make a valid [Stay] (no photos, unknown price). */
internal fun StayRow.toModel(
    photos: List<PhotoRow>,
    amenities: List<AmenityRow>,
    policies: List<PolicyRow>,
    inclusions: List<InclusionRow> = emptyList(),
    translations: List<TranslationRow> = emptyList(),
): Stay? {
    if (photos.isEmpty()) return null
    val fromPrice = fromPrice(from_minor, from_currency, from_basis, from_excludes) ?: return null
    val score = rating_score
    val count = rating_count
    val rating = if (score != null && count != null) Rating(score, count.toInt()) else null
    return Stay(
        id = StayId(id),
        cityId = CityId(city_id),
        areaKey = area_key,
        title = HostText(title_text, title_lang),
        description = HostText(description_text, description_lang),
        facts = StayFacts(bedrooms.toInt(), beds.toInt(), bathrooms.toInt(), sleeps.toInt()),
        photos = photos.sortedBy { it.position }.map { it.toModel() },
        amenities = amenities.mapNotNullTo(LinkedHashSet()) { row -> Amenity.entries.find { it.id == row.amenity_id } },
        policies = policies.sortedBy { it.position }.mapNotNull { it.toModel() },
        rating = rating,
        location = GeoArea(GeoPoint(latitude, longitude), radius_metres.toInt()),
        fromPrice = fromPrice,
        walkMinutesToTransit = walk_minutes?.toInt(),
        longStayDiscountPercent = long_stay_discount_percent?.toInt(),
        instantBook = instant_book != 0L,
        inclusions = inclusions.mapNotNullTo(LinkedHashSet()) { row ->
            PriceInclusion.entries.find { it.id == row.inclusion_id }
        },
        descriptionTranslations = translations.map { HostText(it.text, it.lang) },
    )
}

private fun PhotoRow.toModel(): Photo {
    val text = caption_text
    val lang = caption_lang
    val caption = if (text != null && lang != null) HostText(text, lang) else null
    return Photo(PhotoId(id), url, caption)
}

// --- Policy ---

private object PolicyKind {
    const val FREE_CANCELLATION = "free_cancellation"
    const val FREE_CANCELLATION_BEFORE = "free_cancellation_before"
    const val DEPOSIT = "deposit"
    const val CHECK_IN = "check_in"
    const val CHECK_OUT = "check_out"
    const val MINIMUM_STAY = "minimum_stay"
}

private fun Policy.toRow(stayId: String, position: Long): PolicyRow {
    val empty = PolicyRow(stayId, position, "", null, null, null, null, null, null, null, null)
    return when (this) {
        is Policy.FreeCancellation -> empty.copy(kind = PolicyKind.FREE_CANCELLATION, date = until.toString())
        is Policy.FreeCancellationBefore ->
            empty.copy(kind = PolicyKind.FREE_CANCELLATION_BEFORE, days = daysBeforeCheckIn.toLong())
        is Policy.Deposit -> empty.copy(
            kind = PolicyKind.DEPOSIT,
            amount_minor = amount.minor,
            currency = amount.currency.code,
            refundable = if (refundable) 1 else 0,
        )
        is Policy.CheckIn -> empty.copy(kind = PolicyKind.CHECK_IN, time = from.toString(), method = method.id)
        is Policy.CheckOut -> empty.copy(kind = PolicyKind.CHECK_OUT, time = by.toString())
        is Policy.MinimumStay -> empty.copy(kind = PolicyKind.MINIMUM_STAY, nights = nights.toLong())
    }
}

private fun PolicyRow.toModel(): Policy? = when (kind) {
    PolicyKind.FREE_CANCELLATION -> date?.let { Policy.FreeCancellation(LocalDate.parse(it)) }
    PolicyKind.FREE_CANCELLATION_BEFORE -> days?.let { Policy.FreeCancellationBefore(it.toInt()) }
    PolicyKind.DEPOSIT -> money(amount_minor, currency)?.let { Policy.Deposit(it, refundable = refundable != 0L) }
    PolicyKind.CHECK_IN -> checkIn()
    PolicyKind.CHECK_OUT -> time?.let { Policy.CheckOut(LocalTime.parse(it)) }
    PolicyKind.MINIMUM_STAY -> nights?.let { Policy.MinimumStay(it.toInt()) }
    else -> null
}

private fun PolicyRow.checkIn(): Policy.CheckIn? {
    val from = time ?: return null
    val checkIn = CheckInMethod.entries.find { it.id == method } ?: return null
    return Policy.CheckIn(LocalTime.parse(from), checkIn)
}

// --- Blocked dates ---

internal fun DateRange.toBlockedRow(stayId: StayId): BlockedRow =
    BlockedRow(stay_id = stayId.value, start_date = start.toString(), end_date = end.toString())

internal fun BlockedRow.toModel(): DateRange = DateRange(LocalDate.parse(start_date), LocalDate.parse(end_date))

// --- Quote ---

internal fun Quote.toRow(): QuoteRow = QuoteRow(
    id = id.value,
    stay_id = stayId.value,
    start_date = dates.start.toString(),
    end_date = dates.end.toString(),
    adults = guests.adults.toLong(),
    children = guests.children.toLong(),
    total_minor = total.minor,
    currency = total.currency.code,
    priced_at = pricedAt?.toEpochMilliseconds(),
)

internal fun Quote.lineRows(): List<QuoteLineRow> = breakdown.lines.mapIndexed { position, line ->
    QuoteLineRow(
        quote_id = id.value,
        position = position.toLong(),
        kind = line.kind.id,
        amount_minor = line.amount.minor,
        quantity = line.quantity?.toLong(),
        unit_minor = line.unitAmount?.minor,
    )
}

/**
 * Rebuilds a quote. [PriceBreakdown] checks that the lines sum to the stored total (P-05), so a corrupted row fails
 * here, in data, rather than showing a wrong total; such a quote reads as missing.
 */
internal fun QuoteRow.toModel(lines: List<QuoteLineRow>): Quote? {
    val breakdown = currencyOf(currency)?.let { breakdownOf(lines, Money(total_minor, it)) } ?: return null
    return Quote(
        id = QuoteId(id),
        stayId = StayId(stay_id),
        dates = DateRange(LocalDate.parse(start_date), LocalDate.parse(end_date)),
        guests = Guests(adults.toInt(), children.toInt()),
        breakdown = breakdown,
        pricedAt = priced_at?.let(Instant::fromEpochMilliseconds),
    )
}

/** The breakdown of [lines] totalling [total], or null if a line is unknown or they do not sum to it. */
private fun breakdownOf(lines: List<QuoteLineRow>, total: Money): PriceBreakdown? {
    val currency = total.currency
    val priceLines = lines.sortedBy { it.position }.map { row ->
        val kind = PriceLineKind.entries.find { it.id == row.kind } ?: return null
        PriceLine(
            kind = kind,
            amount = Money(row.amount_minor, currency),
            quantity = row.quantity?.toInt(),
            unitAmount = row.unit_minor?.let { Money(it, currency) },
        )
    }
    return runCatching { PriceBreakdown(priceLines, total) }.getOrNull()
}

// --- Booking ---

internal fun Booking.toRow(): BookingRow = BookingRow(
    id = id.value,
    reference = reference,
    stay_id = stayId.value,
    quote_id = quoteId.value,
    start_date = dates.start.toString(),
    end_date = dates.end.toString(),
    adults = guests.adults.toLong(),
    children = guests.children.toLong(),
    total_minor = total.minor,
    currency = total.currency.code,
    status = status.id,
    created_at = createdAt.toEpochMilliseconds(),
)

internal fun BookingRow.toModel(): Booking? {
    val total = money(total_minor, currency) ?: return null
    val status = BookingStatus.entries.find { it.id == status } ?: return null
    return Booking(
        id = BookingId(id),
        reference = reference,
        stayId = StayId(stay_id),
        quoteId = QuoteId(quote_id),
        dates = DateRange(LocalDate.parse(start_date), LocalDate.parse(end_date)),
        guests = Guests(adults.toInt(), children.toInt()),
        total = total,
        status = status,
        createdAt = Instant.fromEpochMilliseconds(created_at),
    )
}

// --- Recent search ---

internal fun SearchRow.toModel(): SearchQuery {
    val start = start_date?.let(LocalDate::parse)
    val end = end_date?.let(LocalDate::parse)
    return SearchQuery(
        cityId = city_id?.let(::CityId),
        dates = if (start != null && end != null && end > start) DateRange(start, end) else null,
        guests = Guests(adults.toInt().coerceAtLeast(1), children.toInt().coerceAtLeast(0)),
        length = StayLength.entries.find { it.name == length },
        startMonth = start_month?.let(YearMonth::parseOrNull),
    )
}

// --- Shared ---

internal val PriceBasis.id: String get() = name.lowercase()

private fun currencyOf(code: String): Currency? = Currency.entries.find { it.code == code }

private fun money(minor: Long?, code: String?): Money? {
    val currency = code?.let(::currencyOf) ?: return null
    return minor?.let { Money(it, currency) }
}

/** A "from" price, or null when the currency or basis is unknown or no exclusion survives (P-03 needs one). */
private fun fromPrice(minor: Long, currency: String, basis: String, excludes: String): PriceDisplay.From? {
    val money = money(minor, currency) ?: return null
    val per = PriceBasis.entries.find { it.id == basis }?.takeIf { it != PriceBasis.Stay } ?: return null
    val exclusions = excludes.split(SEPARATOR).mapNotNull { id -> PriceExclusion.entries.find { it.id == id } }
    return if (exclusions.isEmpty()) null else PriceDisplay.From(money, per, exclusions)
}
