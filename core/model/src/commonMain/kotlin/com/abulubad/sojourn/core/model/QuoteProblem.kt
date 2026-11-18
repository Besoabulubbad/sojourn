package com.abulubad.sojourn.core.model

/**
 * Why dates and guests could not be priced, in the guest's terms and with the stay's own numbers ("at least five
 * nights", "up to two guests"). Details and checkout say it beside the picker or stepper that caused it.
 */
sealed interface QuoteProblem {
    data class MinimumNights(val nights: Int) : QuoteProblem
    data class TooManyGuests(val sleeps: Int) : QuoteProblem
    data object Unavailable : QuoteProblem
    data object InThePast : QuoteProblem

    /** Nothing was wrong with the request: the device is [offline], or pricing failed. */
    data class Failed(val offline: Boolean) : QuoteProblem
}
