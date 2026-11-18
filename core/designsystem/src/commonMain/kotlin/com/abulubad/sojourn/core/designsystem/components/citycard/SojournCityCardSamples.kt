package com.abulubad.sojourn.core.designsystem.components.citycard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/** The CityCard card's London and Paris, as on Explore. */
object SojournCityCardSamples {

    @Composable
    fun Cities(modifier: Modifier = Modifier) = WithSamplePhotos {
        Row(
            modifier.padding(SojournSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
            verticalAlignment = Alignment.Top,
        ) {
            val width = Modifier.width(SojournCityCardDefaults.Width)
            SojournCityCard(SampleListings.london, onClick = {}, modifier = width)
            SojournCityCard(SampleListings.paris, onClick = {}, modifier = width)
        }
    }

    /** The search flow's Where list: London and Paris as rows. */
    @Composable
    fun Rows(modifier: Modifier = Modifier) = WithSamplePhotos {
        Column(modifier.padding(SojournSpacing.space4)) {
            SojournCityRow(SampleListings.london, onClick = {})
            SojournCityRow(SampleListings.paris, onClick = {})
        }
    }
}
