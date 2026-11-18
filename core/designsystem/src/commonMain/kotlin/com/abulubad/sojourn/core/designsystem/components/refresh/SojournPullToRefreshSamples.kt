package com.abulubad.sojourn.core.designsystem.components.refresh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/** A list mid-refresh: the indicator over content that stays on screen. */
object SojournPullToRefreshSamples {

    @Composable
    fun Refreshing(modifier: Modifier = Modifier) {
        SojournPullToRefresh(refreshing = true, onRefresh = {}, modifier = modifier) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(SojournSpacing.space4),
                verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
            ) {
                repeat(ROWS) {
                    SojournSkeleton(Modifier.fillMaxWidth().height(SojournSpacing.space10), SojournShapes.md)
                }
            }
        }
    }

    private const val ROWS = 3
}
