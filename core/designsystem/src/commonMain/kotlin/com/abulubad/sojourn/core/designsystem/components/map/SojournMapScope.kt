package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.model.GeoPoint

/** Places a map's overlays at geographic positions. */
interface SojournMapScope {
    /** Puts this direct child of the overlay so its [alignment] point sits on [point]; it moves with the map. */
    fun Modifier.at(point: GeoPoint, alignment: Alignment = Alignment.Center): Modifier
}
