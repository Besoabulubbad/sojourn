package com.abulubad.sojourn.core.model

import kotlin.jvm.JvmInline

/** Identifier of a [Stay]. Opaque; never parsed by the app. */
@JvmInline
value class StayId(val value: String)

/** Identifier of a [City]. Doubles as the localization key stem for its name (S-05). */
@JvmInline
value class CityId(val value: String)

/** Identifier of a [Quote]. Travels with the stay through navigation so every surface shows the same total (P-02). */
@JvmInline
value class QuoteId(val value: String)

/** Identifier of a [Booking]. */
@JvmInline
value class BookingId(val value: String)

/** Identifier of a [Photo]. */
@JvmInline
value class PhotoId(val value: String)
