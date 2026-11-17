package com.abulubad.sojourn.core.designsystem.components.pricepin

import androidx.compose.ui.unit.dp

/** Measurements from the PricePin card. */
object SojournPricePinDefaults {
    /** The visible pill; the tap target around it is `touch-min`. */
    val PinHeight = 30.dp
    val PinPadding = 10.dp

    /** The cluster disc. */
    val ClusterSize = 36.dp

    /** Pins closer than this merge into a cluster (applied by the map, documented here). */
    val ClusterDistance = 40.dp

    /** The selected pin's scale. */
    const val SELECTED_SCALE = 1.12f
}
