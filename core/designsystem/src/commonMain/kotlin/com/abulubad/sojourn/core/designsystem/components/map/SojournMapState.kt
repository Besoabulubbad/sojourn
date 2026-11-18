package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.model.GeoBounds
import com.abulubad.sojourn.core.model.GeoPoint
import kotlinx.coroutines.flow.first

/** Where a map looks: its [center] and [zoom] (MapLibre's zoom levels: 0 is the world, about 13 a neighbourhood). */
@Immutable
data class SojournMapCamera(val center: GeoPoint, val zoom: Double)

/**
 * The state of a [SojournMap]: its [camera] (kept current while the guest pans and zooms), its [size], and the
 * camera moves a screen asks for. Survives configuration changes and process death with [rememberSojournMapState].
 */
@Stable
class SojournMapState(initial: SojournMapCamera) {

    /** The camera the map shows now. */
    var camera: SojournMapCamera by mutableStateOf(initial)
        internal set

    /** The map's size on screen; zero until it is laid out. */
    var size: DpSize by mutableStateOf(DpSize.Zero)
        internal set

    /** The parts of the map docked UI covers (search bar, card): fits and the attribution keep clear of them. */
    internal var insets: MapInsets by mutableStateOf(MapInsets.None)

    /** Moves the engine's camera; null until the map is on screen, when a move only records the camera. */
    internal var mover: (suspend (SojournMapCamera, Boolean) -> Unit)? = null

    /** The box the map shows; null until it is laid out. */
    val visibleBounds: GeoBounds?
        get() = size.takeIf { it.width > 0.dp && it.height > 0.dp }
            ?.let { SojournMapProjection.bounds(camera.center, camera.zoom, it) }

    /** Moves the camera to [camera]; [animate] eases there over the map's own duration. */
    suspend fun moveTo(camera: SojournMapCamera, animate: Boolean = true) {
        val move = mover
        if (move != null) move(camera, animate) else this.camera = camera
    }

    /**
     * Shows every one of [points] in the part of the map the [insets] leave clear, [margin] inside its edges, no
     * closer than [maxZoom]. Waits for the map's first layout.
     */
    suspend fun show(
        points: Collection<GeoPoint>,
        animate: Boolean = true,
        margin: Dp = SojournMapDefaults.FitMargin,
        maxZoom: Double = SojournMapDefaults.MAX_FIT_ZOOM,
    ) {
        val bounds = GeoBounds.around(points) ?: return
        val size = snapshotFlow { size }.first { it.width > 0.dp && it.height > 0.dp }
        val clear = DpSize(
            (size.width - insets.left - insets.right).coerceAtLeast(1.dp),
            (size.height - insets.top - insets.bottom).coerceAtLeast(1.dp),
        )
        val fitted = SojournMapProjection.fit(bounds, clear, margin, maxZoom)
        // The clear part's centre is off the map's centre by half the difference of the insets.
        val center = SojournMapProjection.shifted(
            fitted.center,
            dx = (insets.right - insets.left) / 2,
            dy = (insets.bottom - insets.top) / 2,
            zoom = fitted.zoom,
        )
        moveTo(SojournMapCamera(center, fitted.zoom), animate)
    }

    /** Zooms in on [point] by [steps] levels, keeping it where it is on screen if it is the centre. */
    suspend fun zoomInOn(point: GeoPoint, steps: Double = 2.0) =
        moveTo(SojournMapCamera(point, (camera.zoom + steps).coerceAtMost(SojournMapDefaults.MAX_ZOOM)))

    companion object {
        /** Saves the camera only: the rest is rebuilt when the map lays out again. */
        val Saver = listSaver<SojournMapState, Double>(
            save = { listOf(it.camera.center.latitude, it.camera.center.longitude, it.camera.zoom) },
            restore = { SojournMapState(SojournMapCamera(GeoPoint(it[0], it[1]), it[2])) },
        )
    }
}

/** Parts of the map covered by docked UI, in absolute (unmirrored) directions: the map itself is never mirrored. */
@Immutable
internal data class MapInsets(val left: Dp, val top: Dp, val right: Dp, val bottom: Dp) {
    companion object {
        val None = MapInsets(0.dp, 0.dp, 0.dp, 0.dp)
    }
}

/** A [SojournMapState] opening on [center] at [zoom], saved across configuration changes and process death. */
@Composable
fun rememberSojournMapState(center: GeoPoint, zoom: Double = SojournMapDefaults.CITY_ZOOM): SojournMapState =
    rememberSaveable(saver = SojournMapState.Saver) { SojournMapState(SojournMapCamera(center, zoom)) }
