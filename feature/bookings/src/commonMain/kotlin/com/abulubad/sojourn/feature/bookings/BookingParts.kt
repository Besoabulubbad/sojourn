package com.abulubad.sojourn.feature.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.components.bookingcard.SojournBookingCard
import com.abulubad.sojourn.core.designsystem.components.bookingcard.SojournBookingCardAction
import com.abulubad.sojourn.core.designsystem.components.button.SojournButton
import com.abulubad.sojourn.core.designsystem.components.button.SojournButtonVariant
import com.abulubad.sojourn.core.designsystem.components.policy.SojournPolicyRow
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheet
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetState
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.BookingRules
import com.abulubad.sojourn.core.localization.localizedPlace
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_add_to_calendar
import com.abulubad.sojourn.core.localization.resources.action_check_in_guide
import com.abulubad.sojourn.core.localization.resources.action_copy
import com.abulubad.sojourn.core.localization.resources.action_copy_address
import com.abulubad.sojourn.core.localization.resources.action_copy_password
import com.abulubad.sojourn.core.localization.resources.action_directions
import com.abulubad.sojourn.core.localization.resources.adults
import com.abulubad.sojourn.core.localization.resources.booking_address
import com.abulubad.sojourn.core.localization.resources.booking_address_value
import com.abulubad.sojourn.core.localization.resources.booking_status_active
import com.abulubad.sojourn.core.localization.resources.booking_status_cancelled
import com.abulubad.sojourn.core.localization.resources.booking_status_completed
import com.abulubad.sojourn.core.localization.resources.bookings_cancellation_after
import com.abulubad.sojourn.core.localization.resources.bookings_check_in_in
import com.abulubad.sojourn.core.localization.resources.bookings_check_in_tomorrow
import com.abulubad.sojourn.core.localization.resources.bookings_deposit_detail
import com.abulubad.sojourn.core.localization.resources.bookings_details
import com.abulubad.sojourn.core.localization.resources.calendar_notes
import com.abulubad.sojourn.core.localization.resources.calendar_notes_reference
import com.abulubad.sojourn.core.localization.resources.check_in_self_check_in_detail
import com.abulubad.sojourn.core.localization.resources.children
import com.abulubad.sojourn.core.localization.resources.days
import com.abulubad.sojourn.core.localization.resources.guide_key_code
import com.abulubad.sojourn.core.localization.resources.guide_key_code_host
import com.abulubad.sojourn.core.localization.resources.guide_key_code_later
import com.abulubad.sojourn.core.localization.resources.guide_key_code_value
import com.abulubad.sojourn.core.localization.resources.guide_wifi
import com.abulubad.sojourn.core.localization.resources.guide_wifi_later
import com.abulubad.sojourn.core.localization.resources.guide_wifi_network
import com.abulubad.sojourn.core.localization.resources.guide_wifi_password
import com.abulubad.sojourn.core.localization.resources.guide_wifi_value
import com.abulubad.sojourn.core.localization.resources.list_series
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.model.Booking
import com.abulubad.sojourn.core.model.BookingStatus
import com.abulubad.sojourn.core.model.CheckInMethod
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.model.Wifi
import com.abulubad.sojourn.core.platform.CalendarStay
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
 * "Check-in guide": when and how to arrive and leave, where (the booked address), the key code and the Wi-Fi. The
 * code and the Wi-Fi show from arrival day until check-out, never before (ScreenBookings card), large, left to right
 * and copyable; before then the sheet says when they will appear. A host check-in has no code: the host meets the
 * guest. A booking made before key codes existed gets its code by message.
 */
@Composable
internal fun GuideSheet(
    item: BookingItem,
    today: LocalDate,
    sheetState: SojournSheetState,
    onCopy: (String, Copied) -> Unit,
    onDismiss: () -> Unit,
) {
    val booking = item.booking
    val policies = item.stay?.policies.orEmpty()
    val checkIn = policies.filterIsInstance<Policy.CheckIn>().firstOrNull()
    val arrived = BookingRules.showsKeyCode(booking, today)
    SojournSheet(state = sheetState, title = stringResource(Res.string.action_check_in_guide), onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space5)) {
            Column {
                policies.filter { it is Policy.CheckIn || it is Policy.CheckOut }.forEachIndexed { index, policy ->
                    SojournPolicyRow(policy, divider = index > 0)
                }
            }
            booking.address?.let { address -> AddressLines(address, item) }
            KeyCode(booking, hosted = checkIn?.method == CheckInMethod.Host, arrived = arrived, onCopy = onCopy)
            booking.wifi?.let { wifi -> WifiDetails(wifi, booking, arrived, onCopy) }
        }
    }
}

/** The key code (self check-in), or why there is none yet. */
@Composable
private fun KeyCode(booking: Booking, hosted: Boolean, arrived: Boolean, onCopy: (String, Copied) -> Unit) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
        GuideHeading(stringResource(Res.string.guide_key_code))
        val code = booking.keyCode
        when {
            hosted -> BasicText(
                stringResource(Res.string.guide_key_code_host),
                style = SojournTheme.type.body.copy(color = c.ink),
            )
            code != null && arrived -> {
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
                    onClick = { onCopy(code, Copied.KeyCode) },
                    variant = SojournButtonVariant.Secondary,
                    leadingIcon = SojournIcon.Copy,
                )
            }
            // A booking from before key codes existed has none here: it comes by message, as the policy says.
            code == null -> BasicText(
                stringResource(Res.string.check_in_self_check_in_detail),
                style = SojournTheme.type.body.copy(color = c.inkMuted),
            )
            else -> BasicText(
                stringResource(Res.string.guide_key_code_later, formatter.date(booking.dates.start)),
                style = SojournTheme.type.body.copy(color = c.inkMuted),
            )
        }
    }
}

