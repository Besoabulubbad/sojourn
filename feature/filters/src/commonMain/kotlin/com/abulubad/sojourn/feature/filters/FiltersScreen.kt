package com.abulubad.sojourn.feature.filters

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.amenity.icon
import com.abulubad.sojourn.core.designsystem.amenity.label
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.chip.SojournChip
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDivider
import com.abulubad.sojourn.core.designsystem.components.pricerange.SojournPriceRange
import com.abulubad.sojourn.core.designsystem.components.segmented.SojournSegment
import com.abulubad.sojourn.core.designsystem.components.segmented.SojournSegmentedControl
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.components.stepper.SojournStepper
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.theme.enter
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_show_stays
import com.abulubad.sojourn.core.localization.resources.facts_studio
import com.abulubad.sojourn.core.localization.resources.filter_any
import com.abulubad.sojourn.core.localization.resources.filter_free_cancellation
import com.abulubad.sojourn.core.localization.resources.filters_amenities
import com.abulubad.sojourn.core.localization.resources.filters_bathrooms
import com.abulubad.sojourn.core.localization.resources.filters_bedrooms
import com.abulubad.sojourn.core.localization.resources.filters_bedrooms_or_more
import com.abulubad.sojourn.core.localization.resources.filters_beds
import com.abulubad.sojourn.core.localization.resources.filters_booking
import com.abulubad.sojourn.core.localization.resources.filters_clear_all
import com.abulubad.sojourn.core.localization.resources.filters_instant_book
import com.abulubad.sojourn.core.localization.resources.filters_no_match
import com.abulubad.sojourn.core.localization.resources.filters_show_stays_uncounted
import com.abulubad.sojourn.core.localization.resources.filters_title
import com.abulubad.sojourn.core.localization.resources.stays
import com.abulubad.sojourn.core.model.Amenity
import com.abulubad.sojourn.core.model.BedroomChoice
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The filter sheet (ScreenFilters card), drawn over Results by the app's overlay scene:
 *
 * - The price label states the cards' basis ("Total price for 15 nights", "Price per month"), and the histogram
 *   shows where the stays the other filters keep sit (P-04).
 * - Bedrooms is exact: Any, Studio, 1, 2, 3+; never "0 bedrooms" (N-01). Beds and bathrooms are minimums.
 * - Every amenity, then Booking: free cancellation and instant book.
 * - "Show 14 stays" carries the live count, crossfading as it changes; at zero it reads "No stays match" and is
 *   disabled. "Clear all" resets the sheet without closing it.
 */
@Composable
fun FiltersScreen(
    state: FiltersUiState,
    onIntent: (FiltersIntent) -> Unit,
    modifier: Modifier = Modifier,
    sheet: SojournSheetState = rememberSojournSheetState(),
) {
    SojournSheet(
        state = sheet,
        title = stringResource(Res.string.filters_title),
        onDismiss = { onIntent(FiltersIntent.Dismissed) },
        modifier = modifier,
        footer = { Footer(state.apply, onIntent) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5)) {
            Price(state, onIntent)
            Rooms(state, onIntent)
            SojournDivider()
            Amenities(state, onIntent)
            SojournDivider()
            Booking(state, onIntent)
        }
    }
}

@Composable
private fun Price(state: FiltersUiState, onIntent: (FiltersIntent) -> Unit) {
    val preview = state.preview ?: return
    val prices = state.prices ?: return
    val range = state.shownRange ?: return
    SojournPriceRange(
        range = range,
        bounds = prices.bounds,
        histogram = prices.histogram,
        basis = preview.basis,
        onChange = { onIntent(FiltersIntent.PriceChanged(it)) },
        nights = state.nights,
    )
    SojournDivider()
}

