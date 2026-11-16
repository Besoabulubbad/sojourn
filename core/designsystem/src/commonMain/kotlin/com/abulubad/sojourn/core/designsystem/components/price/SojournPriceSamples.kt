package com.abulubad.sojourn.core.designsystem.components.price

import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import com.abulubad.sojourn.core.model.QuoteId

/** The Price card's two cases, in the numbers the design system shows. Shared by previews and screenshot tests. */
object SojournPriceSamples {
    val dated = PriceDisplay.Total(QuoteId("quote-sample"), Money.of(1383, Currency.GBP), nights = 15)
    val fromMonth = PriceDisplay.From(
        Money.of(2450, Currency.GBP),
        PriceBasis.Month,
        listOf(PriceExclusion.CleaningFee),
    )
    val fromNightTwoExclusions = PriceDisplay.From(
        Money.of(92, Currency.GBP),
        PriceBasis.Night,
        listOf(PriceExclusion.CleaningFee, PriceExclusion.CityTax),
    )
}