/** The network and password from arrival day, the password copyable; before then, when they appear. */
@Composable
private fun WifiDetails(wifi: Wifi, booking: Booking, arrived: Boolean, onCopy: (String, Copied) -> Unit) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2)) {
        GuideHeading(stringResource(Res.string.guide_wifi))
        if (!arrived) {
            BasicText(
                stringResource(Res.string.guide_wifi_later, formatter.date(booking.dates.start)),
                style = SojournTheme.type.body.copy(color = c.inkMuted),
            )
            return@Column
        }
        WifiLine(stringResource(Res.string.guide_wifi_network), wifi.network)
        WifiLine(stringResource(Res.string.guide_wifi_password), wifi.password)
        SojournButton(
            stringResource(Res.string.action_copy_password),
            onClick = { onCopy(wifi.password, Copied.WifiPassword) },
            variant = SojournButtonVariant.Secondary,
            leadingIcon = SojournIcon.Copy,
        )
    }
}

/** "Network" or "Password", then the value as written, left to right. */
@Composable
private fun WifiLine(label: String, value: String) {
    val c = SojournTheme.colors
    Column(Modifier.semantics(mergeDescendants = true) {}) {
        BasicText(label, style = SojournTheme.type.bodySm.copy(color = c.inkMuted))
        BasicText(
            stringResource(Res.string.guide_wifi_value, value),
            style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun GuideHeading(text: String) {
    BasicText(
        text,
        modifier = Modifier.semantics { heading() },
        style = SojournTheme.type.titleSm.copy(color = SojournTheme.colors.ink),
    )
}

/** "Address", the flat and building (left to right), then the area and city in the app's language. */
@Composable
internal fun AddressLines(address: String, item: BookingItem) {
    val c = SojournTheme.colors
    Column(
        Modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space1),
    ) {
        GuideHeading(stringResource(Res.string.booking_address))
        BasicText(
            stringResource(Res.string.booking_address_value, address),
            style = SojournTheme.type.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
        )
        item.stay?.let { stay ->
            BasicText(
                localizedPlace(stay.cityId, stay.areaKey),
                style = SojournTheme.type.body.copy(color = c.inkMuted),
            )
        }
    }
}

/**
 * The booked address on a booking's page, with "Copy address" and "Add to calendar" (an all-day entry from check-in
 * through check-out, with the check-in and check-out times and the reference). Shown while the stay is to come.
 */
@Composable
internal fun BookingAddress(
    item: BookingItem,
    onCopy: (String, Copied) -> Unit,
    onAddToCalendar: (CalendarStay) -> Unit,
    modifier: Modifier = Modifier,
) {
    val address = item.booking.address ?: return
    val c = SojournTheme.colors
    val calendarStay = rememberCalendarStay(item, address)
    Column(
        modifier
            .fillMaxWidth()
            .clip(SojournShapes.lg)
            .background(c.surface)
            .padding(SojournSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
    ) {
        AddressLines(address, item)
        SojournButton(
            stringResource(Res.string.action_copy_address),
            onClick = { onCopy(calendarStay.location, Copied.Address) },
            modifier = Modifier.fillMaxWidth(),
            variant = SojournButtonVariant.Secondary,
            leadingIcon = SojournIcon.Copy,
        )
        SojournButton(
            stringResource(Res.string.action_add_to_calendar),
            onClick = { onAddToCalendar(calendarStay) },
            modifier = Modifier.fillMaxWidth(),
            variant = SojournButtonVariant.Secondary,
            leadingIcon = SojournIcon.Calendar,
        )
    }
}

/** The calendar entry, worded now, in the app's language: the stay's title, the full address, times, reference. */
@Composable
private fun rememberCalendarStay(item: BookingItem, address: String): CalendarStay {
    val formatter = rememberSojournFormatter()
    val stay = item.stay
    val booking = item.booking
    val place = stay?.let { localizedPlace(it.cityId, it.areaKey) }
    val policies = stay?.policies.orEmpty()
    val checkIn = policies.filterIsInstance<Policy.CheckIn>().firstOrNull()
    val checkOut = policies.filterIsInstance<Policy.CheckOut>().firstOrNull()
    val reference = formatter.reference(booking.reference)
    val notes = if (checkIn != null && checkOut != null) {
        stringResource(Res.string.calendar_notes, formatter.time(checkIn.from), formatter.time(checkOut.by), reference)
    } else {
        stringResource(Res.string.calendar_notes_reference, reference)
    }
    val location = listOfNotNull(address, place).joinToString(", ")
    return CalendarStay(
        title = stay?.title?.text ?: reference,
        location = location,
        dates = booking.dates,
        notes = notes,
    )
}
