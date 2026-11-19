package com.abulubad.sojourn.feature.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.abulubad.sojourn.core.common.AppClock
import com.abulubad.sojourn.core.designsystem.components.reminder.SojournReminderState
import com.abulubad.sojourn.core.domain.BookingItem
import com.abulubad.sojourn.core.domain.BookingRules
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.reminder_notification_body
import com.abulubad.sojourn.core.localization.resources.reminder_notification_body_no_time
import com.abulubad.sojourn.core.localization.resources.reminder_notification_title
import com.abulubad.sojourn.core.model.Policy
import com.abulubad.sojourn.core.platform.Reminder
import com.abulubad.sojourn.core.platform.Reminders
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** The arrival reminder for one booking: its [state] for the card (null when there is none to offer) and [remind]. */
internal class ArrivalReminder(val state: SojournReminderState?, val remind: () -> Unit)

/**
 * The arrival reminder (BookingRules.reminderAt: the evening before check-in, never late). Once the guest has allowed
 * notifications it is scheduled without asking again; otherwise the card offers it and "Remind me" asks the system.
 * The notification is worded now, in the app's language. Shared with the other booking screen in its own feature.
 */
@Composable
internal fun rememberArrivalReminder(item: BookingItem?): ArrivalReminder {
    val reminders = koinInject<Reminders>()
    val clock = koinInject<AppClock>()
    val scope = rememberCoroutineScope()
    val formatter = rememberSojournFormatter()
    val booking = item?.booking
    val at = booking?.let { BookingRules.reminderAt(it, clock.now(), clock.timeZone) }
    val stayTitle = item?.stay?.title?.text ?: booking?.reference.orEmpty()
    val checkIn = item?.stay?.policies?.filterIsInstance<Policy.CheckIn>()?.firstOrNull()
    val title = stringResource(Res.string.reminder_notification_title)
    val body = if (checkIn != null) {
        stringResource(Res.string.reminder_notification_body, stayTitle, formatter.time(checkIn.from))
    } else {
        stringResource(Res.string.reminder_notification_body_no_time, stayTitle)
    }
    val reminder = if (booking != null && at != null) Reminder(booking.id.value, at, title, body) else null
    var state by remember(reminder) { mutableStateOf<SojournReminderState?>(null) }
    val set = reminder?.let { SojournReminderState.Set(it.at.toLocalDateTime(clock.timeZone)) }
    LaunchedEffect(reminder) {
        state = when {
            reminder == null -> null
            reminders.allowed() -> {
                reminders.schedule(reminder)
                set
            }
            else -> SojournReminderState.Offer
        }
    }
    return ArrivalReminder(state) {
        if (reminder != null) {
            scope.launch {
                state = if (reminders.ask()) {
                    reminders.schedule(reminder)
                    set
                } else {
                    SojournReminderState.Blocked
                }
            }
        }
    }
}
