package com.abulubad.sojourn.feature.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.reminder.SojournReminderCard
import com.abulubad.sojourn.core.designsystem.components.reminder.SojournReminderState
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetDetent
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHost
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournSnackbarHostState
import com.abulubad.sojourn.core.designsystem.components.topbar.SojournTopBar
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.Refund
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_back
import com.abulubad.sojourn.core.localization.resources.action_cancel_booking
import com.abulubad.sojourn.core.localization.resources.action_keep_booking
import com.abulubad.sojourn.core.localization.resources.booking_cancelled_note
import com.abulubad.sojourn.core.localization.resources.cancel_refund_full
import com.abulubad.sojourn.core.localization.resources.cancel_refund_none
import com.abulubad.sojourn.core.localization.resources.cancel_refund_partial
import com.abulubad.sojourn.core.localization.resources.cancel_title
import com.abulubad.sojourn.core.localization.resources.confirmed_total_paid
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.localization.resources.state_offline_title
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A booking's page: the booking card (countdown, place, title, dates and guests, reference, "Directions" and
 * "Check-in guide"), the total paid, the stay's terms, and "Cancel booking" before check-in. A cancelled booking says
 * so; its nights are free again. Built from the ScreenBookings card's parts: the card does not design this page.
 */
@Composable
fun BookingDetailsScreen(
    state: BookingDetailsUiState,
    onIntent: (BookingDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
    guideSheet: SojournSheetState = rememberHalfSheetState(),
    cancelSheet: SojournSheetState = rememberHalfSheetState(),
    snackbar: SojournSnackbarHostState = SojournSnackbarHostState(),
    reminder: SojournReminderState? = null,
    onRemind: () -> Unit = {},
) {
    SojournScaffold(
        modifier,
        topBar = {
            SojournTopBar(title = state.item?.stay?.title?.text, onBack = { onIntent(BookingDetailsIntent.BackTapped) })
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when (val booking = state.booking) {
                is ScreenState.Content -> Page(booking.data, state, onIntent, padding, reminder, onRemind)
                ScreenState.Loading -> Unit
                else -> Missing(onIntent, padding)
            }
            SojournSnackbarHost(
                snackbar,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = padding.calculateBottomPadding() + SojournSpacing.space3)
                    .padding(horizontal = SojournSpacing.space3),
            )
        }
    }
    val item = state.item ?: return
    when (val sheet = state.sheet) {
        BookingSheet.Guide -> GuideSheet(
            item = item,
            today = state.today,
            sheetState = guideSheet,
            onCopy = { text, what -> onIntent(BookingDetailsIntent.CopyTapped(text, what)) },
            onDismiss = { onIntent(BookingDetailsIntent.SheetDismissed) },
        )
        is BookingSheet.Cancel -> CancelSheet(state, sheet, cancelSheet, onIntent)
        null -> Unit
    }
}

@Composable
private fun Page(
    item: BookingItem,
    state: BookingDetailsUiState,
    onIntent: (BookingDetailsIntent) -> Unit,
    padding: PaddingValues,
    reminder: SojournReminderState?,
    onRemind: () -> Unit,
) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        if (item.booking.status == BookingStatus.Cancelled) {
            BasicText(
                stringResource(Res.string.booking_cancelled_note),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SojournShapes.md)
                    .background(c.dangerTint)
                    .padding(SojournSpacing.space3),
                style = SojournTheme.type.bodySm.copy(color = c.danger),
            )
        }
        BookingCard(
            item = item,
            today = state.today,
            onDirections = { onIntent(BookingDetailsIntent.DirectionsTapped) },
            onGuide = { onIntent(BookingDetailsIntent.GuideTapped) },
        )
        // The address and the terms matter while the stay is still to come; a past or cancelled booking is a record.
        val ongoing = item.booking.status == BookingStatus.Upcoming || item.booking.status == BookingStatus.Active
        if (ongoing) {
            BookingAddress(
                item = item,
                onCopy = { text, what -> onIntent(BookingDetailsIntent.CopyTapped(text, what)) },
                onAddToCalendar = { onIntent(BookingDetailsIntent.AddToCalendarTapped(it)) },
            )
        }
        if (item.booking.status == BookingStatus.Upcoming) reminder?.let { SojournReminderCard(it, onRemind) }
        TotalPaid(stringResource(Res.string.confirmed_total_paid), formatter.money(item.booking.total))
        if (ongoing) BookingPolicies(item)
        if (state.canCancel) {
            SojournButton(
                stringResource(Res.string.action_cancel_booking),
                onClick = { onIntent(BookingDetailsIntent.CancelTapped) },
                modifier = Modifier.fillMaxWidth(),
                variant = SojournButtonVariant.Secondary,
            )
        }
    }
}

