package com.abulubad.sojourn.core.designsystem.components.bookingcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.hosttext.HostText
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.booking_reference
import com.abulubad.sojourn.core.model.Photo
import org.jetbrains.compose.resources.stringResource
import com.abulubad.sojourn.core.model.HostText as HostTextModel

/**
 * A booking as the guest needs it on the way (ScreenBookings card): the stay's photo with a countdown [badge]
 * ("Check-in in 12 days"), the [place], the host's [title], the [details] line (dates, nights, guests) and the
 * [reference], then the card's [actions] ("Directions", "Check-in guide").
 *
 * - The reference is isolated left to right, so "SJ-4F7K2Q" never reorders inside Arabic text (ScreenBookings card).
 * - With [onClick] the card body is one button that opens the booking; the actions stay their own buttons.
 * - Two actions sit side by side while both labels fit on one line; otherwise (narrow screens, large text) they stack
 *   full width, so no label breaks mid-word.
 * - The photo slot shows placeholder, loaded and failed states like every photo.
 */
@Composable
fun SojournBookingCard(
    photo: Photo?,
    title: HostTextModel,
    place: String,
    details: String,
    reference: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onClick: (() -> Unit)? = null,
    actions: List<SojournBookingCardAction> = emptyList(),
) {
    val c = SojournTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(SojournShapes.lg)
            .background(c.surface)
            .padding(SojournSpacing.space3),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
        ) {
            Box(Modifier.fillMaxWidth().height(SojournBookingCardDefaults.PhotoHeight).clip(SojournShapes.md)) {
                if (photo != null) {
                    PhotoSlot(
                        photo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        showFailureText = false,
                    )
                }
                badge?.let { Badge(it, Modifier.align(Alignment.TopStart).padding(SojournSpacing.space2)) }
            }
            BasicText(
                place,
                modifier = Modifier.padding(top = SojournSpacing.space2),
                style = SojournTheme.type.label.copy(color = c.inkMuted),
            )
            HostText(title, style = SojournTheme.type.titleSm.copy(color = c.ink), maxLines = TITLE_LINES)
            BasicText(details, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
            BasicText(
                stringResource(Res.string.booking_reference, ltr(reference)),
                style = SojournTheme.type.caption.copy(color = c.inkMuted),
            )
        }
        if (actions.isNotEmpty()) Actions(actions)
    }
}

/** One of the card's buttons: [primary] for the main one ("Check-in guide"), secondary otherwise. */
class SojournBookingCardAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: SojournIcon? = null,
    val primary: Boolean = false,
)

/** Side by side in equal halves when every label fits its half on one line; stacked full width otherwise. */
@Composable
private fun Actions(actions: List<SojournBookingCardAction>) {
    Layout(
        content = {
            actions.forEach { action ->
                SojournButton(
                    action.label,
                    onClick = action.onClick,
                    variant = if (action.primary) SojournButtonVariant.Primary else SojournButtonVariant.Secondary,
                    leadingIcon = action.icon,
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val gap = SojournSpacing.space2.roundToPx()
        val width = constraints.maxWidth
        val share = (width - gap * (measurables.size - 1)) / measurables.size
        val sideBySide = measurables.all { it.maxIntrinsicWidth(constraints.maxHeight) <= share }
        val placeables = measurables.map {
            val w = if (sideBySide) share else width
            it.measure(constraints.copy(minWidth = w, maxWidth = w))
        }
        val height = if (sideBySide) {
            placeables.maxOf { it.height }
        } else {
            placeables.sumOf { it.height } + gap * (placeables.size - 1)
        }
        layout(width, height) {
            var x = 0
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x, y)
                if (sideBySide) x += placeable.width + gap else y += placeable.height + gap
            }
        }
    }
}

@Composable
private fun Badge(text: String, modifier: Modifier) {
    val c = SojournTheme.colors
    BasicText(
        text,
        modifier = modifier
            .background(c.accent, SojournShapes.xs)
            .padding(horizontal = SojournSpacing.space2, vertical = SojournSpacing.space1),
        style = SojournTheme.type.labelSm.copy(color = c.onAccent, fontWeight = FontWeight.SemiBold),
    )
}

/** Wraps [text] in a left-to-right isolate (U+2066 … U+2069): a reference keeps its order in Arabic. */
private fun ltr(text: String): String = "⁦$text⁩"

private const val TITLE_LINES = 2
