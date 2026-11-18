package com.abulubad.sojourn.core.designsystem.components.staysummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedPlace

/** The card's canal studio with its rating, and the Arabic-titled flat in Hydra without one. */
object SojournStaySummarySamples {

    @Composable
    fun Stays(modifier: Modifier = Modifier) = WithSamplePhotos {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5),
        ) {
            listOf(SampleListings.canalStudio, SampleListings.hydraFlat.copy(rating = null)).forEach { stay ->
                SojournStaySummary(
                    photo = stay.photos.first(),
                    title = stay.title,
                    place = localizedPlace(stay.cityId, stay.areaKey),
                    rating = stay.rating,
                )
            }
        }
    }
}
