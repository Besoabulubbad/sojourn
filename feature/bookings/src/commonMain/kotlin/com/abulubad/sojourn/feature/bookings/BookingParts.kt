package com.abulubad.sojourn.feature.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.abulubad.sojourn.core.designsystem.components.bookingcard.SojournBookingCard
import com.abulubad.sojourn.core.designsystem.components.bookingcard.SojournBookingCardAction
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.policy.SojournPolicyRow
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.BookingRules
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_check_in_guide
import com.abulubad.sojourn.core.localization.resources.action_copy
import com.abulubad.sojourn.core.localization.resources.action_directions
import com.abulubad.sojourn.core.localization.resources.adults
import com.abulubad.sojourn.core.localization.resources.booking_status_active
import com.abulubad.sojourn.core.localization.resources.booking_status_cancelled
import com.abulubad.sojourn.core.localization.resources.booking_status_completed
import com.abulubad.sojourn.core.localization.resources.bookings_cancellation_after
import com.abulubad.sojourn.core.localization.resources.bookings_check_in_in
import com.abulubad.sojourn.core.localization.resources.bookings_check_in_tomorrow
import com.abulubad.sojourn.core.localization.resources.bookings_deposit_detail
import com.abulubad.sojourn.core.localization.resources.bookings_details
import com.abulubad.sojourn.core.localization.resources.check_in_self_check_in_detail
import com.abulubad.sojourn.core.localization.resources.children
import com.abulubad.sojourn.core.localization.resources.days
import com.abulubad.sojourn.core.localization.resources.guide_key_code
import com.abulubad.sojourn.core.localization.resources.guide_key_code_host
import com.abulubad.sojourn.core.localization.resources.guide_key_code_later
import com.abulubad.sojourn.core.localization.resources.guide_key_code_value
import com.abulubad.sojourn.core.localization.resources.list_series
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Policy
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A booking as a [SojournBookingCard]: countdown, place, title, dates and guests, reference. Bookings still to come
 * get "Directions" and "Check-in guide"; past and cancelled ones are a record only.
 */
@Composable
internal fun BookingCard(
    item: BookingItem,
    today: LocalDate,
    onDirections: () -> Unit,
    onGuide: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val booking = item.booking
    val stay = item.stay ?: return
    val ongoing = booking.status == BookingStatus.Upcoming || booking.status == BookingStatus.Active
    SojournBookingCard(
        photo = stay.photos.firstOrNull(),
        title = stay.title,
        place = localizedPlace(stay.cityId, stay.areaKey),
        details = detailsLine(item),
        reference = booking.reference,
        modifier = modifier,
        badge = countdown(item, today),
        onClick = onClick,
        actions = if (!ongoing) {
            emptyList()
        } else {
            listOf(
                SojournBookingCardAction(
                    stringResource(Res.string.action_directions),
                    onDirections,
                    SojournIcon.MapPin,
                ),
                SojournBookingCardAction(stringResource(Res.string.action_check_in_guide), onGuide, primary = true),
            )
        },
    )
}

/** "Check-in in 12 days", "Check-in tomorrow", "Staying now", "Completed", "Cancelled". */
@Composable
private fun countdown(item: BookingItem, today: LocalDate): String {
    val formatter = rememberSojournFormatter()
    return when (item.booking.status) {
        BookingStatus.Upcoming -> when (val days = BookingRules.daysUntil(item.booking, today)) {
            1 -> stringResource(Res.string.bookings_check_in_tomorrow)
            else -> stringResource(
                Res.string.bookings_check_in_in,
                pluralStringResource(Res.plurals.days, days, formatter.count(days)),
            )
        }
        BookingStatus.Active -> stringResource(Res.string.booking_status_active)
        BookingStatus.Completed -> stringResource(Res.string.booking_status_completed)
        BookingStatus.Cancelled -> stringResource(Res.string.booking_status_cancelled)
    }
}

/** "12–27 Mar 2027 · 15 nights · 2 adults". */
@Composable
internal fun detailsLine(item: BookingItem): String {
    val formatter = rememberSojournFormatter()
    val dates = item.booking.dates
    return stringResource(
        Res.string.bookings_details,
        formatter.dateRange(dates),
        pluralStringResource(Res.plurals.nights, dates.nights, formatter.count(dates.nights)),
        guestsText(item.booking.guests),
    )
}

