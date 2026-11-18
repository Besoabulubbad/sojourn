package com.abulubad.sojourn.core.designsystem.components.staycard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.priced_hours_ago
import org.jetbrains.compose.resources.pluralStringResource

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

    /** Offline: the cached total with its age under it ("Priced 3 hours ago"), as Results shows it. */
    @Composable
    fun Stale(modifier: Modifier = Modifier) = WithSamplePhotos {
        val formatter = rememberSojournFormatter()
        SojournStayCard(
            stay = SampleListings.canalStudio,
            price = SampleListings.canalTotal,
            saved = false,
            onClick = {},
            onToggleSave = {},
            modifier = modifier.padding(SojournSpacing.space4),
            priceNote = pluralStringResource(Res.plurals.priced_hours_ago, STALE_HOURS, formatter.count(STALE_HOURS)),
        )
    }

    private const val STALE_HOURS = 3

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
