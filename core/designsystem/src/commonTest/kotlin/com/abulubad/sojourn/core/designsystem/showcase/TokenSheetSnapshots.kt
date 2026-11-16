package com.abulubad.sojourn.core.designsystem.showcase

import com.abulubad.sojourn.core.testing.SnapshotCase

/** Phase 2 exit: every token family rendered by `SojournTheme`. Also the showcase swatch sheet. */
val tokenSheetSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("tokens_color") { SojournTokenSheet(TokenSection.Color) },
    SnapshotCase("tokens_type") { SojournTokenSheet(TokenSection.Type) },
    SnapshotCase("tokens_spacing") { SojournTokenSheet(TokenSection.Spacing) },
    SnapshotCase("tokens_shape") { SojournTokenSheet(TokenSection.Shape) },
    SnapshotCase("tokens_icons") { SojournTokenSheet(TokenSection.Icons) },
)
