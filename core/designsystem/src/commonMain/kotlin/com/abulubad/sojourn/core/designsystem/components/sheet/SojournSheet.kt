package com.abulubad.sojourn.core.designsystem.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.components.internal.ShareRow
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.enter
import com.abulubad.sojourn.core.designsystem.theme.exit
import com.abulubad.sojourn.core.designsystem.theme.rememberSojournHaptics
import com.abulubad.sojourn.core.designsystem.theme.settleSpring
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_close
import com.abulubad.sojourn.core.localization.resources.cd_sheet_collapse
import com.abulubad.sojourn.core.localization.resources.cd_sheet_expand
import com.abulubad.sojourn.core.localization.resources.cd_sheet_handle
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A bottom sheet for filters, price details, pickers and the map's results (Sheet card).
 *
 * - It rests only at the [SojournSheetState.detents]: peek (96 dp), half, full. Drag the sheet or its content to
 *   move between them; a fast fling goes to the next stop.
 * - Every sheet has a [title] and a close button: dragging is an extra, never the only way out. The handle offers
 *   expand and collapse to screen readers.
 * - A [modal] sheet enters over `scrim` (`slow`), and closes on the close button, a tap on the scrim, a drag below
 *   its lowest stop, system back or [SojournSheetState.hide] — each animates out and then calls [onDismiss].
 *   A non-modal sheet (the map's
 *   results) has no scrim; back first returns it to its lowest stop.
 * - [content] sits in a scrolling column with the sheet's side padding. The [footer] is pinned to the bottom at
 *   every stop above peek and owns the navigation-bar and keyboard insets (I-01); without a footer the content
 *   takes them. Footer actions share its width and wrap rather than truncate.
 *
 * The sheet draws in the screen's own layer, so place it last in a full-screen `Box` (the app's overlay scene
 * does this for sheet destinations).
 *
 * ```
 * val sheet = rememberSojournSheetState(SojournSheetDetent.Half, setOf(Half, Full))
 * SojournSheet(sheet, title = stringResource(Res.string.filters_title), onDismiss = onClose,
 *     footer = { SojournButton(showStays, onClick = onApply) }) { FilterRows() }
 * ```
 */
@Composable
fun SojournSheet(
    state: SojournSheetState,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    modal: Boolean = true,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val anchored = state.anchored
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val enterSpec = SojournMotion.enter<Float>(SojournMotion.slow)
    val exitSpec = SojournMotion.exit<Float>(SojournMotion.slow)
    SideEffect { state.exitSpec = exitSpec }
    val close: () -> Unit = if (modal) {
        { scope.launch { anchored.animateTo(SheetStop.Hidden, exitSpec) } }
    } else {
        onDismiss
    }

    if (modal) {
        LaunchedEffect(state) {
            val initial = SheetStop.of(state.initial)
            snapshotFlow { anchored.anchors.hasPositionFor(initial) }.first { it }
            launch { anchored.animateTo(initial, enterSpec) }
            // Dismissed once it has been shown and settles hidden again, however it got there.
            snapshotFlow { anchored.settledValue }.dropWhile { it == SheetStop.Hidden }.first { it == SheetStop.Hidden }
            currentOnDismiss()
        }
    }
    SheetBackHandler(state, modal, close)
    if (modal) {
        // Hides the page beneath from screen readers while this sheet is shown (SojournModalSheets).
        val modalSheets = LocalSojournModalSheets.current
        DisposableEffect(modalSheets) {
            modalSheets.opened()
            onDispose { modalSheets.closed() }
        }
    }
    val haptics = rememberSojournHaptics()
    LaunchedEffect(state) {
        // A light tick each time the sheet comes to rest at a new stop (not when it first appears or hides).
        snapshotFlow { anchored.settledValue }
            .dropWhile { it == SheetStop.Hidden }
            .drop(1)
            .filter { it != SheetStop.Hidden }
            .collect { haptics.tick() }
    }

    Box(modifier.fillMaxSize()) {
        if (modal) Scrim(state, onTap = close)
        SheetSurface(state, modal, title, close, footer, content)
    }
}

