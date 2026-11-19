package com.abulubad.sojourn.core.designsystem.components.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import kotlinx.datetime.LocalDateTime

/** The three states: offered, set for the evening before 12 March, and blocked in the phone's settings. */
object SojournReminderCardSamples {

    @Composable
    fun States(modifier: Modifier = Modifier) {
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournReminderCard(SojournReminderState.Offer, onRemind = {})
            SojournReminderCard(SojournReminderState.Set(LocalDateTime.parse(EVENING_BEFORE)), onRemind = {})
            SojournReminderCard(SojournReminderState.Blocked, onRemind = {})
        }
    }

    private const val EVENING_BEFORE = "2027-03-11T18:00"
}
