package com.abulubad.sojourn.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme

/**
 * The screen frame and one of the five owners of system insets (I-01). It measures the docked [topBar] and
 * [bottomBar] and hands [content] the padding it needs: bar heights plus whatever insets the bars do not cover
 * (I-03). Screens apply that padding to their scrolling container's `contentPadding`; they never read insets.
 *
 * The bars themselves (`SojournTopBar`, `SojournBottomNav`, `SojournBookingBar`) consume their own edge's inset,
 * so with a bar present the scaffold adds only the bar's measured height on that edge.
 */
@Composable
fun SojournScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    SubcomposeLayout(modifier.fillMaxSize().background(SojournTheme.colors.bg)) { constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val top = subcompose(Slot.Top, topBar).map { it.measure(loose) }
        val bottom = subcompose(Slot.Bottom, bottomBar).map { it.measure(loose) }
        val topHeight = top.maxOfOrNull { it.height } ?: 0
        val bottomHeight = bottom.maxOfOrNull { it.height } ?: 0

        val padding = PaddingValues(
            start = safe.calculateStartPadding(layoutDirection),
            end = safe.calculateEndPadding(layoutDirection),
            top = if (topHeight > 0) topHeight.toDp() else safe.calculateTopPadding(),
            bottom = if (bottomHeight > 0) bottomHeight.toDp() else safe.calculateBottomPadding(),
        )
        val body = subcompose(Slot.Content) { Box { content(padding) } }
            .map { it.measure(Constraints.fixed(constraints.maxWidth, constraints.maxHeight)) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            body.forEach { it.place(0, 0) }
            top.forEach { it.place(0, 0) }
            bottom.forEach { it.place(0, constraints.maxHeight - bottomHeight) }
        }
    }
}

private enum class Slot { Top, Bottom, Content }

/** Zero padding, for previews of content without a scaffold. */
val NoPadding: PaddingValues = PaddingValues(0.dp)
