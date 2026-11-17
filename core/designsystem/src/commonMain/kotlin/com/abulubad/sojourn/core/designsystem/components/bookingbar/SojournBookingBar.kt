package com.abulubad.sojourn.core.designsystem.components.bookingbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.internal.MessageActionLayout
import com.abulubad.sojourn.core.designsystem.components.price.SojournPrice
import com.abulubad.sojourn.core.designsystem.components.price.SojournPriceSize
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.SojournTransitions
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_choose_dates
import com.abulubad.sojourn.core.localization.resources.action_reserve
import com.abulubad.sojourn.core.localization.resources.booking_bar_detail
import com.abulubad.sojourn.core.localization.resources.price_details
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.PriceDisplay
import org.jetbrains.compose.resources.stringResource

/**
 * The docked bar on Details (BookingBar card): one price, one button.
 *
 * - Docked to the bottom edge, full width and opaque; nothing shows through or under it. It owns the
 *   navigation-bar inset (I-01); `SojournScaffold` gives the screen's content the matching bottom padding (I-03).
 * - Never disabled: with [dates] the button is "Reserve", without them "Choose dates", which opens the picker.
 * - The price is a `PriceDisplay` (P-01). When it changes it swaps with [SojournTransitions.priceSwap].
 * - "Price details" (with the dates when known) opens the breakdown through [onPriceDetails].
 * - With large text the button moves below the price rather than squeezing it.
 */
@Composable
fun SojournBookingBar(
    price: PriceDisplay,
    onPrimary: () -> Unit,
    onPriceDetails: () -> Unit,
    modifier: Modifier = Modifier,
    dates: DateRange? = null,
) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val details = stringResource(Res.string.price_details)
    val detailLine = dates
        ?.let { stringResource(Res.string.booking_bar_detail, formatter.dateRange(it), details) }
        ?: details
    val swap = SojournTransitions.priceSwap()
    MessageActionLayout(
        gap = SojournSpacing.space3,
        // The price is short ("£1,383 total"): it shares the bar with the button down to 45 % of the width.
        stackRatio = BOOKING_BAR_STACK_RATIO,
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface)
            .drawBehind { drawRect(c.hairline, size = Size(size.width, 1.dp.toPx())) }
            .windowInsetsPadding(WindowInsets.navigationBars)
            .defaultMinSize(minHeight = SojournSizes.bookingBar)
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space2),
        message = {
            Column {
                AnimatedContent(
                    targetState = price,
                    transitionSpec = { swap },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    SojournPrice(it, size = SojournPriceSize.Compact)
                }
                BasicText(
                    detailLine,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(role = Role.Button, onClick = onPriceDetails),
                    style = SojournTheme.type.bodySm.copy(color = c.ink, textDecoration = TextDecoration.Underline),
                )
            }
        },
        action = {
            SojournButton(
                stringResource(if (dates != null) Res.string.action_reserve else Res.string.action_choose_dates),
                onClick = onPrimary,
            )
        },
    )
}

private const val BOOKING_BAR_STACK_RATIO = 0.45f
