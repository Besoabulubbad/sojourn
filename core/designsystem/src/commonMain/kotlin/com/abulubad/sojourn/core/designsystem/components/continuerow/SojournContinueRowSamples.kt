package com.abulubad.sojourn.core.designsystem.components.continuerow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.internal.WithSamplePhotos
import com.abulubad.sojourn.core.designsystem.components.searchbar.searchSummaryText
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedCityName
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import kotlinx.datetime.LocalDate

/** The last search in London (dated) and an undated month in Paris, as Explore shows them. */
object SojournContinueRowSamples {

    @Composable
    fun Searches(modifier: Modifier = Modifier) = WithSamplePhotos {
        val london = SearchQuery(
            cityId = SampleListings.london.id,
            dates = DateRange(LocalDate.parse("2026-03-12"), LocalDate.parse("2026-03-27")),
            guests = Guests(adults = 2),
        )
        val paris = SearchQuery(cityId = SampleListings.paris.id, length = StayLength.Month)
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournContinueRow(
                photo = SampleListings.london.photo,
                title = localizedCityName(SampleListings.london.id),
                subtitle = searchSummaryText(london),
                onClick = {},
            )
            SojournContinueRow(
                photo = SampleListings.paris.photo,
                title = localizedCityName(SampleListings.paris.id),
                subtitle = searchSummaryText(paris),
                onClick = {},
            )
        }
    }
}
