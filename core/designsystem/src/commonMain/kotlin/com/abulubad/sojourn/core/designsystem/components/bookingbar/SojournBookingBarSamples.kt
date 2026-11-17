package com.abulubad.sojourn.core.designsystem.components.bookingbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import kotlinx.datetime.LocalDate

/** The BookingBar card's two states: dates chosen ("Reserve") and no dates yet ("Choose dates"). */
object SojournBookingBarSamples {

    @Suppress("MagicNumber") // The card's own numbers.
    private val marchDates = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 3, 27))

    @Suppress("MagicNumber")
    private val fromMonthly =
        PriceDisplay.From(Money.of(2450, Currency.GBP), PriceBasis.Month, listOf(PriceExclusion.CleaningFee))

    @Composable
    fun States(modifier: Modifier = Modifier) {
        Column(
            modifier.padding(vertical = SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            SojournBookingBar(SampleListings.canalTotal, onPrimary = {}, onPriceDetails = {}, dates = marchDates)
            SojournBookingBar(fromMonthly, onPrimary = {}, onPriceDetails = {})
        }
    }
}
