package com.abulubad.sojourn.core.platform

import com.abulubad.sojourn.core.model.GeoPoint
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.QuoteId

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
