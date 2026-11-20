package com.abulubad.sojourn.core.platform

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * The Android side of arrival reminders, which the emulator cannot reach before the day: scheduling arms an alarm,
 * the alarm shows the stored words, a restart arms what is still to come again, and cancelling removes it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidRemindersTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val alarms = shadowOf(context.getSystemService(AlarmManager::class.java))
    private val notifications = shadowOf(context.getSystemService(NotificationManager::class.java))
    private val reminder = Reminder(
        id = "b-1",
        at = Clock.System.now() + 3.days,
        title = "Check-in tomorrow",
        body = "Bright studio by the canal · check-in from 15:00. Your check-in guide is in Sojourn.",
    )

    @Test
    fun `a scheduled reminder arms an alarm and shows its words when it fires`() {
        AndroidReminders(context).schedule(reminder)
        val alarm = assertNotNull(alarms.nextScheduledAlarm)
        assertEquals(reminder.at.toEpochMilliseconds(), alarm.triggerAtTime)

        ReminderReceiver().onReceive(context, Intent().putExtra("reminder_id", reminder.id))

        val shown = notifications.allNotifications.single()
        assertEquals(reminder.title, shown.extras.getString("android.title"))
        assertEquals(reminder.body, shown.extras.getString("android.text"))
        assertNull(ReminderStore(context).get(reminder.id), "a shown reminder is forgotten")
    }

    @Test
    fun `a restart arms stored reminders again and drops past ones`() {
        val past = reminder.copy(id = "b-0", at = Clock.System.now() - 1.days)
        ReminderStore(context).put(reminder)
        ReminderStore(context).put(past)

        RebootReceiver().onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        assertEquals(listOf(reminder.at.toEpochMilliseconds()), alarms.scheduledAlarms.map { it.triggerAtTime })
        assertNull(ReminderStore(context).get(past.id))
    }

    @Test
    fun `cancelling removes the alarm and the stored words`() {
        val reminders = AndroidReminders(context)
        reminders.schedule(reminder)
        reminders.cancel(reminder.id)
        assertEquals(emptyList(), alarms.scheduledAlarms)
        assertNull(ReminderStore(context).get(reminder.id))
    }
}
