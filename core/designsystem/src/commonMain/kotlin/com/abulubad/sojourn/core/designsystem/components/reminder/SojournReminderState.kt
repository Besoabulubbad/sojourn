package com.abulubad.sojourn.core.designsystem.components.reminder

import kotlinx.datetime.LocalDateTime

/** Where the arrival reminder stands for one booking. */
sealed interface SojournReminderState {
    /** Not set yet: the card offers "Remind me" and says what it will do. */
    data object Offer : SojournReminderState

    /** Scheduled for [at], on the phone's clock. */
    data class Set(val at: LocalDateTime) : SojournReminderState

    /** The guest said no to notifications: the card says where to turn them on. */
    data object Blocked : SojournReminderState
}
