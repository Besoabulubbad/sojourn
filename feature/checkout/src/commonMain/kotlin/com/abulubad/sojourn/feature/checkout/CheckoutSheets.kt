package com.abulubad.sojourn.feature.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.daterange.SojournDateRangePicker
import com.abulubad.sojourn.core.designsystem.components.daterange.SojournDayState
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetDetent
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.components.stepper.SojournStepper
import com.abulubad.sojourn.core.designsystem.components.wallet.SojournWalletSheet
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.StayRules
import com.abulubad.sojourn.core.localization.quoteProblemText
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_choose_dates
import com.abulubad.sojourn.core.localization.resources.action_pay
import com.abulubad.sojourn.core.localization.resources.checkout_guests
import com.abulubad.sojourn.core.localization.resources.checkout_update_total
import com.abulubad.sojourn.core.localization.resources.guests
import com.abulubad.sojourn.core.localization.resources.guests_adults
import com.abulubad.sojourn.core.localization.resources.guests_children
import com.abulubad.sojourn.core.localization.resources.guests_children_hint
import com.abulubad.sojourn.core.localization.resources.problem_too_many_guests
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.PriceDisplay
import com.abulubad.sojourn.core.model.QuoteProblem
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The editor the state has open, drawn over the page. */
@Composable
internal fun Sheets(
    state: CheckoutUiState,
    onIntent: (CheckoutIntent) -> Unit,
    datesSheet: SojournSheetState,
    guestsSheet: SojournSheetState,
) {
    when (val sheet = state.sheet) {
        is CheckoutSheet.EditDates -> DatesSheet(state, sheet, onIntent, datesSheet)
        is CheckoutSheet.EditGuests -> GuestsSheet(state, sheet, onIntent, guestsSheet)
        null -> Unit
    }
}

/** The guests editor is two steppers: it opens at half height, with the page still in view above it. */
@Composable
internal fun rememberGuestsSheetState(): SojournSheetState = rememberSojournSheetState(
    SojournSheetDetent.Half,
    setOf(SojournSheetDetent.Half, SojournSheetDetent.Full),
)

/** New dates: booked nights and past days are unavailable and the minimum stay applies, as on Details. */
@Composable
private fun DatesSheet(
    state: CheckoutUiState,
    sheet: CheckoutSheet.EditDates,
    onIntent: (CheckoutIntent) -> Unit,
    sheetState: SojournSheetState,
) {
    val stay = state.stay ?: return
    val blocked = state.blocked
    EditorSheet(
        title = stringResource(Res.string.action_choose_dates),
        sheet = sheet,
        canConfirm = sheet.selection != null,
        onIntent = onIntent,
        sheetState = sheetState,
    ) {
        SojournDateRangePicker(
            month = sheet.month,
            onMonthChange = { onIntent(CheckoutIntent.MonthShown(it)) },
            selection = sheet.selection,
            onSelect = { onIntent(CheckoutIntent.DatesSelected(it)) },
            availability = { day ->
                val booked = blocked.any { day >= it.start && day < it.end }
                if (day < state.today || booked) SojournDayState.Unavailable else SojournDayState.Available
            },
            minNights = StayRules.minimumNights(stay),
        )
    }
}

/** Adults and children, kept within what the stay sleeps, so the steppers cannot ask for too many. */
@Composable
private fun GuestsSheet(
    state: CheckoutUiState,
    sheet: CheckoutSheet.EditGuests,
    onIntent: (CheckoutIntent) -> Unit,
    sheetState: SojournSheetState,
) {
    val sleeps = state.stay?.facts?.sleeps ?: return
    val guests = sheet.guests
    val formatter = rememberSojournFormatter()
    EditorSheet(
        title = stringResource(Res.string.checkout_guests),
        sheet = sheet,
        canConfirm = true,
        onIntent = onIntent,
        sheetState = sheetState,
    ) {
        SojournStepper(
            value = guests.adults,
            onValueChange = { onIntent(CheckoutIntent.GuestsChanged(guests.copy(adults = it))) },
            range = 1..(sleeps - guests.children).coerceAtLeast(1),
            label = stringResource(Res.string.guests_adults),
        )
        SojournStepper(
            value = guests.children,
            onValueChange = { onIntent(CheckoutIntent.GuestsChanged(Guests(guests.adults, it))) },
            range = 0..(sleeps - guests.adults).coerceAtLeast(0),
            label = stringResource(Res.string.guests_children),
            hint = stringResource(Res.string.guests_children_hint),
        )
        BasicText(
            stringResource(
                Res.string.problem_too_many_guests,
                pluralStringResource(Res.plurals.guests, sleeps, formatter.count(sleeps)),
            ),
            style = SojournTheme.type.bodySm.copy(color = SojournTheme.colors.inkMuted),
        )
    }
}

/** An editor's frame: its content, the reason the last change could not be priced, and "Update the total". */
@Composable
private fun EditorSheet(
    title: String,
    sheet: CheckoutSheet,
    canConfirm: Boolean,
    onIntent: (CheckoutIntent) -> Unit,
    sheetState: SojournSheetState,
    content: @Composable () -> Unit,
) {
    SojournSheet(
        state = sheetState,
        title = title,
        onDismiss = { onIntent(CheckoutIntent.SheetDismissed) },
        footer = {
            SojournButton(
                stringResource(Res.string.checkout_update_total),
                onClick = { onIntent(CheckoutIntent.EditConfirmed) },
                enabled = canConfirm,
                loading = sheet.quoting,
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
            content()
            sheet.problem?.let { ProblemText(it) }
        }
    }
}

@Composable
private fun ProblemText(problem: QuoteProblem) {
    BasicText(
        quoteProblemText(problem),
        style = SojournTheme.type.bodySm.copy(color = SojournTheme.colors.danger),
    )
}

/** The demo wallet opens at full height: everything it says is read before "Pay". */
@Composable
internal fun rememberWalletSheetState(): SojournSheetState =
    rememberSojournSheetState(SojournSheetDetent.Full, setOf(SojournSheetDetent.Full))

/** The demo wallet for the quote's total, with the same "Pay £1,383" as the page. */
@Composable
internal fun WalletSheet(state: CheckoutUiState, onIntent: (CheckoutIntent) -> Unit, sheetState: SojournSheetState) {
    val quote = state.quote
    if (!state.wallet || quote == null) return
    val formatter = rememberSojournFormatter()
    SojournWalletSheet(
        state = sheetState,
        price = PriceDisplay.Total(quote.id, quote.total, quote.dates.nights),
        payLabel = stringResource(Res.string.action_pay, formatter.money(quote.total)),
        onPay = { onIntent(CheckoutIntent.WalletConfirmed) },
        onDismiss = { onIntent(CheckoutIntent.WalletDismissed) },
    )
}
