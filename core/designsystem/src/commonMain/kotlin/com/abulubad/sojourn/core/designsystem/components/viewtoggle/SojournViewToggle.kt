package com.abulubad.sojourn.core.designsystem.components.viewtoggle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.components.internal.pressScale
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_search_area
import com.abulubad.sojourn.core.localization.resources.action_view_list
import com.abulubad.sojourn.core.localization.resources.action_view_map
import com.abulubad.sojourn.core.localization.resources.cd_show_list
import com.abulubad.sojourn.core.localization.resources.cd_show_map
import org.jetbrains.compose.resources.stringResource

/**
 * The floating pill that switches Results between list and map (ViewToggle card).
 *
 * The label names where the tap goes: "Map" while [current] is the list, "List" on the map; it is announced as
 * "Show map" / "Show list". Results and Map keep it visible, centred above the bottom bar, clear of the map
 * attribution (I-04). An `ink` pill with `shadow-3`.
 */
@Composable
fun SojournViewToggle(
    current: SojournResultsView,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val toMap = current == SojournResultsView.List
    FloatingPill(
        icon = if (toMap) SojournIcon.Map else SojournIcon.List,
        label = stringResource(if (toMap) Res.string.action_view_map else Res.string.action_view_list),
        description = stringResource(if (toMap) Res.string.cd_show_map else Res.string.cd_show_list),
        onClick = onToggle,
        modifier = modifier,
        light = false,
    )
}

/** The ViewToggle's light sibling on the map: "Search this area" once the map has moved. */
@Composable
fun SojournSearchAreaPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(Res.string.action_search_area)
    FloatingPill(SojournIcon.Retry, label, label, onClick, modifier, light = true)
}

@Composable
private fun FloatingPill(
    icon: SojournIcon,
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier,
    light: Boolean,
) {
    val c = SojournTheme.colors
    val container = if (light) c.surface else c.ink
    val content = if (light) c.ink else c.bg
    val elevation = SojournTheme.elevation.level3
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier
            .padding(vertical = (SojournSizes.touchMin - SojournViewToggleDefaults.PillHeight) / 2)
            .pressScale(interactionSource)
            .focusRing(interactionSource, cornerRadius = null)
            .defaultMinSize(minHeight = SojournViewToggleDefaults.PillHeight)
            .shadow(
                elevation.elevation,
                SojournShapes.full,
                ambientColor = elevation.color,
                spotColor = elevation.color,
            )
            .clip(SojournShapes.full)
            .background(container)
            .border(1.dp, if (light) c.hairline else Color.Transparent, SojournShapes.full)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space2),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SojournIcon(icon, contentDescription = null, tint = content, size = SojournSizes.iconSm)
        BasicText(label, style = SojournTheme.type.label.copy(color = content, fontWeight = FontWeight.SemiBold))
    }
}
