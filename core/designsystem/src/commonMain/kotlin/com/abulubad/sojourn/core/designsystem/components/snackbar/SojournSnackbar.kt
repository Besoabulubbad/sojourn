package com.abulubad.sojourn.core.designsystem.components.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.abulubad.sojourn.core.designsystem.components.internal.MessageActionLayout
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * A brief confirmation with an optional action (Snackbar card): "Removed from Saved · Undo". `ink` bar, `bg` text,
 * announced politely. Prefer an undo to a confirmation dialog for reversible actions. Shown through
 * [SojournSnackbarHost], which keeps it on screen for 5 seconds above the bottom bar.
 */
@Composable
fun SojournSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val c = SojournTheme.colors
    val elevation = SojournTheme.elevation.level3
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .shadow(elevation.elevation, SojournShapes.md, ambientColor = elevation.color, spotColor = elevation.color)
            .background(c.ink, SojournShapes.md)
            .defaultMinSize(minHeight = SojournSizes.touchMin)
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space2),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MessageActionLayout(
            gap = SojournSpacing.space3,
            modifier = Modifier.weight(1f),
            message = {
                BasicText(
                    text = message,
                    style = SojournTheme.type.label.copy(color = c.bg, fontWeight = FontWeight.Normal),
                )
            },
            action = actionLabel?.let { { ActionText(it, onAction, SojournTheme.type.label.copy(color = c.bg)) } },
        )
    }
}

/**
 * A standing condition such as being offline (Snackbar card, banner). `dangerTint` with `danger` text and the
 * offline icon. It takes its place in the layout and pushes content down; it never overlays content.
 */
@Composable
fun SojournOfflineBanner(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val c = SojournTheme.colors
    val style = SojournTheme.type.bodySm.copy(color = c.danger, fontWeight = FontWeight.Medium)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .background(c.dangerTint)
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space2),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SojournIcon(SojournIcon.Offline, contentDescription = null, tint = c.danger, size = SojournSizes.iconSm)
        MessageActionLayout(
            gap = SojournSpacing.space2,
            modifier = Modifier.weight(1f),
            message = { BasicText(message, style = style) },
            action = actionLabel?.let { { ActionText(it, onAction, style) } },
        )
    }
}

/** An underlined, semibold text action with a 48 dp target. */
@Composable
private fun ActionText(label: String, onClick: () -> Unit, style: androidx.compose.ui.text.TextStyle) {
    BasicText(
        text = label,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(role = Role.Button, onClick = onClick),
        style = style.copy(fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline),
    )
}
