package com.abulubad.sojourn.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Every destination in the app (ADR 0005). Keys carry ids and primitives, never domain objects, so the back stack
 * survives process death. A key that shows a price carries the quote id (P-02). This sealed hierarchy is the only
 * place `NavKey` is implemented (Konsist).
 */
@Serializable
sealed interface SojournNavKey : NavKey

/**
 * The Where / When / Who of a search, flattened to primitives for the back stack. Dates are ISO `yyyy-MM-dd`;
 * [length] is a `StayLength` name, set by Explore's stay-length shortcuts or the flexible tab; [startMonth] is the
 * flexible tab's "Starting in", ISO `yyyy-MM`.
 */
@Serializable
data class SearchArgs(
    val cityId: String? = null,
    val checkIn: String? = null,
    val checkOut: String? = null,
    val adults: Int = 1,
    val children: Int = 0,
    val length: String? = null,
    val startMonth: String? = null,
)

/** Steps of the full-screen search flow. */
@Serializable
enum class SearchStep { Where, When, Who }

// Explore tab
@Serializable data object ExploreKey : SojournNavKey

@Serializable
data class SearchKey(val step: SearchStep = SearchStep.Where, val args: SearchArgs = SearchArgs()) : SojournNavKey

@Serializable data class ResultsKey(val args: SearchArgs) : SojournNavKey

/** The map for [args]; [stayId] opens with that stay's pin selected and its card raised (Details' "Show on map"). */
@Serializable data class MapKey(val args: SearchArgs, val stayId: String? = null) : SojournNavKey

/** The filter sheet over the results for [args]: it counts and prices against the same search. */
@Serializable data class FiltersKey(val args: SearchArgs) : SojournNavKey

/**
 * A stay's Details. [quoteId] is the dated total the card showed (P-02); [args] is the search it came from, for the
 * guests a new quote is for and the basis of a "from" price.
 */
@Serializable
data class StayDetailsKey(
    val stayId: String,
    val quoteId: String? = null,
    val args: SearchArgs = SearchArgs(),
) : SojournNavKey

@Serializable data class GalleryKey(val stayId: String, val index: Int = 0) : SojournNavKey

@Serializable data class CheckoutKey(val quoteId: String) : SojournNavKey

@Serializable data class BookingConfirmedKey(val bookingId: String) : SojournNavKey

// Saved tab
@Serializable data object SavedKey : SojournNavKey

// Bookings tab
@Serializable data object BookingsKey : SojournNavKey

@Serializable data class BookingDetailsKey(val bookingId: String) : SojournNavKey

// Profile tab
@Serializable data object ProfileKey : SojournNavKey

@Serializable data object LanguageKey : SojournNavKey

@Serializable data object AppearanceKey : SojournNavKey
