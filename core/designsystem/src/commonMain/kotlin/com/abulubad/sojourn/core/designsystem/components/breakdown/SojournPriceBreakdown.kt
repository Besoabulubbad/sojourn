package com.abulubad.sojourn.core.designsystem.components.breakdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.breakdown_deposit_note
import com.abulubad.sojourn.core.localization.resources.line_city_tax
import com.abulubad.sojourn.core.localization.resources.line_cleaning_fee
import com.abulubad.sojourn.core.localization.resources.line_long_stay_discount
import com.abulubad.sojourn.core.localization.resources.line_nights
import com.abulubad.sojourn.core.localization.resources.line_nights_plain
import com.abulubad.sojourn.core.localization.resources.line_service_fee
import com.abulubad.sojourn.core.localization.resources.line_total
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.PriceBreakdown
import com.abulubad.sojourn.core.model.PriceLine
import com.abulubad.sojourn.core.model.PriceLineKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Every line that makes up a total (PriceBreakdown card): the nightly rate times the nights, discounts, each fee,
 * then the total. `PriceBreakdown` cannot be built unless its lines sum to the total (P-05), so what this shows
 * always adds up.
 *
 * Discounts are negative lines, shown with a minus sign in `brandInk` — never a red/green pair. The refundable
 * [deposit] is not part of the total: it is listed under it, as a note.
 */
@Composable
fun SojournPriceBreakdown(breakdown: PriceBreakdown, modifier: Modifier = Modifier, deposit: Money? = null) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val body = SojournTheme.type.body
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
        breakdown.lines.forEach { line ->
            val discount = line.amount.isNegative
            LineRow(
                label = lineLabel(line),
                amount = formatter.money(line.amount),
                style = body.copy(
                    color = if (discount) c.brandInk else c.ink,
                    fontWeight = if (discount) FontWeight.Medium else FontWeight.Normal,
                ),
            )
        }
        LineRow(
            label = stringResource(Res.string.line_total),
            amount = formatter.money(breakdown.total),
            style = SojournTheme.type.bodyLg.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .drawBehind { drawRect(c.hairline, size = Size(size.width, 1.dp.toPx())) }
                .padding(top = SojournSpacing.space3),
        )
        if (deposit != null) {
            BasicText(
                stringResource(Res.string.breakdown_deposit_note, formatter.money(deposit)),
                style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
            )
        }
    }
}

@Composable
private fun LineRow(label: String, amount: String, style: TextStyle, modifier: Modifier = Modifier) {
    // One announcement per line: "Cleaning fee, £60".
    Row(
        modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        verticalAlignment = Alignment.Top,
    ) {
        BasicText(label, modifier = Modifier.weight(1f), style = style)
        BasicText(amount, style = style)
    }
}

@Composable
private fun lineLabel(line: PriceLine): String {
    val formatter = rememberSojournFormatter()
    return when (line.kind) {
        PriceLineKind.Nights -> {
            val quantity = line.quantity
            val unit = line.unitAmount
            if (quantity != null && unit != null) {
                stringResource(
                    Res.string.line_nights,
                    formatter.money(unit),
                    pluralStringResource(Res.plurals.nights, quantity, formatter.count(quantity)),
                )
            } else {
                stringResource(Res.string.line_nights_plain)
            }
        }
        PriceLineKind.LongStayDiscount -> stringResource(Res.string.line_long_stay_discount)
        PriceLineKind.CleaningFee -> stringResource(Res.string.line_cleaning_fee)
        PriceLineKind.ServiceFee -> stringResource(Res.string.line_service_fee)
        PriceLineKind.CityTax -> stringResource(Res.string.line_city_tax)
    }
}
