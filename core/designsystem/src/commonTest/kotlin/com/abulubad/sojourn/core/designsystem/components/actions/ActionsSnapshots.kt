package com.abulubad.sojourn.core.designsystem.components.actions

import com.abulubad.sojourn.core.designsystem.components.actionbar.SojournActionBarSamples
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonSamples
import com.abulubad.sojourn.core.designsystem.components.chip.SojournChipSamples
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonSamples
import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * The Actions group: Button, Chip, IconButton. Each component's main sheet is its default state and runs the full
 * matrix on Android. The sheets are the `*Samples` objects the previews use too.
 */
val actionsSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("button_variants", fullMatrix = true) { SojournButtonSamples.Variants() },
    SnapshotCase("button_states") { SojournButtonSamples.States() },
    SnapshotCase("button_long_label") { SojournButtonSamples.LongLabel() },
    SnapshotCase("action_bar_footers", fullMatrix = true) { SojournActionBarSamples.Footers() },
    SnapshotCase("chip_row", fullMatrix = true) { SojournChipSamples.Row() },
    SnapshotCase("icon_button_variants", fullMatrix = true) { SojournIconButtonSamples.Variants() },
)
