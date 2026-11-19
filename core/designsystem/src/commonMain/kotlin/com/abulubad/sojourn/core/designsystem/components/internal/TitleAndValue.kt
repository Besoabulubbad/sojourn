package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/**
 * A label at the start and its value at the end, on one line when both fit at full width; else the value moves under
 * the label, so neither is squeezed into a column of letters at 320 dp or large text (ListRow, the demo wallet).
 */
@Composable
internal fun TitleAndValue(
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
