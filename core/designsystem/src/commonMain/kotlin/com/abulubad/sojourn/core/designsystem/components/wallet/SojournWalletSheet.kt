package com.abulubad.sojourn.core.designsystem.components.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDivider
import com.abulubad.sojourn.core.designsystem.components.internal.TitleAndValue
import com.abulubad.sojourn.core.designsystem.components.price.SojournPrice
import com.abulubad.sojourn.core.designsystem.components.price.SojournPriceSize
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.app_name
import com.abulubad.sojourn.core.localization.resources.wallet_card
import com.abulubad.sojourn.core.localization.resources.wallet_card_value
import com.abulubad.sojourn.core.localization.resources.wallet_hint
import com.abulubad.sojourn.core.localization.resources.wallet_note
import com.abulubad.sojourn.core.localization.resources.wallet_pay_to
import com.abulubad.sojourn.core.localization.resources.wallet_title
import com.abulubad.sojourn.core.localization.resources.wallet_total
import com.abulubad.sojourn.core.model.PriceDisplay
import org.jetbrains.compose.resources.stringResource

/**
 * The demo wallet (ScreenCheckout: "payment uses the platform sheet first"). It stands where Google Pay or Apple Pay
 * would open and says plainly that it is a demo: no real payment brand, logo or card, and no money moves.
 *
 * Who is paid, a demo card, the [price] (through [SojournPrice], P-02), and [payLabel] ("Pay £1,383", the same words
 * as the page's button). [onPay] confirms; closing the sheet in any way calls [onDismiss] and nothing is charged.
 */
@Composable
fun SojournWalletSheet(
    state: SojournSheetState,
    price: PriceDisplay.Total,
    payLabel: String,
    onPay: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SojournTheme.colors
    SojournSheet(
        state = state,
        title = stringResource(Res.string.wallet_title),
        onDismiss = onDismiss,
        modifier = modifier,
        footer = {
            Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
                SojournButton(payLabel, onClick = onPay, modifier = Modifier.fillMaxWidth())
                BasicText(
                    stringResource(Res.string.wallet_hint),
                    modifier = Modifier.fillMaxWidth(),
                    style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4)) {
            DemoNote()
            Column(
                Modifier.fillMaxWidth().clip(SojournShapes.lg).background(c.surfaceSunken),
            ) {
                Line(stringResource(Res.string.wallet_pay_to)) {
                    BasicText(stringResource(Res.string.app_name), style = valueStyle())
                }
                SojournDivider(Modifier.padding(horizontal = SojournSpacing.space4))
                Line(stringResource(Res.string.wallet_card)) {
                    BasicText(stringResource(Res.string.wallet_card_value), style = valueStyle())
                }
                SojournDivider(Modifier.padding(horizontal = SojournSpacing.space4))
                Line(stringResource(Res.string.wallet_total)) {
                    SojournPrice(price, size = SojournPriceSize.Compact)
                }
            }
        }
    }
}

/** Says, before anything else, that this is a demo and nothing is charged. */
@Composable
private fun DemoNote() {
    val c = SojournTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(SojournShapes.md)
            .background(c.accentTint)
            .padding(SojournSpacing.space3)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        verticalAlignment = Alignment.Top,
    ) {
        SojournIcon(SojournIcon.Info, contentDescription = null, tint = c.accentInk, size = SojournSizes.iconSm)
        BasicText(stringResource(Res.string.wallet_note), style = SojournTheme.type.bodySm.copy(color = c.accentInk))
    }
}

/** A label at the start, its value at the end (under it when both do not fit); read as one line. */
@Composable
private fun Line(label: String, value: @Composable () -> Unit) {
    TitleAndValue(
        title = { BasicText(label, style = SojournTheme.type.body.copy(color = SojournTheme.colors.inkMuted)) },
        value = value,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space3)
            .semantics(mergeDescendants = true) {},
    )
}

@Composable
private fun valueStyle() = SojournTheme.type.body.copy(color = SojournTheme.colors.ink, fontWeight = FontWeight.Medium)
