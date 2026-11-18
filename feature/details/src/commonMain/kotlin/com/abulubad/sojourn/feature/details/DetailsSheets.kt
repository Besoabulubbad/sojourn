package com.abulubad.sojourn.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.components.breakdown.SojournPriceBreakdown
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.daterange.SojournDateRangePicker
import com.abulubad.sojourn.core.designsystem.components.daterange.SojournDayState
import com.abulubad.sojourn.core.designsystem.components.price.SojournPrice
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.StayRules
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_choose_dates
import com.abulubad.sojourn.core.localization.resources.details_dates_apply
import com.abulubad.sojourn.core.localization.resources.details_from_price_note
import com.abulubad.sojourn.core.localization.resources.guests
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.price_details
import com.abulubad.sojourn.core.localization.resources.problem_dates_in_past
import com.abulubad.sojourn.core.localization.resources.problem_minimum_nights
import com.abulubad.sojourn.core.localization.resources.problem_too_many_guests
import com.abulubad.sojourn.core.localization.resources.problem_unavailable
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_offline_title
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.ui.ScreenState
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The sheet the state has open, drawn over the page. */
@Composable
internal fun Sheets(
    state: DetailsUiState,
    onIntent: (DetailsIntent) -> Unit,
    datesSheet: SojournSheetState,
    priceSheet: SojournSheetState,
) {
    when (val sheet = state.sheet) {
        is DetailsSheet.Dates -> DatesSheet(state, sheet, onIntent, datesSheet)
        DetailsSheet.PriceDetails -> PriceSheet(state, onIntent, priceSheet)
        null -> Unit
    }
}

/**
 * "Choose dates": booked nights and past days are unavailable, the stay's minimum stay applies, and "See the total"
 * prices the selection. A problem the device or the API finds is said in the stay's own numbers.
 */
@Composable
private fun DatesSheet(
    state: DetailsUiState,
    sheet: DetailsSheet.Dates,
    onIntent: (DetailsIntent) -> Unit,
    sheetState: SojournSheetState,
) {
    val stay = state.stay ?: return
    val blocked = when (val details = state.details) {
        is ScreenState.Content -> details.data.blocked
        is ScreenState.Offline -> details.cached?.blocked.orEmpty()
        else -> emptyList()
    }
    SojournSheet(
        state = sheetState,
        title = stringResource(Res.string.action_choose_dates),
        onDismiss = { onIntent(DetailsIntent.SheetDismissed) },
        footer = {
            SojournButton(
                stringResource(Res.string.details_dates_apply),
                onClick = { onIntent(DetailsIntent.DatesConfirmed) },
                enabled = sheet.selection != null,
                loading = sheet.quoting,
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
            SojournDateRangePicker(
                month = sheet.month,
                onMonthChange = { onIntent(DetailsIntent.MonthShown(it)) },
                selection = sheet.selection,
                onSelect = { onIntent(DetailsIntent.DatesSelected(it)) },
                availability = { day ->
                    val booked = blocked.any { day >= it.start && day < it.end }
                    if (day < state.today || booked) SojournDayState.Unavailable else SojournDayState.Available
                },
                minNights = StayRules.minimumNights(stay),
            )
            sheet.problem?.let { problem ->
                BasicText(
                    problemText(problem),
                    style = SojournTheme.type.bodySm.copy(color = SojournTheme.colors.danger),
                )
            }
        }
    }
}

@Composable
private fun problemText(problem: DatesProblem): String {
    val formatter = rememberSojournFormatter()
    return when (problem) {
        is DatesProblem.MinimumNights -> stringResource(
            Res.string.problem_minimum_nights,
            pluralStringResource(Res.plurals.nights, problem.nights, formatter.count(problem.nights)),
        )
        is DatesProblem.TooManyGuests -> stringResource(
            Res.string.problem_too_many_guests,
            pluralStringResource(Res.plurals.guests, problem.sleeps, formatter.count(problem.sleeps)),
        )
        DatesProblem.Unavailable -> stringResource(Res.string.problem_unavailable)
        DatesProblem.InThePast -> stringResource(Res.string.problem_dates_in_past)
        is DatesProblem.Failed -> stringResource(
            if (problem.error == AppError.Offline) Res.string.state_offline_title else Res.string.state_error_body,
        )
    }
}

/**
 * "Price details": with dates, the quote's breakdown, whose lines sum to the total on the bar (P-05), and the
 * deposit held apart from it; without, the "from" price with what it leaves out, and how to see a total.
 */
@Composable
private fun PriceSheet(state: DetailsUiState, onIntent: (DetailsIntent) -> Unit, sheetState: SojournSheetState) {
    val quote = state.quote
    val price = state.price ?: return
    SojournSheet(
        state = sheetState,
        title = stringResource(Res.string.price_details),
        onDismiss = { onIntent(DetailsIntent.SheetDismissed) },
    ) {
        if (quote != null) {
            val deposit = state.stay?.policies?.filterIsInstance<Policy.Deposit>()?.firstOrNull()?.amount
            SojournPriceBreakdown(quote.breakdown, deposit = deposit)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
                SojournPrice(price)
                BasicText(
                    stringResource(Res.string.details_from_price_note),
                    style = SojournTheme.type.body.copy(color = SojournTheme.colors.inkMuted),
                )
            }
        }
    }
}
