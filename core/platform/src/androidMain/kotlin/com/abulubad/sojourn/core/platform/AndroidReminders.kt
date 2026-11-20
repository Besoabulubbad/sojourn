package com.abulubad.sojourn.core.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Arrival reminders on Android. A reminder is stored (so it survives a restart: [RebootReceiver] arms it again) and
 * armed with an inexact alarm, which needs no exact-alarm permission and may run a few minutes late. When it fires,
 * [ReminderReceiver] shows the notification and forgets it.
 *
 * Asking needs an activity: the shell calls [bind] once with a launcher for the notification permission and passes
 * each answer to [answered].
 */
class AndroidReminders(private val context: Context) : Reminders {

    private var launch: ((String) -> Unit)? = null
    private var pending: CompletableDeferred<Boolean>? = null

    /** The activity's launcher for a runtime permission (Android 13+ asks for notifications). */
    fun bind(launcher: (String) -> Unit) {
        launch = launcher
    }

    fun answered(granted: Boolean) {
        pending?.complete(granted)
        pending = null
    }

    override suspend fun allowed(): Boolean =
        context.getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() == true &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || granted())

    override suspend fun ask(): Boolean {
        val launcher = launch
        return when {
            allowed() -> true
            // Below Android 13 notifications need no permission; off means the guest turned them off in Settings.
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || launcher == null -> false
            else -> {
                val answer = CompletableDeferred<Boolean>().also { pending = it }
                launcher(Manifest.permission.POST_NOTIFICATIONS)
                answer.await()
            }
        }
    }

    override fun schedule(reminder: Reminder) {
        ReminderStore(context).put(reminder)
        arm(context, reminder)
    }

    override fun cancel(id: String) {
        ReminderStore(context).remove(id)
        context.getSystemService(AlarmManager::class.java)?.cancel(fireIntent(context, id))
        context.getSystemService(NotificationManager::class.java)?.cancel(id.hashCode())
    }

    private fun granted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    internal companion object {
        const val EXTRA_ID = "reminder_id"
        const val CHANNEL = "arrival"

        fun arm(context: Context, reminder: Reminder) {
            context.getSystemService(AlarmManager::class.java)
                ?.set(AlarmManager.RTC_WAKEUP, reminder.at.toEpochMilliseconds(), fireIntent(context, reminder.id))
        }

        fun fireIntent(context: Context, id: String): PendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

/** Stored reminders, so a restart can arm them again: one entry per id, its time and words. */
internal class ReminderStore(context: Context) {
    private val prefs = context.getSharedPreferences("sojourn_reminders", Context.MODE_PRIVATE)

    fun put(reminder: Reminder) = prefs.edit()
        .putLong("${reminder.id}$AT", reminder.at.toEpochMilliseconds())
        .putString("${reminder.id}$TITLE", reminder.title)
        .putString("${reminder.id}$BODY", reminder.body)
        .apply()

    fun get(id: String): Reminder? {
        val at = prefs.getLong("$id$AT", -1)
        val title = prefs.getString("$id$TITLE", null)
        val body = prefs.getString("$id$BODY", null)
        return if (at >= 0 && title != null && body != null) {
            Reminder(id, Instant.fromEpochMilliseconds(at), title, body)
        } else {
            null
        }
    }

    fun remove(id: String) = prefs.edit().remove("$id$AT").remove("$id$TITLE").remove("$id$BODY").apply()

    fun all(): List<Reminder> = prefs.all.keys.filter { it.endsWith(AT) }.map { it.removeSuffix(AT) }.mapNotNull(::get)

    private companion object {
        const val AT = ".at"
        const val TITLE = ".title"
        const val BODY = ".body"
    }
}

/** Shows a reminder when its alarm fires; a tap opens the app. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(AndroidReminders.EXTRA_ID) ?: return
        val store = ReminderStore(context)
        val reminder = store.get(id) ?: return
        store.remove(id)
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                AndroidReminders.CHANNEL,
                context.getString(R.string.reminder_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, id.hashCode(), it, PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = NotificationCompat.Builder(context, AndroidReminders.CHANNEL)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle(reminder.title)
            .setContentText(reminder.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(id.hashCode(), notification)
    }
}

/** Alarms do not survive a restart: arms every stored reminder still to come again. */
class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val now = Clock.System.now()
        val store = ReminderStore(context)
        store.all().forEach { reminder ->
            if (reminder.at > now) AndroidReminders.arm(context, reminder) else store.remove(reminder.id)
        }
    }
}
