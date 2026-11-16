package com.abulubad.sojourn.core.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.abulubad.sojourn.core.model.GeoPoint
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

actual val platformModule: Module = module {
    single<Haptics> { AndroidHaptics(androidContext()) }
    single<ShareSheet> { AndroidShareSheet(androidContext()) }
    single<MapsLauncher> { AndroidMapsLauncher(androidContext()) }
    single<AccessibilitySettings> { AndroidAccessibilitySettings(androidContext()) }
    single<PaymentSheet> { StubPaymentSheet() }
}
