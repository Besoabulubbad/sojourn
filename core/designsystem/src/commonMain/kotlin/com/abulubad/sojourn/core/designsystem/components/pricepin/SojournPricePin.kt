package com.abulubad.sojourn.core.designsystem.components.pricepin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.move
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.stays
import com.abulubad.sojourn.core.model.PriceDisplay
import org.jetbrains.compose.resources.pluralStringResource

/**
 * A map marker that shows a stay's price (PricePin card): the same total as its card for a dated search, the
 * "from" figure otherwise — whatever [price] the card shows.
 *
 * [state] Selected inverts to `ink` and scales to 1.12 over `fast`; Seen drops to `surfaceSunken`. The pill is
 * 30 dp tall inside a 48 dp target, and is announced as its price and selected state.
 */
@Composable
fun SojournPricePin(
    price: PriceDisplay,
    state: SojournPinState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SojournTheme.colors
    val label = rememberSojournFormatter().money(price.amount)
    val selected = state == SojournPinState.Selected
    val scale by animateFloatAsState(
        if (selected) SojournPricePinDefaults.SELECTED_SCALE else 1f,
        SojournMotion.move(SojournMotion.fast),
    )
    val (container, content, border) = when (state) {
        SojournPinState.Default -> Triple(c.surface, c.ink, c.hairline)
        SojournPinState.Seen -> Triple(c.surfaceSunken, c.inkMuted, c.hairline)
        SojournPinState.Selected -> Triple(c.ink, c.bg, c.ink)
    }
    PinTarget(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = label
            this.selected = selected
        },
    ) {
        Pill(
            container,
            border,
            minWidth = 0.dp,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        ) {
            // A price never wraps: "£1,38 / 3" would misread.
            BasicText(
                label,
                style = SojournTheme.type.labelSm.copy(color = content, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/** Pins closer than 40 dp merge into one disc with the number of stays; tapping it zooms in. */
@Composable
fun SojournClusterPin(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val description = pluralStringResource(Res.plurals.stays, count, formatter.count(count))
    PinTarget(onClick, modifier.semantics { contentDescription = description }) {
        Pill(
            container = c.brand,
            border = Color.Transparent,
            minWidth = SojournPricePinDefaults.ClusterSize,
            minHeight = SojournPricePinDefaults.ClusterSize,
        ) {
            BasicText(
                formatter.count(count),
                style = SojournTheme.type.labelSm.copy(color = c.onBrand, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/** The 48 dp target every pin sits in; the merged description replaces the visible text for screen readers. */
@Composable
private fun PinTarget(onClick: () -> Unit, modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .defaultMinSize(SojournSizes.touchMin, SojournSizes.touchMin)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun Pill(
    container: Color,
    border: Color,
    minWidth: Dp,
    modifier: Modifier = Modifier,
    minHeight: Dp = SojournPricePinDefaults.PinHeight,
    content: @Composable () -> Unit,
) {
    val elevation = SojournTheme.elevation.level2
    Box(
        modifier
            .shadow(
                elevation.elevation,
                SojournShapes.full,
                ambientColor = elevation.color,
                spotColor = elevation.color,
            )
            .clip(SojournShapes.full)
            .background(container)
            .border(1.dp, border, SojournShapes.full)
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .padding(horizontal = SojournPricePinDefaults.PinPadding),
        contentAlignment = Alignment.Center,
    ) { content() }
}
