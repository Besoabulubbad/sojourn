package com.abulubad.sojourn

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/**
 * Entry point for the iOS app: `ContentView.swift` wraps this controller. The name is fixed by the Xcode project.
 * The window takes the app's appearance, so the status bar and system sheets match a Light or Dark choice.
 */
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController {
    App(onDarkTheme = { dark ->
        val style = if (dark) {
            UIUserInterfaceStyle.UIUserInterfaceStyleDark
        } else {
            UIUserInterfaceStyle.UIUserInterfaceStyleLight
        }
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
            .forEach { it.overrideUserInterfaceStyle = style }
    })
}

/** Starts Koin; `iOSApp.init` calls it once, before the first view (Swift: `MainViewControllerKt.startKoinIos()`). */
fun startKoinIos() {
    initKoin()
}
