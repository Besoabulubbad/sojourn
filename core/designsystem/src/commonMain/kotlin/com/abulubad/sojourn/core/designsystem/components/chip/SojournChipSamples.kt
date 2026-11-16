package com.abulubad.sojourn.core.designsystem.components.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.amenity_washer
import com.abulubad.sojourn.core.localization.resources.amenity_wifi
import com.abulubad.sojourn.core.localization.resources.amenity_workspace
import com.abulubad.sojourn.core.localization.resources.facts_studio
import org.jetbrains.compose.resources.stringResource

/** The Chip card's example row: selected, unselected, with an icon, and a read-only fact. */
object SojournChipSamples {

    @Composable
    fun Row(modifier: Modifier = Modifier) {
        FlowRow(
            modifier = modifier.padding(SojournSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        ) {
            SojournChip(stringResource(Res.string.facts_studio), selected = true, onClick = {})
            SojournChip(stringResource(Res.string.amenity_workspace), selected = false, onClick = {})
            SojournChip(
                stringResource(Res.string.amenity_washer),
                selected = false,
                onClick = {},
                icon = SojournIcon.Washer,
            )
            SojournFactChip(stringResource(Res.string.amenity_wifi), icon = SojournIcon.Check)
        }
    }
}
