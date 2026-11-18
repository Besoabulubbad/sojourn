package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.model.GeoBounds
import com.abulubad.sojourn.core.model.GeoPoint
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan

/**
 * Web Mercator, the projection MapLibre draws with: at zoom `z` the world is `512 × 2^z` dp square. Pin grouping,
 * the stand-in map in previews and screenshots, fits and the stay's area circle all use it, so they agree with the
 * map. Worked in doubles: at street zoom the world is hundreds of millions of dp wide, past a float's precision.
 */
object SojournMapProjection {

    /** [point] in world dp at [zoom], from the top-left of the world. */
    fun world(point: GeoPoint, zoom: Double): DpOffset = worldOf(point, zoom).let { DpOffset(it.x.dp, it.y.dp) }

    /** The point at world dp [offset] at [zoom]. */
    fun point(offset: DpOffset, zoom: Double): GeoPoint =
        pointOf(World(offset.x.value.toDouble(), offset.y.value.toDouble()), zoom)

    /** Where [point] sits in a map of [size] centred on [center] at [zoom], from its top-left. */
    fun screen(point: GeoPoint, center: GeoPoint, zoom: Double, size: DpSize): DpOffset {
        val p = worldOf(point, zoom)
        val c = worldOf(center, zoom)
        return DpOffset((p.x - c.x + size.width.value / 2).dp, (p.y - c.y + size.height.value / 2).dp)
    }

    /** The box a map of [size] centred on [center] at [zoom] shows. */
    fun bounds(center: GeoPoint, zoom: Double, size: DpSize): GeoBounds {
        val c = worldOf(center, zoom)
        val halfWidth = size.width.value / 2.0
        val halfHeight = size.height.value / 2.0
        val northWest = pointOf(World(c.x - halfWidth, c.y - halfHeight), zoom)
        val southEast = pointOf(World(c.x + halfWidth, c.y + halfHeight), zoom)
        return GeoBounds(southEast.latitude, northWest.longitude, northWest.latitude, southEast.longitude)
    }

    /** [point] moved by [dx], [dy] dp on screen at [zoom]. */
    fun shifted(point: GeoPoint, dx: Dp, dy: Dp, zoom: Double): GeoPoint {
        val at = worldOf(point, zoom)
        return pointOf(World(at.x + dx.value, at.y + dy.value), zoom)
    }

    /** How many metres one dp covers at [latitude] and [zoom]. */
    fun metresPerDp(latitude: Double, zoom: Double): Double =
        EARTH_CIRCUMFERENCE_M * cos(latitude * PI / HALF_TURN) / worldSize(zoom)

    /** [metres] on the ground at [latitude], as dp at [zoom]. */
    fun dp(metres: Int, latitude: Double, zoom: Double): Dp = (metres / metresPerDp(latitude, zoom)).dp

    /**
     * The centre and zoom that show all of [bounds] in a map of [size], [margin] clear on every side, no closer than
     * [maxZoom] (a single stay would otherwise fill the screen).
     */
    fun fit(bounds: GeoBounds, size: DpSize, margin: Dp, maxZoom: Double): SojournMapCamera {
        val northWest = worldOf(GeoPoint(bounds.north, bounds.west), 0.0)
        val southEast = worldOf(GeoPoint(bounds.south, bounds.east), 0.0)
        val center = pointOf(World((northWest.x + southEast.x) / 2, (northWest.y + southEast.y) / 2), 0.0)
        val width = max(southEast.x - northWest.x, MIN_SPAN)
        val height = max(southEast.y - northWest.y, MIN_SPAN)
        val zoomX = log2(max((size.width - margin * 2).value.toDouble(), 1.0) / width)
        val zoomY = log2(max((size.height - margin * 2).value.toDouble(), 1.0) / height)
        return SojournMapCamera(center, min(min(zoomX, zoomY), maxZoom))
    }

    private class World(val x: Double, val y: Double)

    private fun worldOf(point: GeoPoint, zoom: Double): World {
        val size = worldSize(zoom)
        val x = (point.longitude + HALF_TURN) / FULL_TURN
        val latitude = point.latitude.coerceIn(-MAX_LATITUDE, MAX_LATITUDE) * PI / HALF_TURN
        val y = (1 - ln(tan(latitude) + 1 / cos(latitude)) / PI) / 2
        return World(x * size, y * size)
    }

    private fun pointOf(world: World, zoom: Double): GeoPoint {
        val size = worldSize(zoom)
        val longitude = world.x / size * FULL_TURN - HALF_TURN
        val latitude = atan(sinh(PI * (1 - 2 * world.y / size))) * HALF_TURN / PI
        return GeoPoint(latitude, longitude)
    }

    private fun worldSize(zoom: Double): Double = WORLD_DP * 2.0.pow(zoom)

    /** The world's width at zoom 0, in dp (MapLibre's 512-point tiles). */
    private const val WORLD_DP = 512.0
    private const val EARTH_CIRCUMFERENCE_M = 40_075_016.686
    private const val MAX_LATITUDE = 85.051_128_78
    private const val HALF_TURN = 180.0
    private const val FULL_TURN = 360.0
    private const val MIN_SPAN = 1e-9
}