@Composable
private fun SheetSurface(
    state: SojournSheetState,
    modal: Boolean,
    title: String,
    close: () -> Unit,
    footer: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = SojournTheme.colors
    val anchored = state.anchored
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val flingThreshold = with(density) { SojournSheetDefaults.FlingThreshold.toPx() }
    val settle = remember(anchored, flingThreshold) { SheetSettle(anchored, flingThreshold) }
    val nestedScroll = remember(anchored, settle) { SheetNestedScroll(anchored, settle) }
    val elevation = SojournTheme.elevation.level3
    val topInset = WindowInsets.statusBars
    val peekInset = WindowInsets.navigationBars
    val bottomInsets = WindowInsets.navigationBars.union(WindowInsets.ime).only(WindowInsetsSides.Bottom)
    val showFooter = footer != null && anchored.targetValue != SheetStop.Peek
    Column(
        Modifier
            .layout { measurable, constraints ->
                val height = constraints.maxHeight
                val anchors = sheetAnchors(
                    state = state,
                    modal = modal,
                    height = height.toFloat(),
                    top = topInset.getTop(this).toFloat(),
                    peek = SojournSheetDefaults.PeekHeight.toPx() + peekInset.getBottom(this),
                )
                val target = anchored.targetValue.takeIf { anchors.hasPositionFor(it) } ?: SheetStop.of(state.initial)
                anchored.updateAnchors(anchors, target)
                val offset = anchored.offset.takeUnless { it.isNaN() } ?: height.toFloat()
                val visible = (height - offset).roundToInt().coerceIn(0, height)
                val placeable = measurable.measure(constraints.copy(minHeight = visible, maxHeight = visible))
                layout(constraints.maxWidth, height) { placeable.place(0, offset.roundToInt()) }
            }
            .shadow(
                elevation.elevation,
                SojournShapes.sheetTop,
                ambientColor = elevation.color,
                spotColor = elevation.color,
            )
            .clip(SojournShapes.sheetTop)
            .background(c.surface)
            .nestedScroll(nestedScroll)
            .anchoredDraggable(
                state = anchored,
                reverseDirection = false,
                orientation = Orientation.Vertical,
                flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                    anchored,
                    animationSpec = SojournMotion.settleSpring(),
                ),
            ),
    ) {
        Handle(state, onMove = { detent -> scope.launch { state.animateTo(detent) } })
        Row(
            Modifier.fillMaxWidth().padding(start = SojournSpacing.space4, end = SojournSpacing.space1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                title,
                modifier = Modifier.weight(1f).semantics { heading() },
                style = SojournTheme.type.titleMd.copy(color = c.ink),
            )
            SojournIconButton(SojournIcon.Close, stringResource(Res.string.cd_close), onClick = close)
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .then(if (footer == null) Modifier.windowInsetsPadding(bottomInsets) else Modifier)
                .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space2),
            content = content,
        )
        if (showFooter && footer != null) {
            // Footer actions share the row, and wrap to a second row rather than break a word (large text).
            ShareRow(
                gap = SojournSpacing.space3,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind { drawRect(c.hairline, size = Size(size.width, 1.dp.toPx())) }
                    .windowInsetsPadding(bottomInsets)
                    .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space3),
                content = footer,
            )
        }
    }
}

/** The stops for this sheet in a layer [height] px tall; a modal sheet also gets the hidden stop below it. */
private fun sheetAnchors(
    state: SojournSheetState,
    modal: Boolean,
    height: Float,
    top: Float,
    peek: Float,
): DraggableAnchors<SheetStop> = DraggableAnchors {
    if (modal) SheetStop.Hidden at height
    if (SojournSheetDetent.Peek in state.detents) SheetStop.Peek at height - peek
    if (SojournSheetDetent.Half in state.detents) SheetStop.Half at height / 2f
    if (SojournSheetDetent.Full in state.detents) SheetStop.Full at top
}

