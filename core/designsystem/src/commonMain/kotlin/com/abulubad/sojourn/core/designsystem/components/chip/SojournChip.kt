package com.abulubad.sojourn.core.designsystem.components.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.components.internal.pressScale
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * A compact choice (Chip card): filter chips toggle, choice chips pick one of a set.
 *
 * - Selected is a filled `ink` chip **and** a semibold label, never a colour shift alone (C-04). Screen readers hear
 *   "selected".
 * - 40 dp tall with extra touch area to `touch-min` (48 dp).
 * - A row holds one kind of thing: amenities and room counts do not share a row.
 *
 * For read-only facts use [SojournFactChip].
 */
@Composable
fun SojournChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: SojournIcon? = null,
) {
    val c = SojournTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val content = if (selected) c.bg else c.ink
    ChipBody(
        label = label,
        icon = icon,
        container = if (selected) c.ink else c.surface,
        content = content,
        border = if (selected) c.ink else c.borderControl,
        semibold = selected,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .pressScale(interactionSource)
            .focusRing(interactionSource, cornerRadius = null)
            .clip(SojournShapes.full)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onClick = onClick,
            ),
    )
}

/** A read-only fact (Chip card, tinted): `brandTint` fill, `brandInk` label, not interactive. */
@Composable
fun SojournFactChip(
    label: String,
    modifier: Modifier = Modifier,
    icon: SojournIcon? = null,
) {
    val c = SojournTheme.colors
    ChipBody(
        label = label,
        icon = icon,
        container = c.brandTint,
        content = c.brandInk,
        border = Color.Transparent,
        semibold = false,
        modifier = modifier,
    )
}

@Composable
private fun ChipBody(
    label: String,
    icon: SojournIcon?,
    container: Color,
    content: Color,
    border: Color,
    semibold: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = SojournChipDefaults.Height)
            .clip(SojournShapes.full)
            .background(container)
            .border(1.dp, border, SojournShapes.full)
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space1),
        horizontalArrangement = Arrangement.spacedBy(SojournChipDefaults.IconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) SojournIcon(icon, contentDescription = null, tint = content, size = SojournSizes.iconSm)
        BasicText(
            text = label,
            style = SojournTheme.type.label.copy(
                color = content,
                fontWeight = if (semibold) FontWeight.SemiBold else SojournTheme.type.label.fontWeight,
            ),
        )
    }
}
