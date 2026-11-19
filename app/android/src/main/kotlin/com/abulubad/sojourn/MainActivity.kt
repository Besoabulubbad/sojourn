package com.abulubad.sojourn

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.abulubad.sojourn.core.platform.AndroidReminders
import org.koin.android.ext.android.inject

/**
 * The single activity: edge to edge (I-01), the shared [App] as its content. The system bars follow the app's
 * appearance rather than the device's, so their icons stay legible when the guest picks Light or Dark.
 */
class MainActivity : ComponentActivity() {

    // The arrival reminder asks for the notification permission (Android 13+) through this activity.
    private val reminders: AndroidReminders by inject()
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        reminders.answered(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        reminders.bind { permission -> notificationPermission.launch(permission) }
        setContent {
            App(onDarkTheme = { dark ->
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark },
                )
            })
        }
    }

    private companion object {
        // enableEdgeToEdge's own defaults for three-button navigation, kept when the bars follow the app.
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