/** The grab handle: decorative to touch (the whole sheet drags), expand/collapse to a screen reader. */
@Composable
private fun Handle(state: SojournSheetState, onMove: (SojournSheetDetent) -> Unit) {
    val current = state.anchored.currentValue.detent
    val atTop = current == state.highest
    val next = if (atTop) {
        state.lowest
    } else {
        SojournSheetDetent.entries.firstOrNull { it in state.detents && current != null && it > current }
    }
    val description = stringResource(Res.string.cd_sheet_handle)
    val actionLabel = stringResource(if (atTop) Res.string.cd_sheet_collapse else Res.string.cd_sheet_expand)
    Box(
        Modifier
            .fillMaxWidth()
            .then(
                if (state.detents.size > 1 && next != null) {
                    Modifier.semantics {
                        contentDescription = description
                        onClick(label = actionLabel) {
                            onMove(next)
                            true
                        }
                    }
                } else {
                    Modifier.clearAndSetSemantics {}
                },
            )
            .padding(top = SojournSpacing.space2, bottom = SojournSpacing.space1),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(SojournSheetDefaults.HandleWidth, SojournSheetDefaults.HandleHeight)
                .background(SojournTheme.colors.borderControl, SojournShapes.full),
        )
    }
}

/** `scrim` behind a modal sheet, fading with how far the sheet is open; a tap closes the sheet. */
@Composable
private fun Scrim(state: SojournSheetState, onTap: () -> Unit) {
    val scrim = SojournTheme.colors.scrim
    val anchored = state.anchored
    val currentOnTap by rememberUpdatedState(onTap)
    Box(
        Modifier
            .fillMaxSize()
            .clearAndSetSemantics {}
            .pointerInput(Unit) { detectTapGestures { currentOnTap() } }
            .drawBehind {
                val hidden = anchored.anchors.positionOf(SheetStop.Hidden)
                val rest = anchored.anchors.positionOf(SheetStop.of(state.lowest))
                val offset = anchored.offset
                val shown = ((hidden - offset) / (hidden - rest)).coerceIn(0f, 1f)
                // NaN while the anchors are not laid out yet (or hidden == rest): draw nothing.
                if (shown.isNaN()) return@drawBehind
                drawRect(scrim.copy(alpha = scrim.alpha * shown))
            },
    )
}

/** System back closes a modal sheet; on a non-modal one it first returns the sheet to its lowest stop. */
@Composable
private fun SheetBackHandler(state: SojournSheetState, modal: Boolean, close: () -> Unit) {
    // Previews and screenshot hosts have no dispatcher; there is nothing to go back from there.
    if (LocalNavigationEventDispatcherOwner.current == null) return
    val scope = rememberCoroutineScope()
    val anchored = state.anchored
    val enabled = if (modal) {
        anchored.targetValue != SheetStop.Hidden
    } else {
        anchored.targetValue != SheetStop.of(state.lowest)
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = enabled,
        onBackCompleted = { if (modal) close() else scope.launch { state.animateTo(state.lowest) } },
    )
}

/** Settles after a fling from scrolling content: the next stop in its direction when fast, else the nearest. */
private class SheetSettle(private val anchored: AnchoredDraggableState<SheetStop>, private val threshold: Float) {
    suspend operator fun invoke(velocity: Float) {
        val offset = anchored.offset.takeUnless { it.isNaN() } ?: return
        val anchors = anchored.anchors
        val target = if (abs(velocity) > threshold) {
            anchors.closestAnchor(offset, searchUpwards = velocity > 0) ?: anchors.closestAnchor(offset)
        } else {
            anchors.closestAnchor(offset)
        }
        if (target != null) anchored.animateTo(target, SojournMotion.settleSpring())
    }
}

/**
 * Lets the content scroll and the sheet move as one: dragging up raises the sheet before the content scrolls,
 * and pulling down past the top of the content lowers it.
 */
private class SheetNestedScroll(
    private val anchored: AnchoredDraggableState<SheetStop>,
    private val settle: SheetSettle,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        return if (delta < 0 && source == NestedScrollSource.UserInput) {
            Offset(0f, anchored.dispatchRawDelta(delta))
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        if (source == NestedScrollSource.UserInput) Offset(0f, anchored.dispatchRawDelta(available.y)) else Offset.Zero

    override suspend fun onPreFling(available: Velocity): Velocity {
        val offset = anchored.offset
        return if (available.y < 0 && !offset.isNaN() && offset > anchored.anchors.minPosition()) {
            settle(available.y)
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        settle(available.y)
        return available
    }
}
