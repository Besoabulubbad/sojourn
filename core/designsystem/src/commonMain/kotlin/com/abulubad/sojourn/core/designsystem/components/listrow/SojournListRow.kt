package com.abulubad.sojourn.core.designsystem.components.listrow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.focusRing
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * A panel of rows (ScreenProfile card's settings list): the `surface` with `radius-lg`. Rows inside pass `divider`
 * from the second on. With [choices], the panel is one selectable group, so a screen reader says "1 of 3".
 */
@Composable
fun SojournListGroup(
    modifier: Modifier = Modifier,
    choices: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(SojournShapes.lg)
            .background(SojournTheme.colors.surface)
            .padding(vertical = SojournSpacing.space1)
            .then(if (choices) Modifier.semantics { selectableGroup() } else Modifier),
        content = content,
    )
}

/**
 * A row that opens a page (ScreenProfile card): an icon, the [title], the current [value] when there is one
 * ("English", "System", "1,240 points") and a chevron that follows reading direction (L-05). One button, read as
 * title then value. When both do not fit on one line (a narrow screen, large text), the value moves under the
 * title rather than squeezing it.
 */
@Composable
fun SojournListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: SojournIcon? = null,
    value: String? = null,
    divider: Boolean = false,
) {
    val c = SojournTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    RowFrame(
        modifier.clickable(
            role = Role.Button,
            indication = ripple(),
            interactionSource = interactionSource,
            onClick = onClick,
        ),
        interactionSource,
        divider,
    ) {
        if (icon != null) SojournIcon(icon, contentDescription = null, tint = c.ink, size = SojournSizes.iconSm)
        TitleAndValue(
            title = {
                BasicText(title, style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.Medium))
            },
            value = value?.let { { BasicText(it, style = SojournTheme.type.body.copy(color = c.inkMuted)) } },
            modifier = Modifier.weight(1f),
        )
        SojournIcon(SojournIcon.ChevronEnd, contentDescription = null, tint = c.inkMuted, size = SojournSizes.iconSm)
    }
}

/**
 * One option of a single choice (the Language and Appearance pages): the [title], an optional [detail] under it,
 * and a check when [selected]. A radio button to assistive technology; put the options in a [SojournListGroup]
 * with `choices = true`.
 */
@Composable
fun SojournChoiceRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: SojournIcon? = null,
    detail: String? = null,
    divider: Boolean = false,
) {
    val c = SojournTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    RowFrame(
        modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            indication = ripple(),
            interactionSource = interactionSource,
            onClick = onClick,
        ),
        interactionSource,
        divider,
    ) {
        if (icon != null) SojournIcon(icon, contentDescription = null, tint = c.ink, size = SojournSizes.iconSm)
        Column(Modifier.weight(1f)) {
            BasicText(
                title,
                style = SojournTheme.type.body.copy(
                    color = c.ink,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )
            if (detail != null) BasicText(detail, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
        }
        if (selected) {
            SojournIcon(SojournIcon.Check, contentDescription = null, tint = c.brandInk, size = SojournSizes.iconSm)
        }
    }
}

/** The title at the start and the value at the end on one line when both fit at full width; else stacked. */
@Composable
private fun TitleAndValue(
    title: @Composable () -> Unit,
    value: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val gap = SojournSpacing.space3
    Layout(
        contents = listOf(title, value ?: {}),
        modifier = modifier,
    ) { (titles, values), constraints ->
        val width = constraints.maxWidth
        val titleNeeds = titles.sumOf { it.maxIntrinsicWidth(Constraints.Infinity) }
        val valueNeeds = values.sumOf { it.maxIntrinsicWidth(Constraints.Infinity) }
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val gapPx = gap.roundToPx()
        if (values.isEmpty() || titleNeeds + gapPx + valueNeeds <= width) {
            val v = values.map { it.measure(loose) }
            val valueSpace = v.maxOfOrNull { it.width + gapPx } ?: 0
            val t = titles.map { it.measure(loose.copy(maxWidth = (width - valueSpace).coerceAtLeast(0))) }
            val height = maxOf(t.maxOfOrNull { it.height } ?: 0, v.maxOfOrNull { it.height } ?: 0)
            layout(width, height) {
                t.forEach { it.placeRelative(0, (height - it.height) / 2) }
                v.forEach { it.placeRelative(width - it.width, (height - it.height) / 2) }
            }
        } else {
            val t = titles.map { it.measure(loose) }
            val v = values.map { it.measure(loose) }
            val titleHeight = t.maxOfOrNull { it.height } ?: 0
            val height = titleHeight + (v.maxOfOrNull { it.height } ?: 0)
            layout(width, height) {
                t.forEach { it.placeRelative(0, 0) }
                v.forEach { it.placeRelative(0, titleHeight) }
            }
        }
    }
}

@Composable
private fun RowFrame(
    modifier: Modifier,
    interactionSource: MutableInteractionSource,
    divider: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    val c = SojournTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .focusRing(interactionSource, cornerRadius = SojournShapes.all.getValue("radius-md"))
            .then(modifier)
            .defaultMinSize(minHeight = SojournSizes.touchMin)
            .padding(horizontal = SojournSpacing.space4)
            .then(
                if (divider) {
                    Modifier.drawBehind { drawRect(c.hairline, size = Size(size.width, 1.dp.toPx())) }
                } else {
                    Modifier
                },
            )
            .padding(vertical = SojournSpacing.space3),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
