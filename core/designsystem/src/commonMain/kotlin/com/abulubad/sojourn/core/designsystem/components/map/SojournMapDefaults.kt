package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Zooms and sizes of [SojournMap]. */
object SojournMapDefaults {
    /** A whole city in view. */
    const val CITY_ZOOM: Double = 11.5

    /** A neighbourhood: the Details mini map, with room around a stay's area. */
    const val AREA_ZOOM: Double = 13.0

    /** Fitting a few stays never zooms closer than this, so one stay does not fill the screen. */
    const val MAX_FIT_ZOOM: Double = 15.0

    /** The closest the guest or a cluster tap can zoom. */
    const val MAX_ZOOM: Double = 18.0

    /** The edge of a stay's area disc. */
    val AreaEdge: Dp = 2.dp

    /** Space kept between fitted pins and the edge of the clear part of the map. */
    val FitMargin: Dp = 48.dp

    /** Where OpenFreeMap serves OpenMapTiles vector tiles (ADR 0017). */
    const val TILES_URL: String = "https://tiles.openfreemap.org/planet"

    /** Label fonts, served beside the tiles. */
    const val GLYPHS_URL: String = "https://tiles.openfreemap.org/fonts/{fontstack}/{range}.pbf"
}
