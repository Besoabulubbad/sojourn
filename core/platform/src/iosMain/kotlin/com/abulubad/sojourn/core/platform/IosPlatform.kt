package com.abulubad.sojourn.core.platform

import com.abulubad.sojourn.core.model.GeoPoint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKitUI.EKEventEditViewAction
import platform.EventKitUI.EKEventEditViewController
import platform.EventKitUI.EKEventEditViewDelegateProtocol
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
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
import platform.darwin.NSObject

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

/**
 * The system event editor, filled in and presented over the app; the guest adds it there. Since iOS 17 this needs no
 * calendar access: the editor runs outside the app.
 */
class IosCalendarSaver : CalendarSaver {
    private val store = EKEventStore()

    // The editor holds its delegate weakly; this keeps it alive while the editor is open.
    private var closer: CloseWhenDone? = null

    override fun add(stay: CalendarStay): Boolean {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return false
        val event = EKEvent.eventWithEventStore(store)
        event.title = stay.title
        event.location = stay.location
        event.notes = stay.notes
        event.allDay = true
        event.startDate = nsDate(stay.dates.start)
        // An all-day event on iOS covers its end date: check-out day is included.
        event.endDate = nsDate(stay.dates.end)
        val editor = EKEventEditViewController()
        editor.eventStore = store
        editor.event = event
        editor.editViewDelegate = CloseWhenDone().also { closer = it }
        root.presentViewController(editor, animated = true, completion = null)
        return true
    }

    private fun nsDate(date: LocalDate): NSDate {
        val parts = NSDateComponents()
        parts.year = date.year.toLong()
        parts.month = date.month.number.toLong()
        parts.day = date.day.toLong()
        return checkNotNull(NSCalendar.currentCalendar.dateFromComponents(parts)) { "A calendar date is always valid" }
    }
}

/** Closes the event editor whether the guest added the event or cancelled. */
private class CloseWhenDone : NSObject(), EKEventEditViewDelegateProtocol {
    override fun eventEditViewController(
        controller: EKEventEditViewController,
        didCompleteWithAction: EKEventEditViewAction,
    ) {
        controller.dismissViewControllerAnimated(true, completion = null)
    }
}

actual val platformModule: Module = module {
    single<Haptics> { IosHaptics() }
    single<ShareSheet> { IosShareSheet() }
    single<MapsLauncher> { IosMapsLauncher() }
    single<Clipboard> { IosClipboard() }
    single<AccessibilitySettings> { IosAccessibilitySettings() }
    single<PaymentSheet> { StubPaymentSheet() }
    single<CalendarSaver> { IosCalendarSaver() }
    single<Reminders> { IosReminders() }
}
