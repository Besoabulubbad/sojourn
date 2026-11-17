package com.abulubad.sojourn.core.designsystem.components.breakdown

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBreakdown
import com.abulubad.sojourn.core.model.PriceLine
import com.abulubad.sojourn.core.model.PriceLineKind

/** The PriceBreakdown card's quote: £98 × 15 nights, a 10 % long-stay discount, cleaning, and the deposit note. */
object SojournPriceBreakdownSamples {

    @Suppress("MagicNumber") // The card's own numbers; they sum to £1,383 (P-05).
    private val quote = PriceBreakdown.of(
        listOf(
            PriceLine(
                PriceLineKind.Nights,
                Money.of(1470, Currency.GBP),
                quantity = 15,
                unitAmount = Money.of(98, Currency.GBP),
            ),
            PriceLine(PriceLineKind.LongStayDiscount, -Money.of(147, Currency.GBP)),
            PriceLine(PriceLineKind.CleaningFee, Money.of(60, Currency.GBP)),
        ),
    )

    @Suppress("MagicNumber")
    private val deposit = Money.of(250, Currency.GBP)

    @Composable
    fun Quote(modifier: Modifier = Modifier) {
        SojournPriceBreakdown(quote, modifier.padding(SojournSpacing.space4), deposit = deposit)
    }
}
