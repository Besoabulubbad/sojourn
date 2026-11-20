package com.abulubad.sojourn.core.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.CalendarContract
import android.provider.Settings
import com.abulubad.sojourn.core.model.GeoPoint
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Predefined effects (API 29+) so the feel matches the system; older devices stay silent rather than buzz. */
class AndroidHaptics(context: Context) : Haptics {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    override fun tick() = play(VibrationEffect.EFFECT_TICK)
    override fun success() = play(VibrationEffect.EFFECT_CLICK)
    override fun error() = play(VibrationEffect.EFFECT_DOUBLE_CLICK)

    private fun play(effect: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) vibrator?.vibrate(VibrationEffect.createPredefined(effect))
    }
}

class AndroidClipboard(private val context: Context) : Clipboard {
    override fun copy(text: String) {
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText(null, text))
    }
}

class AndroidShareSheet(private val context: Context) : ShareSheet {
    override fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

class AndroidMapsLauncher(private val context: Context) : MapsLauncher {
    override fun open(point: GeoPoint, label: String) {
        val coordinates = "${point.latitude},${point.longitude}"
        val uri = "geo:$coordinates?q=$coordinates(${Uri.encode(label)})".let(Uri::parse)
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/** "Remove animations" sets the animator duration scale to 0. */
class AndroidAccessibilitySettings(private val context: Context) : AccessibilitySettings {
    override fun reduceMotion(): Boolean =
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}

/** The calendar app's insert screen, filled in; the guest saves it there. No calendar permission is needed. */
class AndroidCalendarSaver(private val context: Context) : CalendarSaver {
    override fun add(stay: CalendarStay): Boolean {
        val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, utcMidnight(stay.dates.start))
            // An all-day event ends at the start of the day after its last day: check-out day is included.
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, utcMidnight(stay.dates.end.plus(1, DateTimeUnit.DAY)))
            .putExtra(CalendarContract.Events.TITLE, stay.title)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, stay.location)
            .putExtra(CalendarContract.Events.DESCRIPTION, stay.notes)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val handler = intent.resolveActivity(context.packageManager) ?: return false
        context.startActivity(intent.setComponent(handler))
        return true
    }

    // All-day events are stored at UTC midnight on Android.
    private fun utcMidnight(date: LocalDate): Long = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

actual val platformModule: Module = module {
    single<Haptics> { AndroidHaptics(androidContext()) }
    single<ShareSheet> { AndroidShareSheet(androidContext()) }
    single<MapsLauncher> { AndroidMapsLauncher(androidContext()) }
    single<Clipboard> { AndroidClipboard(androidContext()) }
    single<AccessibilitySettings> { AndroidAccessibilitySettings(androidContext()) }
    single<PaymentSheet> { StubPaymentSheet() }
    single<CalendarSaver> { AndroidCalendarSaver(androidContext()) }
    // One instance, so the activity that binds the permission launcher and the screens that ask share it.
    single { AndroidReminders(androidContext()) }
    single<Reminders> { get<AndroidReminders>() }
}
