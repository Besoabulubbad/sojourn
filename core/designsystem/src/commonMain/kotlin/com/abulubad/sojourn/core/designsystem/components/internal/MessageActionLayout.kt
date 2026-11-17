package com.abulubad.sojourn.core.designsystem.components.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max

/**
 * A message with an optional trailing action. The action sits at the end of the message's row while the message
 * keeps at least [stackRatio] of the width; otherwise (large text, a narrow screen) it moves below the message,
 * aligned to the end, so the message never shrinks to a word per line.
 */
@Composable
internal fun MessageActionLayout(
    gap: Dp,
    message: @Composable () -> Unit,
    action: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    stackRatio: Float = STACK_RATIO,
) {
    Layout(contents = listOf(message, action ?: {}), modifier = modifier) { (messages, actions), constraints ->
        val width = constraints.maxWidth
        val gapPx = gap.roundToPx()
        val actionPlaceable = actions.firstOrNull()?.measure(Constraints(maxWidth = width))
        val besideWidth = width - (actionPlaceable?.let { it.width + gapPx } ?: 0)
        val stacked = actionPlaceable != null && besideWidth < width * stackRatio
        val messageWidth = if (stacked || actionPlaceable == null) width else besideWidth
        val messagePlaceable = messages.first().measure(Constraints(minWidth = messageWidth, maxWidth = messageWidth))
        if (actionPlaceable == null) {
            layout(width, messagePlaceable.height) { messagePlaceable.placeRelative(0, 0) }
        } else if (stacked) {
            layout(width, messagePlaceable.height + actionPlaceable.height) {
                messagePlaceable.placeRelative(0, 0)
                actionPlaceable.placeRelative(width - actionPlaceable.width, messagePlaceable.height)
            }
        } else {
            val height = max(messagePlaceable.height, actionPlaceable.height)
            layout(width, height) {
                messagePlaceable.placeRelative(0, (height - messagePlaceable.height) / 2)
                actionPlaceable.placeRelative(width - actionPlaceable.width, (height - actionPlaceable.height) / 2)
            }
        }
    }
}

/** The share of the row a message keeps before its action moves below it (snackbar, banner). */
internal const val STACK_RATIO = 0.6f