@Composable
private fun Rooms(state: FiltersUiState, onIntent: (FiltersIntent) -> Unit) {
    Section(Res.string.filters_bedrooms) {
        val formatter = rememberSojournFormatter()
        val options = listOf<SojournSegment<BedroomChoice?>>(
            SojournSegment(null, stringResource(Res.string.filter_any)),
            SojournSegment(BedroomChoice.Studio, stringResource(Res.string.facts_studio)),
            SojournSegment(BedroomChoice.One, formatter.count(1)),
            SojournSegment(BedroomChoice.Two, formatter.count(2)),
            SojournSegment(
                BedroomChoice.ThreeOrMore,
                stringResource(Res.string.filters_bedrooms_or_more, formatter.count(THREE)),
            ),
        )
        SojournSegmentedControl(options, state.draft.bedrooms, { onIntent(FiltersIntent.BedroomsSelected(it)) })
        SojournStepper(
            value = state.draft.minBeds ?: 1,
            onValueChange = { onIntent(FiltersIntent.BedsChanged(it)) },
            range = 1..MAX_ROOMS,
            label = stringResource(Res.string.filters_beds),
        )
        SojournStepper(
            value = state.draft.minBathrooms ?: 1,
            onValueChange = { onIntent(FiltersIntent.BathroomsChanged(it)) },
            range = 1..MAX_ROOMS,
            label = stringResource(Res.string.filters_bathrooms),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Amenities(state: FiltersUiState, onIntent: (FiltersIntent) -> Unit) {
    Section(Res.string.filters_amenities) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        ) {
            Amenity.entries.forEach { amenity ->
                SojournChip(
                    label = stringResource(amenity.label),
                    selected = amenity in state.draft.amenities,
                    onClick = { onIntent(FiltersIntent.AmenityTapped(amenity)) },
                    icon = amenity.icon,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Booking(state: FiltersUiState, onIntent: (FiltersIntent) -> Unit) {
    Section(Res.string.filters_booking) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
            SojournChip(
                label = stringResource(Res.string.filter_free_cancellation),
                selected = state.draft.freeCancellation,
                onClick = { onIntent(FiltersIntent.FreeCancellationTapped) },
            )
            SojournChip(
                label = stringResource(Res.string.filters_instant_book),
                selected = state.draft.instantBook,
                onClick = { onIntent(FiltersIntent.InstantBookTapped) },
            )
        }
    }
}

@Composable
private fun Section(
    title: StringResource,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
        BasicText(
            stringResource(title),
            modifier = Modifier.semantics { heading() },
            style = SojournTheme.type.titleSm.copy(color = SojournTheme.colors.ink),
        )
        content()
    }
}

@Composable
private fun Footer(apply: ApplyButton, onIntent: (FiltersIntent) -> Unit) {
    // One child of the sheet's footer: "Clear all" at its own width, the apply button taking the rest (the card's
    // link and wide button), so "Show 14 stays" stays on one line.
    Row(
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SojournButton(
            stringResource(Res.string.filters_clear_all),
            onClick = { onIntent(FiltersIntent.ClearAllTapped) },
            variant = SojournButtonVariant.Ghost,
        )
        val formatter = rememberSojournFormatter()
        Crossfade(
            apply,
            modifier = Modifier.weight(1f),
            animationSpec = SojournMotion.enter(SojournMotion.fast),
            label = "apply",
        ) { shown ->
            val text = when (shown) {
                is ApplyButton.Count -> stringResource(
                    Res.string.action_show_stays,
                    pluralStringResource(Res.plurals.stays, shown.stays, formatter.count(shown.stays)),
                )
                ApplyButton.NoMatch -> stringResource(Res.string.filters_no_match)
                ApplyButton.Uncounted -> stringResource(Res.string.filters_show_stays_uncounted)
            }
            SojournButton(
                text,
                onClick = { onIntent(FiltersIntent.ApplyTapped) },
                modifier = Modifier.fillMaxWidth(),
                enabled = shown != ApplyButton.NoMatch,
            )
        }
    }
}

private const val THREE = 3

/** The steppers' top: more beds or bathrooms than this is not a filter anyone needs. */
private const val MAX_ROOMS = 6
