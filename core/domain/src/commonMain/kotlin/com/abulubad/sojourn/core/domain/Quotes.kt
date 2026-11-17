package com.abulubad.sojourn.core.domain

import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.common.AppResult
import com.abulubad.sojourn.core.data.PricingRepository
import com.abulubad.sojourn.core.data.StayRepository
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.model.QuoteId
import com.abulubad.sojourn.core.model.StayId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Prices chosen dates for a stay. Rules the device can check are checked first (dates in the past, minimum stay,
 * capacity, dates already booked), so the date picker answers at once and offline; then the API prices it. The
 * returned quote's id is what travels with the stay to every surface showing the total (P-02).
 */
class GetQuote(
    private val stays: StayRepository,
    private val pricing: PricingRepository,
    private val clock: AppClock,
) {
    suspend operator fun invoke(stayId: StayId, dates: DateRange, guests: Guests): AppResult<Quote> {
        if (dates.start < clock.today()) return invalid(StayRules.Problem.DATES_IN_PAST)
        val stay = stays.observeStay(stayId).first()
        if (stay != null) {
            val problem = StayRules.bookingProblem(stay, dates, guests, stays.observeBlockedDates(stayId).first())
            if (problem != null) return invalid(problem)
        }
        return pricing.quote(stayId, dates, guests)
    }

    private fun invalid(reason: String) = AppResult.Failure(AppError.Validation(reason))
}

/** A quote by id, for the booking bar, checkout and pay button: the same total the card showed (P-02). */
class ObserveQuote(private val pricing: PricingRepository) {
    operator fun invoke(id: QuoteId): Flow<Quote?> = pricing.observeQuote(id)
}
