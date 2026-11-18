package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.model.GeoArea

/**
 * A stay's area on the map: before booking its position is an area, not a pin (brand book). A `brandTint` disc with a
 * `brand` edge, [area]'s radius on the ground at [zoom]. Place it on the area's centre with `Modifier.at`. Silent to
 * screen readers: the place line beside the map names the area.
 */
@Composable
fun SojournMapArea(area: GeoArea, zoom: Double, modifier: Modifier = Modifier) {
    val c = SojournTheme.colors
    val diameter = SojournMapProjection.dp(area.radiusMetres, area.center.latitude, zoom) * 2
    Box(
        modifier
            .size(diameter)
            .clearAndSetSemantics {}
            .background(c.brandTint, CircleShape)
            .border(SojournMapDefaults.AreaEdge, c.brand, CircleShape),
    )
}
