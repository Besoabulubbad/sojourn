package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * Lays its children out as equal shares of one row, the way segments and paired fields sit. With large text it
 * protects words instead: each child gets at least its longest word and the rest is shared; if even that does not
 * fit, children wrap onto further rows (start-aligned, mirrored in RTL). A word never breaks mid-way.
 */
@Composable
internal fun ShareRow(gap: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val minimums = measurables.map { it.minIntrinsicWidth(Constraints.Infinity) }
        var next = 0
        val rows = shareRows(constraints.maxWidth, minimums, gapPx).map { widths ->
            val row = measurables.subList(next, next + widths.size)
            next += widths.size
            val height = row.indices.maxOf { row[it].maxIntrinsicHeight(widths[it]) }
            row.mapIndexed { i, child -> child.measure(Constraints.fixed(widths[i], height)) }
        }
        val height = rows.sumOf { it.first().height } + gapPx * (rows.size - 1)
        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + gapPx
                }
                y += row.first().height + gapPx
            }
        }
    }
}

/** Packs children into rows by their minimum widths, then sizes each row with [shareRow]. */
internal fun shareRows(available: Int, minimums: List<Int>, gap: Int): List<List<Int>> {
    val rows = mutableListOf<List<Int>>()
    var current = mutableListOf<Int>()
    minimums.forEach { minimum ->
        if (current.isNotEmpty() && current.sum() + minimum + gap * current.size > available) {
            rows += current
            current = mutableListOf()
        }
        current += minimum
    }
    rows += current
    return rows.map { shareRow(available - gap * (it.size - 1), it) }
}

/**
 * Equal shares when every minimum fits its share; otherwise each gets its minimum plus an equal part of what is
 * left. The rounding remainder goes to the last child so the row fills [available] exactly.
 */
internal fun shareRow(available: Int, minimums: List<Int>): List<Int> {
    val count = minimums.size
    val equal = available / count
    val widths = if (minimums.all { it <= equal } || minimums.sum() >= available) {
        List(count) { equal }
    } else {
        val extra = (available - minimums.sum()) / count
        minimums.map { it + extra }
    }
    return widths.toMutableList().also { it[count - 1] += available - widths.sum() }
}
