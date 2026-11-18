package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.abulubad.sojourn.core.designsystem.components.pricepin.SojournPricePinDefaults
import com.abulubad.sojourn.core.model.GeoPoint
import kotlin.math.hypot

/** One pin on the map: a single item, or a cluster of items whose pins would overlap. */
@Immutable
sealed interface SojournPinGroup<out T> {
    /** Where the pin sits: the item's position, or the middle of a cluster's items. */
    val position: GeoPoint

    data class Single<T>(val item: T, override val position: GeoPoint) : SojournPinGroup<T>

    /** [positions] are the items' own, for zooming to the cluster. */
    data class Cluster<T>(
        val items: List<T>,
        override val position: GeoPoint,
        val positions: List<GeoPoint>,
    ) : SojournPinGroup<T>
}

/**
 * Groups [items] into pins at [zoom] (PricePin card: overlapping pins merge into a cluster with a count). Items closer
 * than [distance] on screen join the first pin they are close to, in the order given, so a list sorted by the
 * screen's priority keeps its first items as single pins where it can. [keep] is never clustered: the selected stay
 * stays its own pin.
 */
fun <T> sojournPinGroups(
    items: List<T>,
    zoom: Double,
    position: (T) -> GeoPoint,
    keep: (T) -> Boolean = { false },
    distance: Dp = SojournPricePinDefaults.ClusterDistance,
): List<SojournPinGroup<T>> {
    class Pile(val anchor: GeoPoint, val members: MutableList<T>, val positions: MutableList<GeoPoint>)

    val piles = mutableListOf<Pile>()
    val kept = mutableListOf<SojournPinGroup<T>>()
    for (item in items) {
        val point = position(item)
        if (keep(item)) {
            kept += SojournPinGroup.Single(item, point)
            continue
        }
        val near = piles.firstOrNull { pile ->
            // The offset between the two, worked in doubles: exact at any zoom.
            val apart = SojournMapProjection.screen(point, pile.anchor, zoom, DpSize.Zero)
            hypot(apart.x.value.toDouble(), apart.y.value.toDouble()) < distance.value
        }
        if (near != null) {
            near.members += item
            near.positions += point
        } else {
            piles += Pile(point, mutableListOf(item), mutableListOf(point))
        }
    }
    return piles.map { pile ->
        if (pile.members.size == 1) {
            SojournPinGroup.Single(pile.members.single(), pile.anchor)
        } else {
            val middle = GeoPoint(
                pile.positions.map { it.latitude }.average(),
                pile.positions.map { it.longitude }.average(),
            )
            SojournPinGroup.Cluster(pile.members.toList(), middle, pile.positions.toList())
        }
    } + kept
}
