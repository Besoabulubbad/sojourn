package com.abulubad.sojourn.core.designsystem

import com.abulubad.sojourn.core.designsystem.components.price.priceSnapshots
import com.abulubad.sojourn.core.designsystem.showcase.tokenSheetSnapshots
import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * Every screenshot case in `core:designsystem`, rendered on Android and iOS. A new component adds its
 * `<component>Snapshots` list here (Konsist fails the build if a public `Sojourn*` component has no case).
 */
val designSystemSnapshots: List<SnapshotCase> =
    tokenSheetSnapshots +
        priceSnapshots
