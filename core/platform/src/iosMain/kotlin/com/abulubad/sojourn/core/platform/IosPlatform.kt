package com.abulubad.sojourn.core.platform

import com.abulubad.sojourn.core.model.GeoPoint
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UIPasteboard

/** Light impact for ticks; the notification generator for payment success and failure. */
class IosHaptics : Haptics {
    override fun tick() = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
    override fun success() = UINotificationFeedbackGenerator().notificationOccurred(
        UINotificationFeedbackType.UINotificationFeedbackTypeSuccess,
    )
    override fun error() = UINotificationFeedbackGenerator().notificationOccurred(
        UINotificationFeedbackType.UINotificationFeedbackTypeError,
    )
}

/** Presents the activity sheet from the key window's root controller. */
class IosShareSheet : ShareSheet {
    override fun share(text: String) {
        val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            controller,
            animated = true,
            completion = null,
        )
    }
}

class IosClipboard : Clipboard {
    override fun copy(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}

/** Apple Maps at the area centre. */
class IosMapsLauncher : MapsLauncher {
    override fun open(point: GeoPoint, label: String) {
        val components = NSURLComponents(string = "https://maps.apple.com/")
        components.queryItems = listOf(
            NSURLQueryItem(name = "ll", value = "${point.latitude},${point.longitude}"),
            NSURLQueryItem(name = "q", value = label),
        )
        val url: NSURL = components.URL ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

class IosAccessibilitySettings : AccessibilitySettings {
    override fun reduceMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()
}

actual val platformModule: Module = module {
    single<Haptics> { IosHaptics() }
    single<ShareSheet> { IosShareSheet() }
    single<MapsLauncher> { IosMapsLauncher() }
    single<Clipboard> { IosClipboard() }
    single<AccessibilitySettings> { IosAccessibilitySettings() }
    single<PaymentSheet> { StubPaymentSheet() }
}
