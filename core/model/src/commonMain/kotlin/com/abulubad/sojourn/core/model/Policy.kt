package com.abulubad.sojourn.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** How a guest gets in. Localized by id (S-05). */
enum class CheckInMethod(val id: String) {
    SelfCheckIn("self_check_in"),
    Host("host"),
    Lockbox("lockbox"),
}

/**
 * Policies carry their numbers (N-03): a cancellation has a date, a deposit an amount, check-in a time and a method.
 * A policy without its number cannot be constructed.
 */
sealed interface Policy {
    data class FreeCancellation(val until: LocalDate) : Policy
    data class Deposit(val amount: Money, val refundable: Boolean = true) : Policy
    data class CheckIn(val from: LocalTime, val method: CheckInMethod) : Policy
    data class CheckOut(val by: LocalTime) : Policy
    data class MinimumStay(val nights: Int) : Policy {
        init {
            require(nights >= 1) { "Minimum stay is at least one night" }
        }
    }
}
