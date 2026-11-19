package com.abulubad.sojourn.core.platform

import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.QuoteId
import kotlin.time.Instant

/**
 * Haptics (docs/design-system/motion.md): a light tick on save, stepper change, date selected and sheet detents;
 * success and error patterns for payment. Nothing else vibrates. Android: tick; iOS: light impact.
 */
interface Haptics {
    fun tick()
    fun success()
    fun error()
}

/** The platform share sheet (Android chooser, iOS activity sheet). [text] is already localized. */
fun interface ShareSheet {
    fun share(text: String)
}

/** The system clipboard: copies [text] (a key code) so the guest can paste it. */
fun interface Clipboard {
    fun copy(text: String)
}

/** Opens the platform maps app at an area. A stay's position stays approximate before booking (brand book). */
fun interface MapsLauncher {
    fun open(point: GeoPoint, label: String)
}

/** Accessibility settings components read: "reduce motion" swaps movement for a 150 ms fade (M-03). */
fun interface AccessibilitySettings {
    fun reduceMotion(): Boolean
}

/** What the payment sheet is asked to charge: the quote's all-in total (P-02). */
data class PaymentRequest(val quoteId: QuoteId, val total: Money)

/** How a payment attempt ended. */
sealed interface PaymentResult {
    data class Authorized(val token: String) : PaymentResult
    data object Cancelled : PaymentResult
    data class Failed(val reason: String) : PaymentResult
}

/**
 * Google Pay / Apple Pay first (brand book, Platform differences). Real sheets need merchant accounts and land in
 * Phase 6; until then [StubPaymentSheet] authorizes every request so the checkout flow is demonstrable.
 */
fun interface PaymentSheet {
    suspend fun pay(request: PaymentRequest): PaymentResult
}

/** Authorizes every request with a deterministic token. Not a real payment: no money moves in this demo. */
class StubPaymentSheet : PaymentSheet {
    override suspend fun pay(request: PaymentRequest): PaymentResult =
        PaymentResult.Authorized(token = "stub-${request.quoteId.value}")
}

/**
 * A booked stay for the phone's calendar: one all-day event from check-in through check-out (the guest is there that
 * morning), with where it is and what to remember.
 */
data class CalendarStay(val title: String, val location: String, val dates: DateRange, val notes: String)

/**
 * Adds a stay to the phone's calendar through the system's own event editor: the guest sees it and confirms, and the
 * app needs no calendar permission (Android's insert screen; iOS's event editor, which since iOS 17 adds events
 * without access). False when there is nowhere to add it, so the screen can say so.
 */
fun interface CalendarSaver {
    fun add(stay: CalendarStay): Boolean
}

/**
 * A notification the guest asked for. [id] (the booking's) replaces or cancels it; it shows at [at]. The words are
 * written when it is scheduled, in the app's language, because nothing of the app runs when it shows.
 */
data class Reminder(val id: String, val at: Instant, val title: String, val body: String)

/**
 * Local notifications for arrival reminders. Nothing leaves the phone: no push service. [ask] shows the system's own
 * question once (Android 13+, iOS); [allowed] says whether the guest has said yes.
 */
interface Reminders {
    suspend fun allowed(): Boolean

    /** Asks the system to let the app notify; true when it may. Only asks when the guest chose to be reminded. */
    suspend fun ask(): Boolean

    fun schedule(reminder: Reminder)

    fun cancel(id: String)
}
