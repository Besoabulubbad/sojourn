package com.abulubad.sojourn.core.designsystem.components.segmented

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.abulubad.sojourn.core.designsystem.components.internal.ShareRow
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * Two to five mutually exclusive options that fit one row (SegmentedControl card): Dates / Flexible, bedrooms.
 *
 * - The selected segment is a raised `surface` pill on the sunken track; screen readers hear radio buttons.
 * - Bedrooms use "Studio" for zero; the UI never prints "0 bedrooms".
 * - Segments share the width equally. With large text each segment gets at least its longest word, and segments
 *   wrap onto a second row rather than break a word. If that happens at normal sizes, use chips.
 */
@Composable
fun <T> SojournSegmentedControl(
    options: List<SojournSegment<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(options.size in SojournSegmentedControlDefaults.OptionCount) {
        "A segmented control has 2 to 5 options, not ${options.size}; use chips"
    }
    val c = SojournTheme.colors
    val raised = SojournTheme.elevation.level1
    ShareRow(
        gap = SojournSegmentedControlDefaults.SegmentGap,
        modifier = modifier
            .fillMaxWidth()
            .clip(SojournShapes.md)
            .background(c.surfaceSunken)
            .padding(SojournSegmentedControlDefaults.TrackPadding)
            .selectableGroup(),
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = SojournSegmentedControlDefaults.SegmentMinHeight)
                    .focusRing(interactionSource, cornerRadius = SojournShapes.all.getValue("radius-sm"))
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                raised.elevation,
                                SojournShapes.sm,
                                ambientColor = raised.color,
                                spotColor = raised.color,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clip(SojournShapes.sm)
                    .background(if (isSelected) c.surface else Color.Transparent)
                    .selectable(
                        selected = isSelected,
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.RadioButton,
                        onClick = { onSelect(option.value) },
                    )
                    .padding(horizontal = SojournSpacing.space2),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    option.label,
                    style = SojournTheme.type.label.copy(
                        color = if (isSelected) c.ink else c.inkMuted,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}
