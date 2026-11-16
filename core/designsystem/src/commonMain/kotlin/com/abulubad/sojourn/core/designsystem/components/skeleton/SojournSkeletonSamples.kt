package com.abulubad.sojourn.core.designsystem.components.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/** The Skeleton card's example: a stay card loading. */
object SojournSkeletonSamples {
    @Composable
    fun StayCard(modifier: Modifier = Modifier) {
        SojournStayCardSkeleton(modifier.padding(SojournSpacing.space4))
    }

    /** Loose blocks for any other layout: a round avatar beside two text lines (a list row loading). */
    @Composable
    fun Row(modifier: Modifier = Modifier) {
        Row(
            modifier.padding(SojournSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SojournSkeleton(Modifier.size(SojournSizes.touchMin), SojournShapes.full)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
                SojournSkeleton(
                    Modifier.fillMaxWidth(SojournSkeletonDefaults.SECOND_LINE_FRACTION).height(SojournSpacing.space4),
                )
                SojournSkeleton(
                    Modifier.fillMaxWidth(SojournSkeletonDefaults.FIRST_LINE_FRACTION).height(SojournSpacing.space3),
                )
            }
        }
    }
}
