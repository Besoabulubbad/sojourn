package com.abulubad.sojourn.core.designsystem.components.pricepin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.QuoteId

/** The PricePin card's four states: default, seen, selected and a cluster of six. */
object SojournPricePinSamples {

    private const val CLUSTER = 6

    @Composable
    fun States(modifier: Modifier = Modifier) {
        // Two rows: on the map every pin has its own place, and four pins at 200 % text do not share 320 dp.
        Column(modifier.fillMaxWidth().padding(SojournSpacing.space4)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SojournPricePin(total(DEFAULT_PRICE), SojournPinState.Default, onClick = {})
                SojournPricePin(total(SEEN_PRICE), SojournPinState.Seen, onClick = {})
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SojournPricePin(SampleListings.canalTotal, SojournPinState.Selected, onClick = {})
                SojournClusterPin(CLUSTER, onClick = {})
            }
        }
    }

    private const val DEFAULT_PRICE = 1210L
    private const val SEEN_PRICE = 1560L
    private const val NIGHTS = 15

    private fun total(pounds: Long) =
        PriceDisplay.Total(QuoteId("sample-$pounds"), Money.of(pounds, Currency.GBP), nights = NIGHTS)
}
