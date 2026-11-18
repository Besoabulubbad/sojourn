package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Density
import com.abulubad.sojourn.core.model.GeoPoint

/** Where the stand-in map places an overlay child. */
internal data class GeoPlacement(val point: GeoPoint, val alignment: Alignment)

internal data class GeoPlacementElement(val placement: GeoPlacement) : ModifierNodeElement<GeoPlacementNode>() {
    override fun create() = GeoPlacementNode(placement)

    override fun update(node: GeoPlacementNode) {
        node.placement = placement
    }
}

internal class GeoPlacementNode(var placement: GeoPlacement) : Modifier.Node(), ParentDataModifierNode {
    override fun Density.modifyParentData(parentData: Any?): Any = placement
}
