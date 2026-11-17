package com.abulubad.sojourn.core.designsystem.components.keyfacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/** The KeyFacts card's studio, and a two-bedroom flat for the plural forms. */
object SojournKeyFactsSamples {

    @Composable
    fun StudioAndFlat(modifier: Modifier = Modifier) {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            SojournKeyFacts(SampleListings.canalStudio.facts)
            SojournKeyFacts(SampleListings.hydraFlat.facts)
        }
    }
}