@Composable
private fun guestsText(guests: Guests): String {
    val formatter = rememberSojournFormatter()
    val adults = pluralStringResource(Res.plurals.adults, guests.adults, formatter.count(guests.adults))
    if (guests.children == 0) return adults
    val children = pluralStringResource(Res.plurals.children, guests.children, formatter.count(guests.children))
    return stringResource(Res.string.list_series, adults, children)
}

/**
 * The stay's terms for this booking (ScreenBookings card): free cancellation and what happens after it, the deposit
 * and when it is released, and how check-in works.
 */
@Composable
internal fun BookingPolicies(item: BookingItem, modifier: Modifier = Modifier) {
    val policies = item.stay?.policies.orEmpty()
    val formatter = rememberSojournFormatter()
    val kept = BookingRules.KEPT_NIGHTS.coerceAtMost(item.booking.dates.nights)
    val shown = policies.filter {
        it is Policy.FreeCancellation || it is Policy.FreeCancellationBefore ||
            it is Policy.Deposit || it is Policy.CheckIn
    }
    Column(modifier) {
        shown.forEachIndexed { index, policy ->
            val detail = when (policy) {
                is Policy.FreeCancellation, is Policy.FreeCancellationBefore -> stringResource(
                    Res.string.bookings_cancellation_after,
                    pluralStringResource(Res.plurals.nights, kept, formatter.count(kept)),
                )
                is Policy.Deposit -> stringResource(Res.string.bookings_deposit_detail)
                is Policy.CheckIn -> if (policy.method == CheckInMethod.SelfCheckIn) {
                    stringResource(Res.string.check_in_self_check_in_detail)
                } else {
                    null
                }
                else -> null
            }
            SojournPolicyRow(policy, detail = detail, divider = index > 0)
        }
    }
}

/**
 * "Check-in guide": when and how to arrive, when to leave, and the key code. The code shows from arrival day until
 * check-out, never before (ScreenBookings card), large, left to right and copyable; before then the sheet says when
 * it will appear. A booking made before key codes existed gets its code by message.
 */
@Composable
internal fun GuideSheet(
    item: BookingItem,
    today: LocalDate,
    sheetState: SojournSheetState,
    onCopy: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val booking = item.booking
    val policies = item.stay?.policies.orEmpty()
    val checkIn = policies.filterIsInstance<Policy.CheckIn>().firstOrNull()
    SojournSheet(state = sheetState, title = stringResource(Res.string.action_check_in_guide), onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4)) {
            Column {
                policies.filter { it is Policy.CheckIn || it is Policy.CheckOut }.forEachIndexed { index, policy ->
                    SojournPolicyRow(policy, divider = index > 0)
                }
            }
            if (checkIn?.method == CheckInMethod.Host) {
                BasicText(
                    stringResource(Res.string.guide_key_code_host),
                    style = SojournTheme.type.body.copy(color = c.ink),
                )
                return@Column
            }
            BasicText(
                stringResource(Res.string.guide_key_code),
                style = SojournTheme.type.titleSm.copy(color = c.ink),
            )
            val code = booking.keyCode
            if (code != null && BookingRules.showsKeyCode(booking, today)) {
                // One line across the row: on iOS at 3x, the isolated code measured a little narrow in Arabic and
                // its last digit wrapped under the rest.
                BasicText(
                    stringResource(Res.string.guide_key_code_value, code),
                    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                    style = SojournTheme.type.display.copy(color = c.ink),
                    softWrap = false,
                    maxLines = 1,
                )
                SojournButton(
                    stringResource(Res.string.action_copy),
                    onClick = { onCopy(code) },
                    variant = SojournButtonVariant.Secondary,
                    leadingIcon = SojournIcon.Copy,
                )
            } else {
                // A booking from before key codes existed has none here: it comes by message, as the policy says.
                val text = if (code == null) {
                    stringResource(Res.string.check_in_self_check_in_detail)
                } else {
                    stringResource(Res.string.guide_key_code_later, formatter.date(booking.dates.start))
                }
                BasicText(text, style = SojournTheme.type.body.copy(color = c.inkMuted))
            }
        }
    }
}
