package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * The only haptics in the app (motion.md, Haptics): a light [tick] for save, a stepper change, a date selected and
 * a sheet reaching a stop; [success] when payment is confirmed; [error] when it fails. Nothing else vibrates.
 * Reduce motion does not affect haptics.
 */
@Stable
class SojournHaptics internal constructor(private val feedback: HapticFeedback) {
    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

    fun success() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)

    fun error() = feedback.performHapticFeedback(HapticFeedbackType.Reject)
}

/** The platform's haptic feedback, limited to the three Sojourn patterns. */
@Composable
fun rememberSojournHaptics(): SojournHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { SojournHaptics(feedback) }
}
