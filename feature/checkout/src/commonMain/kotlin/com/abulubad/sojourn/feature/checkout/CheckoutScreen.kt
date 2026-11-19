package com.abulubad.sojourn.feature.checkout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.designsystem.components.actionbar.SojournActionBar
import com.abulubad.sojourn.core.designsystem.components.breakdown.SojournPriceBreakdown
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDivider
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.policy.SojournPolicyRow
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.components.skeleton.SojournSkeleton
import com.abulubad.sojourn.core.designsystem.components.snackbar.SojournOfflineBanner
import com.abulubad.sojourn.core.designsystem.components.staysummary.SojournStaySummary
import com.abulubad.sojourn.core.designsystem.components.topbar.SojournTopBar
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.StayDetails
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_back
import com.abulubad.sojourn.core.localization.resources.action_edit
import com.abulubad.sojourn.core.localization.resources.action_pay
import com.abulubad.sojourn.core.localization.resources.action_retry
import com.abulubad.sojourn.core.localization.resources.cd_edit_dates
import com.abulubad.sojourn.core.localization.resources.cd_edit_guests
import com.abulubad.sojourn.core.localization.resources.checkout_booking_failed
import com.abulubad.sojourn.core.localization.resources.checkout_dates
import com.abulubad.sojourn.core.localization.resources.checkout_gone_body
import com.abulubad.sojourn.core.localization.resources.checkout_gone_title
import com.abulubad.sojourn.core.localization.resources.checkout_guests
import com.abulubad.sojourn.core.localization.resources.checkout_offline
import com.abulubad.sojourn.core.localization.resources.checkout_payment_failed
import com.abulubad.sojourn.core.localization.resources.checkout_price
import com.abulubad.sojourn.core.localization.resources.checkout_price_changed
import com.abulubad.sojourn.core.localization.resources.checkout_repriced
import com.abulubad.sojourn.core.localization.resources.checkout_title
import com.abulubad.sojourn.core.localization.resources.offline_banner
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.Quote
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import org.jetbrains.compose.resources.stringResource

/**
 * Checkout (ScreenCheckout card): one review screen before payment.
 *
 * - The stay, then the trip (dates and guests, each with "Edit"), then the price: the breakdown is open, never
 *   behind a link, with every line that makes up the total and the refundable deposit apart from it. Then the
 *   cancellation terms.
 * - "Pay £1,383" spans the docked bar and repeats the total. It opens the demo wallet, which says no money moves
 *   and asks again with the same words. While paying the page's button holds its place and shows progress.
 * - A payment that did not book says why just above the button, in plain words, with the page unchanged.
 * - Loading shows the page's shape; an expired quote says so with a way back; offline keeps the cached page under
 *   the banner (paying then says it needs a connection).
 */
@Composable
fun CheckoutScreen(
    state: CheckoutUiState,
    onIntent: (CheckoutIntent) -> Unit,
    modifier: Modifier = Modifier,
    datesSheet: SojournSheetState = rememberSojournSheetState(),
    guestsSheet: SojournSheetState = rememberGuestsSheetState(),
    walletSheet: SojournSheetState = rememberWalletSheetState(),
    scroll: ScrollState = rememberScrollState(),
) {
    Box(modifier.fillMaxSize()) {
        SojournScaffold(
            topBar = {
                SojournTopBar(
                    title = stringResource(Res.string.checkout_title),
                    onBack = { onIntent(CheckoutIntent.BackTapped) },
                )
            },
            bottomBar = {
                val quote = state.quote
                if (quote != null && state.stay != null) PayBar(state, quote, onIntent)
            },
        ) { padding ->
            val quote = state.quote
            when (val details = state.details) {
                ScreenState.Loading -> Loading(padding)
                is ScreenState.Content -> if (quote != null) Page(details.data, quote, state, onIntent, scroll, padding)
                is ScreenState.Offline -> {
                    val cached = details.cached
                    if (cached != null && quote != null) {
                        Page(cached, quote, state, onIntent, scroll, padding, offline = true)
                    } else {
                        Failed(AppError.Offline, onIntent, padding)
                    }
                }
                is ScreenState.Error -> Failed(details.error, onIntent, padding)
                // A quote's stay is never empty: checkout maps no stay to an error.
                ScreenState.Empty -> Failed(AppError.NotFound(""), onIntent, padding)
            }
        }
        Sheets(state, onIntent, datesSheet, guestsSheet)
        WalletSheet(state, onIntent, walletSheet)
    }
}

@Composable
private fun PayBar(state: CheckoutUiState, quote: Quote, onIntent: (CheckoutIntent) -> Unit) {
    val formatter = rememberSojournFormatter()
    Column {
        state.notice?.let { Notice(it) }
        SojournActionBar(
            primary = stringResource(Res.string.action_pay, formatter.money(quote.total)),
            onPrimary = { onIntent(CheckoutIntent.PayTapped) },
            primaryLoading = state.paying,
            fillPrimary = true,
        )
    }
}

