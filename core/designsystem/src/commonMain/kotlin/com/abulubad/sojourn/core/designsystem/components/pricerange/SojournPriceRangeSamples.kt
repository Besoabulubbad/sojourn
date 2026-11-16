package com.abulubad.sojourn.core.designsystem.components.pricerange

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.MoneyRange
import com.abulubad.sojourn.core.model.PriceBasis

/** The RangeSlider card's example: total price for 15 nights, £900 to £2,400 within £300 to £4,300. */
object SojournPriceRangeSamples {

    private const val NIGHTS = 15
    val Bounds = MoneyRange(Money.of(300, Currency.GBP), Money.of(4_300, Currency.GBP))
    val Selected = MoneyRange(Money.of(900, Currency.GBP), Money.of(2_400, Currency.GBP))

    /** Stays per price bucket, the card's histogram. */
    @Suppress("MagicNumber")
    val Histogram = listOf(8, 14, 22, 30, 40, 44, 36, 30, 24, 18, 12, 9, 6, 4)

    @Composable
    fun Total(modifier: Modifier = Modifier) {
        var range by remember { mutableStateOf(Selected) }
        SojournPriceRange(
            range = range,
            bounds = Bounds,
            histogram = Histogram,
            basis = PriceBasis.Stay,
            onChange = { range = it },
            modifier = modifier.padding(SojournSpacing.space4),
            nights = NIGHTS,
        )
    }
}
