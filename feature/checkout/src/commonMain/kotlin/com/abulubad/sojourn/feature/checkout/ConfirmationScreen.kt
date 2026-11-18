package com.abulubad.sojourn.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.actionbar.SojournActionBar
import com.abulubad.sojourn.core.designsystem.components.divider.SojournDivider
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyState
import com.abulubad.sojourn.core.designsystem.components.emptystate.SojournEmptyStateAction
import com.abulubad.sojourn.core.designsystem.components.staysummary.SojournStaySummary
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_done
import com.abulubad.sojourn.core.localization.resources.action_view_booking
import com.abulubad.sojourn.core.localization.resources.booking_reference
import com.abulubad.sojourn.core.localization.resources.checkout_dates
import com.abulubad.sojourn.core.localization.resources.checkout_guests
import com.abulubad.sojourn.core.localization.resources.confirmed_body
import com.abulubad.sojourn.core.localization.resources.confirmed_title
import com.abulubad.sojourn.core.localization.resources.confirmed_total_paid
import com.abulubad.sojourn.core.localization.resources.state_error_body
import com.abulubad.sojourn.core.localization.resources.state_error_title
import com.abulubad.sojourn.core.ui.ScreenState
import com.abulubad.sojourn.core.ui.SojournScaffold
import org.jetbrains.compose.resources.stringResource

/**
 * "You're booked": the confirmation checkout hands over to once the booking exists. Not on the ScreenCheckout card
 * yet; built from its components: a check, the reference as written (never mirrored), the stay, the dates, the
 * guests and the total paid, then "View booking" and "Done". Back from here returns to the stay, not to a paid
 * checkout.
 */
@Composable
fun ConfirmationScreen(
    state: ConfirmationUiState,
    onIntent: (ConfirmationIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SojournScaffold(
        modifier = modifier,
        bottomBar = {
            if (state.booking !is ScreenState.Content) return@SojournScaffold
            SojournActionBar(
                primary = stringResource(Res.string.action_view_booking),
                onPrimary = { onIntent(ConfirmationIntent.ViewBookingTapped) },
                secondary = stringResource(Res.string.action_done),
                onSecondary = { onIntent(ConfirmationIntent.DoneTapped) },
            )
        },
    ) { padding ->
        when (val booking = state.booking) {
            is ScreenState.Content -> Confirmed(booking.data, padding)
            ScreenState.Loading -> Unit
            // The booking was stored before this screen opened; anything else is unexpected.
            is ScreenState.Error, is ScreenState.Offline, ScreenState.Empty -> Missing(onIntent, padding)
        }
    }
}

@Composable
private fun Confirmed(item: BookingItem, padding: PaddingValues) {
    val c = SojournTheme.colors
    val booking = item.booking
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
    ) {
        SojournIcon(SojournIcon.CheckCircle, contentDescription = null, tint = c.brand, size = SojournSizes.touchMin)
        BasicText(
            stringResource(Res.string.confirmed_title),
            modifier = Modifier.semantics { heading() },
            style = SojournTheme.type.titleLg.copy(color = c.ink),
        )
        BasicText(stringResource(Res.string.confirmed_body), style = SojournTheme.type.body.copy(color = c.inkMuted))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(SojournShapes.lg)
                .background(c.surface)
                .padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            BasicText(
                stringResource(Res.string.booking_reference, booking.reference),
                style = SojournTheme.type.label.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
            )
            item.stay?.let { stay ->
                SojournStaySummary(
                    photo = stay.photos.firstOrNull(),
                    title = stay.title,
                    place = localizedPlace(stay.cityId, stay.areaKey),
                )
            }
            SojournDivider()
            Fact(stringResource(Res.string.checkout_dates), datesText(booking.dates))
            Fact(stringResource(Res.string.checkout_guests), guestsText(booking.guests))
            Fact(stringResource(Res.string.confirmed_total_paid), rememberSojournFormatter().money(booking.total))
        }
    }
}

/** One labelled fact, the label above its value so large text never squeezes either; read as one. */
@Composable
private fun Fact(label: String, value: String) {
    val c = SojournTheme.colors
    Column(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
    ) {
        BasicText(label, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
        BasicText(value, style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun Missing(onIntent: (ConfirmationIntent) -> Unit, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding).padding(SojournSpacing.space4)) {
        SojournEmptyState(
            icon = SojournIcon.Alert,
            title = stringResource(Res.string.state_error_title),
            body = stringResource(Res.string.state_error_body),
            action = SojournEmptyStateAction(stringResource(Res.string.action_done)) {
                onIntent(ConfirmationIntent.DoneTapped)
            },
        )
    }
}
