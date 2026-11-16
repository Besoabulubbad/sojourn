package com.abulubad.sojourn.core.designsystem.components.viewtoggle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

/** The ViewToggle card's pills: "Map" on the list, "List" on the map, and "Search this area". */
object SojournViewToggleSamples {

    @Composable
    fun Pills(modifier: Modifier = Modifier) {
        Column(
            modifier.fillMaxWidth().padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SojournViewToggle(SojournResultsView.List, onToggle = {})
            SojournViewToggle(SojournResultsView.Map, onToggle = {})
            SojournSearchAreaPill(onClick = {})
        }
    }
}
