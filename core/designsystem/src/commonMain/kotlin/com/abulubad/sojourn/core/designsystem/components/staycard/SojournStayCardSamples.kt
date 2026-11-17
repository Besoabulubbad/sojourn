package com.abulubad.sojourn.core.designsystem.components.staycard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/** The StayCard card's examples: the canal studio with a dated total, and the compact map card. */
object SojournStayCardSamples {

    /** The full card: long-stay badge, three photos, saved, "£1,383 total". */
    @Composable
    fun Full(modifier: Modifier = Modifier) = WithSamplePhotos {
        SojournStayCard(
            stay = SampleListings.canalStudio,
            price = SampleListings.canalTotal,
            saved = true,
            onClick = {},
            onToggleSave = {},
            modifier = modifier.padding(SojournSpacing.space4),
        )
    }

    /** The compact map card, with an Arabic host title in either UI language (S-06). */
    @Composable
    fun Compact(modifier: Modifier = Modifier) = WithSamplePhotos {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournStayCard(
                SampleListings.canalStudio,
                SampleListings.canalTotal,
                saved = false,
                onClick = {},
                onToggleSave = {},
                layout = SojournStayCardLayout.Compact,
            )
            SojournStayCard(
                SampleListings.hydraFlat,
                SampleListings.hydraFlat.fromPrice,
                saved = false,
                onClick = {},
                onToggleSave = {},
                layout = SojournStayCardLayout.Compact,
            )
        }
    }
}
