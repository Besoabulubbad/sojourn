package com.abulubad.sojourn.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * Arrival reminders on iOS: local notifications with a calendar trigger, which the system keeps across restarts. The
 * system asks once; after that the answer lives in Settings › Sojourn › Notifications.
 */
class IosReminders : Reminders {
    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun allowed(): Boolean = suspendCancellableCoroutine { done ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            done.resume(status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional)
        }
    }

    override suspend fun ask(): Boolean = suspendCancellableCoroutine { done ->
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { granted, _ ->
            done.resume(granted)
        }
    }

    @OptIn(ExperimentalForeignApi::class) // convert() of the calendar unit flags.
    override fun schedule(reminder: Reminder) {
        val content = UNMutableNotificationContent()
        content.setTitle(reminder.title)
        content.setBody(reminder.body)
        content.setSound(UNNotificationSound.defaultSound)
        val date = NSDate.dateWithTimeIntervalSince1970(reminder.at.epochSeconds.toDouble())
        val units = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
            NSCalendarUnitHour or NSCalendarUnitMinute
        val parts = NSCalendar.currentCalendar.components(units.convert(), fromDate = date)
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(parts, repeats = false)
        center.addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(reminder.id, content, trigger),
            withCompletionHandler = null,
        )
    }

    override fun cancel(id: String) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(id))
    }
}