/** Why the last payment did not book, announced as it appears. Each reason says nothing was charged. */
@Composable
private fun Notice(notice: CheckoutNotice) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val text = when (notice) {
        CheckoutNotice.PaymentFailed -> stringResource(Res.string.checkout_payment_failed)
        is CheckoutNotice.PriceChanged ->
            stringResource(Res.string.checkout_price_changed, formatter.money(notice.total))
        CheckoutNotice.Offline -> stringResource(Res.string.checkout_offline)
        CheckoutNotice.BookingFailed -> stringResource(Res.string.checkout_booking_failed)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
            .background(c.dangerTint)
            .padding(horizontal = SojournSpacing.space4, vertical = SojournSpacing.space3),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
        verticalAlignment = Alignment.Top,
    ) {
        SojournIcon(SojournIcon.Alert, contentDescription = null, tint = c.danger, size = SojournSizes.iconSm)
        BasicText(text, style = SojournTheme.type.bodySm.copy(color = c.danger))
    }
}

@Composable
private fun Page(
    details: StayDetails,
    quote: Quote,
    state: CheckoutUiState,
    onIntent: (CheckoutIntent) -> Unit,
    scroll: ScrollState,
    padding: PaddingValues,
    offline: Boolean = false,
) {
    val stay = details.stay
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(padding)
            .padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        if (offline) SojournOfflineBanner(stringResource(Res.string.offline_banner))
        SojournStaySummary(
            photo = stay.photos.firstOrNull(),
            title = stay.title,
            place = localizedPlace(stay.cityId, stay.areaKey),
            rating = details.rating,
        )
        Panel {
            EditRow(
                label = stringResource(Res.string.checkout_dates),
                value = datesText(quote.dates),
                editDescription = stringResource(Res.string.cd_edit_dates),
                onEdit = { onIntent(CheckoutIntent.EditDatesTapped) },
            )
            SojournDivider()
            EditRow(
                label = stringResource(Res.string.checkout_guests),
                value = guestsText(quote.guests),
                editDescription = stringResource(Res.string.cd_edit_guests),
                onEdit = { onIntent(CheckoutIntent.EditGuestsTapped) },
            )
        }
        Panel {
            BasicText(
                stringResource(Res.string.checkout_price),
                style = SojournTheme.type.titleSm.copy(color = SojournTheme.colors.ink),
            )
            SojournPriceBreakdown(quote.breakdown, deposit = state.deposit)
            // After a price change while paying, the notice above "Pay" already says what changed and why.
            if (state.notice !is CheckoutNotice.PriceChanged) state.previousTotal?.let { Repriced(it) }
        }
        state.cancellation?.let { SojournPolicyRow(it) }
    }
}

/** A re-price is said, never silent: "Updated for your changes. It was £1,383." */
@Composable
private fun Repriced(was: Money) {
    BasicText(
        stringResource(Res.string.checkout_repriced, rememberSojournFormatter().money(was)),
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = SojournTheme.type.bodySm.copy(color = SojournTheme.colors.brandInk),
    )
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(SojournShapes.lg)
            .background(SojournTheme.colors.surface)
            .padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) { content() }
}

/** "Dates / 12–27 Mar 2027 · 15 nights" with "Edit" at the end, which a screen reader hears as "Edit dates". */
@Composable
private fun EditRow(label: String, value: String, editDescription: String, onEdit: () -> Unit) {
    val c = SojournTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.weight(1f).semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
        ) {
            BasicText(label, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
            BasicText(value, style = SojournTheme.type.body.copy(color = c.ink))
        }
        SojournButton(
            stringResource(Res.string.action_edit),
            onClick = onEdit,
            modifier = Modifier.semantics { contentDescription = editDescription },
            variant = SojournButtonVariant.Ghost,
        )
    }
}

@Composable
private fun Loading(padding: PaddingValues) {
    Column(
        Modifier.fillMaxSize().padding(padding).padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(SojournSpacing.space3)) {
            SojournSkeleton(Modifier.size(SojournSizes.touchMin), SojournShapes.md)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
                SojournSkeleton(Modifier.fillMaxWidth(SKELETON_MEDIUM).height(SojournSpacing.space4))
                SojournSkeleton(Modifier.fillMaxWidth(SKELETON_SHORT).height(SojournSpacing.space3))
            }
        }
        SojournSkeleton(Modifier.fillMaxWidth().height(SojournSizes.bookingBar), SojournShapes.lg)
        SojournSkeleton(Modifier.fillMaxWidth().height(SojournSizes.bookingBar * 2), SojournShapes.lg)
    }
}

@Composable
private fun Failed(error: AppError, onIntent: (CheckoutIntent) -> Unit, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding).padding(SojournSpacing.space4)) {
        if (error is AppError.NotFound) {
            SojournEmptyState(
                icon = SojournIcon.Calendar,
                title = stringResource(Res.string.checkout_gone_title),
                body = stringResource(Res.string.checkout_gone_body),
                action = SojournEmptyStateAction(stringResource(Res.string.action_back)) {
                    onIntent(CheckoutIntent.BackTapped)
                },
            )
        } else {
            SojournEmptyState(
                icon = if (error == AppError.Offline) SojournIcon.Offline else SojournIcon.Alert,
                title = stringResource(Res.string.state_error_title),
                body = stringResource(Res.string.state_error_body),
                action = SojournEmptyStateAction(stringResource(Res.string.action_retry)) {
                    onIntent(CheckoutIntent.RetryTapped)
                },
            )
        }
    }
}

private const val SKELETON_SHORT = 0.4f
private const val SKELETON_MEDIUM = 0.6f
