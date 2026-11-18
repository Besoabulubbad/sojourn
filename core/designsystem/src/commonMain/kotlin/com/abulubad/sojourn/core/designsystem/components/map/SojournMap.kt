package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.LocalAppLocale
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.map_attribution
import com.abulubad.sojourn.core.model.GeoPoint
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

/**
 * A map in the app's colours (ScreenMap card): land, water, parks and roads are the map tokens, so the map follows
 * light and dark and never contrasts with the app; place names are in the app's language. MapLibre with OpenFreeMap
 * tiles (ADR 0017), wrapped so no feature sees either.
 *
 * - [overlay] draws on the map at geographic positions (`Modifier.at`): price pins, clusters, a stay's area. They
 *   move with the map. The map itself is never mirrored; overlays docked by the screen are.
 * - [contentPadding] is the part of the map the screen's docked UI covers: fits keep pins out from under it, and the
 *   attribution ("© OpenStreetMap contributors") sits just above its bottom edge, always visible.
 * - [onGuestMoved] reports the box in view each time the guest finishes a pan or zoom ("Search this area"); camera
 *   moves the screen makes do not count. [onMapTapped] reports a tap on the map, not on an overlay.
 * - [interactive] false keeps the camera still (Details' mini map); the screen makes the whole map one button.
 * - In previews and screenshots (`WithSojournMapStandIn`) the map is a stand-in in `mapLand`, the overlays placed by
 *   the same projection.
 */
@Composable
fun SojournMap(
    state: SojournMapState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactive: Boolean = true,
    onGuestMoved: (com.abulubad.sojourn.core.model.GeoBounds) -> Unit = {},
    onMapTapped: () -> Unit = {},
    overlay: @Composable SojournMapScope.() -> Unit = {},
) {
    val direction = LocalLayoutDirection.current
    SideEffect { state.insets = contentPadding.toInsets(direction) }
    val density = LocalDensity.current
    Box(
        modifier
            .fillMaxSize()
            .onSizeChanged { state.size = it.toDpSize(density) },
    ) {
        if (LocalSojournMapStandIn.current || LocalInspectionMode.current) {
            StandInMap(state, overlay)
        } else {
            LiveMap(state, interactive, onGuestMoved, onMapTapped, overlay)
        }
        Attribution(
            Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = contentPadding.calculateStartPadding(direction) + SojournSpacing.space2,
                    bottom = contentPadding.calculateBottomPadding() + SojournSpacing.space1,
                ),
        )
    }
}

/** True inside previews and screenshot tests: [SojournMap] draws its stand-in, not MapLibre. */
internal val LocalSojournMapStandIn = staticCompositionLocalOf { false }

@Composable
private fun LiveMap(
    state: SojournMapState,
    interactive: Boolean,
    onGuestMoved: (com.abulubad.sojourn.core.model.GeoBounds) -> Unit,
    onMapTapped: () -> Unit,
    overlay: @Composable SojournMapScope.() -> Unit,
) {
    val colors = SojournTheme.colors
    val language = LocalAppLocale.current.tag
    val style = remember(colors, language) { BaseStyle.Json(sojournMapStyle(colors, language)) }
    val start = remember { state.camera }
    val map = rememberMapState(baseStyle = style, initialCameraPosition = start.toCameraPosition())
    val guestMoved by rememberUpdatedState(onGuestMoved)
    val tapped by rememberUpdatedState(onMapTapped)

    DisposableEffect(map) {
        state.mover = { camera, animate ->
            if (animate) {
                map.animateCamera(CameraUpdate(center = camera.center.toPosition(), zoom = camera.zoom))
            } else {
                map.setCameraPosition(camera.toCameraPosition())
            }
        }
        onDispose { state.mover = null }
    }
    LaunchedEffect(map) {
        snapshotFlow { map.cameraPosition }.collect { position ->
            state.camera = SojournMapCamera(
                GeoPoint(position.center.latitude, position.center.longitude),
                position.zoom,
            )
        }
    }
    LaunchedEffect(map) {
        // A pan or zoom counts once the guest lets go and the map settles, not on every frame.
        var byGuest = false
        snapshotFlow { map.isCameraMoving to map.cameraMoveReason }.collect { (moving, reason) ->
            if (moving && reason == CameraMoveReason.Gesture) byGuest = true
            if (!moving && byGuest) {
                byGuest = false
                state.visibleBounds?.let(guestMoved)
            }
        }
    }
    val interactions = remember(interactive) {
        if (interactive) {
            MapInteractions {
                camera {
                    rotate { enabled = false }
                    pitch { enabled = false }
                }
                callbacks {
                    click {
                        onUnhandled {
                            tapped()
                            ClickResult.Consume
                        }
                    }
                }
            }
        } else {
            MapInteractions.None
        }
    }
    MaplibreMap(Modifier.fillMaxSize(), state = map, interactions = interactions) {
        LiveScope(this).overlay()
    }
}

