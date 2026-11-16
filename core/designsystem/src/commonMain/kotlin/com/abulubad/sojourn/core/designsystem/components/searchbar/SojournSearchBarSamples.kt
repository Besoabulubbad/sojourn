package com.abulubad.sojourn.core.designsystem.components.searchbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButton
import com.abulubad.sojourn.core.designsystem.components.iconbutton.SojournIconButtonVariant
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.cd_filters
import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SearchQuery
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** The SearchBar card's examples: the Explore prompt, and the Results summary beside the filter button. */
object SojournSearchBarSamples {

    /** London, 12–27 March 2027, two guests: the card's summary. */
    val London = SearchQuery(
        cityId = CityId("london"),
        dates = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 3, 27)),
        guests = Guests(adults = 2),
    )

    private const val ACTIVE_FILTERS = 2

    @Composable
    fun Forms(modifier: Modifier = Modifier) {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5),
        ) {
            SojournSearchBar(query = null, onClick = {})
            Row(
                horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SojournSearchBar(query = London, onClick = {}, modifier = Modifier.weight(1f), onBack = {})
                SojournIconButton(
                    SojournIcon.Filters,
                    stringResource(Res.string.cd_filters),
                    onClick = {},
                    variant = SojournIconButtonVariant.Outline,
                    badge = ACTIVE_FILTERS,
                )
            }
        }
    }
}
