package com.abulubad.sojourn.core.designsystem.components.refresh

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme

/**
 * Pull to refresh for a scrolling list (ScreenResults: "pull to refresh"). Pulling past the threshold calls
 * [onRefresh]; the indicator, a `brand` arc on a `surface` disc, stays while [refreshing] is true. [indicatorTop]
 * places it below the screen's top bar (the scaffold's top padding), so it never draws under the bar.
 *
 * Refreshing never hides the list: cached content stays on screen while the new data loads (ADR 0007).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SojournPullToRefresh(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorTop: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    val c = SojournTheme.colors
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = refreshing,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = indicatorTop),
                containerColor = c.surface,
                color = c.brand,
            )
        },
        content = content,
    )
}
