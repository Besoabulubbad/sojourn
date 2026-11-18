package com.abulubad.sojourn.core.designsystem.components.actionbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.internal.MessageActionLayout
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * The docked bar of a step-by-step flow (the ScreenSearchFlow footer): the step's [primary] action at the end, an
 * optional low-emphasis [secondary] link at the start ("Any time") and an optional [note] beside the button
 * ("15 nights").
 *
 * - Docked to the bottom edge, full width and opaque, like `SojournBookingBar`. It owns the navigation-bar inset
 *   (I-01); `SojournScaffold` gives the screen's content the matching bottom padding (I-03).
 * - The [note] is announced politely when it changes, so a screen reader hears the night count as a range completes.
 * - With large text the note and button move below the link rather than squeezing it.
 * - Prefer a working [primary] to a disabled one; [primaryEnabled] is for the step that cannot go on yet (no dates).
 * - [primaryLoading] keeps the button's place while its action runs ("Pay £1,383" while paying).
 * - [fillPrimary] is for a flow's one final action (the ScreenCheckout card's "Pay £1,383"): the button spans the
 *   bar and the link and note are not shown.
 */
@Composable
fun SojournActionBar(
    primary: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    secondary: String? = null,
    onSecondary: () -> Unit = {},
    note: String? = null,
    primaryLoading: Boolean = false,
    fillPrimary: Boolean = false,
) {
    if (fillPrimary) {
        Box(modifier.actionBarChrome()) {
            SojournButton(
                primary,
                onClick = onPrimary,
                modifier = Modifier.fillMaxWidth(),
                loading = primaryLoading,
                enabled = primaryEnabled,
            )
        }
        return
    }
    val c = SojournTheme.colors
    MessageActionLayout(
        gap = SojournSpacing.space3,
        // The link is short: it keeps the row unless it would wrap (large text, a narrow screen).
        stackOnlyIfWrapped = true,
        modifier = modifier.actionBarChrome(),
        message = {
            // Holds the row's height when there is no link, so the button keeps the end of the bar.
            Box(Modifier.defaultMinSize(minHeight = SojournSizes.touchMin), contentAlignment = Alignment.CenterStart) {
                if (secondary != null) {
                    BasicText(
                        secondary,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clickable(role = Role.Button, onClick = onSecondary),
                        style = SojournTheme.type.body.copy(color = c.ink, textDecoration = TextDecoration.Underline),
                    )
                }
            }
        },
        action = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (note != null) {
                    BasicText(
                        note,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
                    )
                }
                SojournButton(primary, onClick = onPrimary, loading = primaryLoading, enabled = primaryEnabled)
            }
        },
    )
}

/** Docked, opaque, a hairline on top, clear of the navigation bar (I-01). */
@Composable
private fun Modifier.actionBarChrome(): Modifier {
    val c = SojournTheme.colors
    return fillMaxWidth()
        .background(c.surface)
        .drawBehind { drawRect(c.hairline, size = Size(size.width, 1.dp.toPx())) }
        .windowInsetsPadding(WindowInsets.navigationBars)
        .defaultMinSize(minHeight = SojournSizes.bookingBar)
        .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space2)
}
