package com.abulubad.sojourn.core.designsystem.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.stepper.SojournStepper
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_clear_filters
import com.abulubad.sojourn.core.localization.resources.action_show_stays
import com.abulubad.sojourn.core.localization.resources.filters_title
import com.abulubad.sojourn.core.localization.resources.guests_adults
import com.abulubad.sojourn.core.localization.resources.guests_children
import com.abulubad.sojourn.core.localization.resources.guests_children_hint
import com.abulubad.sojourn.core.localization.resources.stays
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The Sheet card's examples: a modal filters sheet at half height, and the map's results sheet at peek. */
object SojournSheetSamples {

    /** Filters over `scrim`, with the footer pinned: "Clear filters" and "Show 14 stays". */
    @Composable
    fun Filters(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        val count = pluralStringResource(Res.plurals.stays, SAMPLE_STAYS, formatter.count(SAMPLE_STAYS))
        Box(modifier.fillMaxWidth().height(FILTERS_HEIGHT).background(SojournTheme.colors.bg)) {
            SojournSheet(
                state = rememberSojournSheetState(
                    SojournSheetDetent.Half,
                    setOf(SojournSheetDetent.Half, SojournSheetDetent.Full),
                ),
                title = stringResource(Res.string.filters_title),
                onDismiss = {},
                footer = {
                    SojournButton(
                        stringResource(Res.string.action_clear_filters),
                        onClick = {},
                        variant = SojournButtonVariant.Ghost,
                    )
                    SojournButton(stringResource(Res.string.action_show_stays, count), onClick = {})
                },
            ) {
                var adults by remember { mutableIntStateOf(2) }
                var children by remember { mutableIntStateOf(0) }
                SojournStepper(adults, { adults = it }, ADULTS, stringResource(Res.string.guests_adults))
                SojournStepper(
                    children,
                    { children = it },
                    CHILDREN,
                    stringResource(Res.string.guests_children),
                    hint = stringResource(Res.string.guests_children_hint),
                )
            }
        }
    }

    /** The map's results sheet, not modal, resting at peek with the result count as its title. */
    @Composable
    fun Peek(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        Box(modifier.fillMaxWidth().height(PEEK_HEIGHT).background(SojournTheme.colors.brandTint)) {
            SojournSheet(
                state = rememberSojournSheetState(
                    SojournSheetDetent.Peek,
                    setOf(SojournSheetDetent.Peek, SojournSheetDetent.Half, SojournSheetDetent.Full),
                ),
                title = pluralStringResource(Res.plurals.stays, SAMPLE_STAYS, formatter.count(SAMPLE_STAYS)),
                onDismiss = {},
                modal = false,
            ) {}
        }
    }

    private const val SAMPLE_STAYS = 14
    private val ADULTS = 1..16
    private val CHILDREN = 0..10
    private val FILTERS_HEIGHT = 560.dp
    private val PEEK_HEIGHT = 240.dp
}