/** "Total paid  £1,383", read as one. */
@Composable
private fun TotalPaid(label: String, value: String) {
    val c = SojournTheme.colors
    Row(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BasicText(label, style = SojournTheme.type.body.copy(color = c.inkMuted))
        BasicText(value, style = SojournTheme.type.body.copy(color = c.ink))
    }
}

/**
 * "Cancel this booking?": what comes back under the stay's policy, then "Cancel booking" (it acts at once, so it is
 * the danger button) and "Keep booking". A failure keeps the sheet open with the reason.
 */
@Composable
private fun CancelSheet(
    state: BookingDetailsUiState,
    sheet: BookingSheet.Cancel,
    sheetState: SojournSheetState,
    onIntent: (BookingDetailsIntent) -> Unit,
) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val refund = when (val owed = state.refund) {
        is Refund.Full -> stringResource(Res.string.cancel_refund_full, formatter.money(owed.amount))
        is Refund.Partial -> stringResource(
            Res.string.cancel_refund_partial,
            formatter.money(owed.amount),
            pluralStringResource(Res.plurals.nights, owed.keptNights, formatter.count(owed.keptNights)),
        )
        Refund.None, null -> stringResource(Res.string.cancel_refund_none)
    }
    SojournSheet(
        state = sheetState,
        title = stringResource(Res.string.cancel_title),
        onDismiss = { onIntent(BookingDetailsIntent.SheetDismissed) },
        footer = {
            Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
                SojournButton(
                    stringResource(Res.string.action_cancel_booking),
                    onClick = { onIntent(BookingDetailsIntent.CancelConfirmed) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = SojournButtonVariant.Danger,
                    loading = sheet.cancelling,
                )
                SojournButton(
                    stringResource(Res.string.action_keep_booking),
                    onClick = { onIntent(BookingDetailsIntent.SheetDismissed) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = SojournButtonVariant.Secondary,
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
            BasicText(refund, style = SojournTheme.type.body.copy(color = c.ink))
            sheet.error?.let { error ->
                val reason = if (error == AppError.Offline) {
                    Res.string.state_offline_title
                } else {
                    Res.string.state_error_body
                }
                BasicText(
                    stringResource(reason),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = SojournTheme.type.bodySm.copy(color = c.danger),
                )
            }
        }
    }
}

@Composable
private fun Missing(onIntent: (BookingDetailsIntent) -> Unit, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding).padding(SojournSpacing.space4)) {
        SojournEmptyState(
            icon = SojournIcon.Alert,
            title = stringResource(Res.string.state_error_title),
            body = stringResource(Res.string.state_error_body),
            action = SojournEmptyStateAction(stringResource(Res.string.action_back)) {
                onIntent(BookingDetailsIntent.BackTapped)
            },
        )
    }
}

/** The guide and "Cancel this booking?" are a few lines: half height, with the booking still in view. */
@Composable
internal fun rememberHalfSheetState(): SojournSheetState = rememberSojournSheetState(
    SojournSheetDetent.Half,
    setOf(SojournSheetDetent.Half, SojournSheetDetent.Full),
)
