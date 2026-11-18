package com.abulubad.sojourn.core.designsystem.components.navigation

import com.abulubad.sojourn.core.designsystem.components.bottomnav.SojournBottomNavSamples
import com.abulubad.sojourn.core.designsystem.components.refresh.SojournPullToRefreshSamples
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetSamples
import com.abulubad.sojourn.core.designsystem.components.topbar.SojournTopBarSamples
import com.abulubad.sojourn.core.designsystem.components.viewtoggle.SojournViewToggleSamples
import com.abulubad.sojourn.core.testing.SnapshotCase

/** The Navigation group: TopBar, BottomNav, Sheet, ViewToggle; each default in the full matrix, plus the peek sheet. */
val navigationSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("top_bar_states", fullMatrix = true) { SojournTopBarSamples.States() },
    SnapshotCase("step_top_bar", fullMatrix = true) { SojournTopBarSamples.Steps() },
    SnapshotCase("bottom_nav_tabs", fullMatrix = true) { SojournBottomNavSamples.Tabs() },
    SnapshotCase("sheet_filters", fullMatrix = true) { SojournSheetSamples.Filters() },
    SnapshotCase("sheet_peek") { SojournSheetSamples.Peek() },
    SnapshotCase("view_toggle_pills", fullMatrix = true) { SojournViewToggleSamples.Pills() },
    SnapshotCase("pull_to_refresh") { SojournPullToRefreshSamples.Refreshing() },
)
