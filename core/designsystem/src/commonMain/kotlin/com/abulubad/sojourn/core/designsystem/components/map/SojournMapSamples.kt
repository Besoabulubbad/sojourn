package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournClusterPin
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournPinState
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournPricePin
import com.abulubad.sojourn.core.designsystem.preview.WithSojournMapStandIn
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.model.GeoPoint

/** A city map with the three pin states and a cluster, and a stay's area map as Details shows it. */
@Suppress("MagicNumber") // Sample data: real London positions.
object SojournMapSamples {

    private val paddington = GeoPoint(51.5180, -0.1800)
    private val pins = listOf(
        paddington to SojournPinState.Selected,
        GeoPoint(51.5120, -0.2020) to SojournPinState.Seen,
        GeoPoint(51.5410, -0.1450) to SojournPinState.Default,
    )
    private val cluster = GeoPoint(51.5256, -0.0782)

    @Composable
    fun CityAndArea(modifier: Modifier = Modifier) = WithSojournMapStandIn {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            val city = rememberSojournMapState(paddington)
            LaunchedEffect(city) { city.show(pins.map { it.first } + cluster, animate = false) }
            SojournMap(city, Modifier.fillMaxWidth().height(320.dp).clip(SojournShapes.lg)) {
                pins.forEach { (point, state) ->
                    SojournPricePin(SampleListings.canalTotal, state, onClick = {}, modifier = Modifier.at(point))
                }
                SojournClusterPin(2, onClick = {}, modifier = Modifier.at(cluster))
            }
            val area = SampleListings.canalStudio.location
            val stay = rememberSojournMapState(area.center, SojournMapDefaults.AREA_ZOOM)
            val shape = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(SojournShapes.lg)
            SojournMap(stay, shape, interactive = false) {
                SojournMapArea(area, stay.camera.zoom, Modifier.at(area.center))
            }
        }
    }
}
