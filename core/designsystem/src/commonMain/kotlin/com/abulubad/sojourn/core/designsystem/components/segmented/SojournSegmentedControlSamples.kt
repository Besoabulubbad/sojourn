package com.abulubad.sojourn.core.designsystem.components.segmented

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.facts_studio
import com.abulubad.sojourn.core.localization.resources.filter_any
import com.abulubad.sojourn.core.localization.resources.search_mode_dates
import com.abulubad.sojourn.core.localization.resources.search_mode_flexible
import org.jetbrains.compose.resources.stringResource

/** The SegmentedControl card's examples: the search mode, and minimum bedrooms with "Studio" and "3+". */
object SojournSegmentedControlSamples {

    private const val STUDIO = 0
    private const val THREE_PLUS = 3

    @Composable
    fun Rows(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        var flexible by remember { mutableStateOf(false) }
        var bedrooms by remember { mutableStateOf<Int?>(STUDIO) }
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            SojournSegmentedControl(
                options = listOf(
                    SojournSegment(false, stringResource(Res.string.search_mode_dates)),
                    SojournSegment(true, stringResource(Res.string.search_mode_flexible)),
                ),
                selected = flexible,
                onSelect = { flexible = it },
            )
            SojournSegmentedControl(
                options = listOf(
                    SojournSegment(null, stringResource(Res.string.filter_any)),
                    SojournSegment(STUDIO, stringResource(Res.string.facts_studio)),
                    SojournSegment(1, formatter.count(1)),
                    SojournSegment(2, formatter.count(2)),
                    SojournSegment(THREE_PLUS, formatter.plus(THREE_PLUS)),
                ),
                selected = bedrooms,
                onSelect = { bedrooms = it },
            )
        }
    }
}
