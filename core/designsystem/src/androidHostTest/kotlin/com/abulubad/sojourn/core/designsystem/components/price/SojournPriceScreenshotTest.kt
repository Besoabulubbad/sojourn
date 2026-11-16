package com.abulubad.sojourn.core.designsystem.components.price

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.testing.SojournScreenshotTest
import org.junit.Test

/** The Price card's states. The dated price is the default state and runs the full 16-cell matrix. */
class SojournPriceScreenshotTest(variant: SojournVariant) : SojournScreenshotTest(variant) {

    @Test
    fun dated() = snapshot("price_dated", fullMatrix = true) {
        SojournPrice(SojournPriceSamples.dated, Modifier.padding(SojournSpacing.space4))
    }

    @Test
    fun fromMonth() = snapshot("price_from_month") {
        SojournPrice(SojournPriceSamples.fromMonth, Modifier.padding(SojournSpacing.space4))
    }

    @Test
    fun fromNightTwoExclusions() = snapshot("price_from_night_two_exclusions") {
        SojournPrice(SojournPriceSamples.fromNightTwoExclusions, Modifier.padding(SojournSpacing.space4))
    }

    @Test
    fun compact() = snapshot("price_compact") {
        SojournPrice(
            SojournPriceSamples.dated,
            Modifier.padding(SojournSpacing.space4),
            size = SojournPriceSize.Compact,
        )
    }
}