private class LiveScope(private val scope: MapOverlayScope) : SojournMapScope {
    override fun Modifier.at(point: GeoPoint, alignment: Alignment): Modifier =
        with(scope) { this@at.placedAt(point.toPosition(), alignment) }
}

/** The map's land colour with the overlays where the map would put them; for previews and screenshots. */
@Composable
private fun StandInMap(state: SojournMapState, overlay: @Composable SojournMapScope.() -> Unit) {
    DisposableEffect(state) {
        state.mover = { camera, _ -> state.camera = camera }
        onDispose { state.mover = null }
    }
    val density = LocalDensity.current
    Layout(
        content = { StandInScope.overlay() },
        modifier = Modifier.fillMaxSize().background(SojournTheme.colors.mapLand),
    ) { measurables, constraints ->
        val loose = Constraints()
        val placeables = measurables.map { it.measure(loose) to it.parentData as? GeoPlacement }
        layout(constraints.maxWidth, constraints.maxHeight) {
            val size = IntSize(constraints.maxWidth, constraints.maxHeight).toDpSize(density)
            placeables.forEach { (placeable, placement) ->
                placement ?: return@forEach
                val at = SojournMapProjection.screen(placement.point, state.camera.center, state.camera.zoom, size)
                // The map is never mirrored: absolute placement, and the anchor read left to right.
                val box = IntSize(placeable.width, placeable.height)
                val anchor = placement.alignment.align(IntSize.Zero, box, LayoutDirection.Ltr)
                placeable.place(IntOffset(at.x.roundToPx() - anchor.x, at.y.roundToPx() - anchor.y))
            }
        }
    }
}

private object StandInScope : SojournMapScope {
    override fun Modifier.at(point: GeoPoint, alignment: Alignment): Modifier =
        then(GeoPlacementElement(GeoPlacement(point, alignment)))
}

/**
 * "© OpenStreetMap contributors · OpenFreeMap": required by the tiles' licence, never covered. Map chrome, like
 * MapLibre's own: it keeps its size when the guest enlarges text, so it never grows over the map it credits.
 */
@Composable
private fun Attribution(modifier: Modifier) {
    val c = SojournTheme.colors
    val caption = SojournTheme.type.caption
    val fixed = with(LocalDensity.current) { (caption.fontSize.value / fontScale).sp }
    BasicText(
        stringResource(Res.string.map_attribution),
        modifier = modifier
            .clip(SojournShapes.sm)
            .background(c.surface)
            .padding(horizontal = SojournSpacing.space1),
        style = caption.copy(color = c.inkMuted, fontSize = fixed, lineHeight = fixed * LINE_HEIGHT),
    )
}

private const val LINE_HEIGHT = 1.3f

private fun SojournMapCamera.toCameraPosition() = CameraPosition(center = center.toPosition(), zoom = zoom)

private fun GeoPoint.toPosition() = Position(longitude = longitude, latitude = latitude)

private fun IntSize.toDpSize(density: Density) = with(density) { DpSize(width.toDp(), height.toDp()) }

private fun PaddingValues.toInsets(direction: LayoutDirection) = MapInsets(
    left = if (direction == LayoutDirection.Ltr) calculateStartPadding(direction) else calculateEndPadding(direction),
    top = calculateTopPadding(),
    right = if (direction == LayoutDirection.Ltr) calculateEndPadding(direction) else calculateStartPadding(direction),
    bottom = calculateBottomPadding(),
)
