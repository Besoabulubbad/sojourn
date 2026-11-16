package com.abulubad.sojourn.core.designsystem.components.price

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.localization.SojournFormatter
import com.abulubad.sojourn.core.localization.localizedList
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.exclusion_city_tax
import com.abulubad.sojourn.core.localization.resources.exclusion_cleaning_fee
import com.abulubad.sojourn.core.localization.resources.exclusion_deposit
import com.abulubad.sojourn.core.localization.resources.exclusion_utilities
import com.abulubad.sojourn.core.localization.resources.price_before_fees
import com.abulubad.sojourn.core.localization.resources.price_from_month
import com.abulubad.sojourn.core.localization.resources.price_from_night
import com.abulubad.sojourn.core.localization.resources.price_nightly_all_in
import com.abulubad.sojourn.core.localization.resources.price_total
import com.abulubad.sojourn.core.model.PriceBasis
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.PriceExclusion
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The only component allowed to print a price (Price card, standard P-01). It takes a [PriceDisplay], never a
 * number, so the card, the pin, the booking bar and checkout show the same figure from the same quote (P-02).
 *
 * - [PriceDisplay.Total]: leads with the total for the stay ("£1,383 total"); underneath, the nightly figure with
 *   all fees included ("£92 a night, all fees included"). A raw nightly rate that excludes fees is never shown.
 * - [PriceDisplay.From]: "from £2,450 a month", and underneath what it excludes ("Before cleaning fee. Add dates for
 *   the exact total."). The word "from" is never dropped (P-03).
 *
 * Every amount is formatted by [SojournFormatter] in the in-app locale and wrapped in a bidi isolate, and every
 * sentence comes from a string resource, so "per night£93.93" cannot happen (S-02, S-04, P-06). The two lines are
 * read as one by screen readers.
 *
 * ```
 * SojournPrice(quote.display)
 * SojournPrice(stay.fromPrice, size = SojournPriceSize.Compact)
 * ```
 */
@Composable
fun SojournPrice(
    price: PriceDisplay,
    modifier: Modifier = Modifier,
    size: SojournPriceSize = SojournPriceSize.Card,
) {
    val formatter = rememberSojournFormatter()
    val lines = price.lines(formatter)
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        BasicText(
            text = lines.lead,
            style = SojournTheme.type.price.copy(color = SojournTheme.colors.ink),
        )
        if (size == SojournPriceSize.Card) {
            BasicText(
                text = lines.support,
                style = SojournTheme.type.caption.copy(color = SojournTheme.colors.inkMuted),
            )
        }
    }
}

private class PriceLines(val lead: String, val support: String)

@Composable
private fun PriceDisplay.lines(formatter: SojournFormatter): PriceLines = when (this) {
    is PriceDisplay.Total -> PriceLines(
        lead = stringResource(Res.string.price_total, formatter.money(amount)),
        support = stringResource(Res.string.price_nightly_all_in, formatter.moneyAverage(amount, nights)),
    )
    is PriceDisplay.From -> PriceLines(
        lead = stringResource(
            if (per == PriceBasis.Month) Res.string.price_from_month else Res.string.price_from_night,
            formatter.money(amount),
        ),
        support = stringResource(
            Res.string.price_before_fees,
            localizedList(excludes.map { stringResource(it.label) }),
        ),
    )
}

private val PriceExclusion.label: StringResource
    get() = when (this) {
        PriceExclusion.Utilities -> Res.string.exclusion_utilities
        PriceExclusion.CleaningFee -> Res.string.exclusion_cleaning_fee
        PriceExclusion.Deposit -> Res.string.exclusion_deposit
        PriceExclusion.CityTax -> Res.string.exclusion_city_tax
    }
