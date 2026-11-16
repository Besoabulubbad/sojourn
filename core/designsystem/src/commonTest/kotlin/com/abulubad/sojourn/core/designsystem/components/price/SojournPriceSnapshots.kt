package com.abulubad.sojourn.core.designsystem.components.price

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.testing.SnapshotCase

/** The Price card's states. The dated price is the default state and runs the full matrix on Android. */
val priceSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("price_dated", fullMatrix = true) {
        SojournPrice(SojournPriceSamples.dated, Modifier.padding(SojournSpacing.space4))
    },
    SnapshotCase("price_from_month") {
        SojournPrice(SojournPriceSamples.fromMonth, Modifier.padding(SojournSpacing.space4))
    },
    SnapshotCase("price_from_night_two_exclusions") {
        SojournPrice(SojournPriceSamples.fromNightTwoExclusions, Modifier.padding(SojournSpacing.space4))
    },
    SnapshotCase("price_compact") {
        SojournPrice(
            SojournPriceSamples.dated,
            Modifier.padding(SojournSpacing.space4),
            size = SojournPriceSize.Compact,
        )
    },
)
