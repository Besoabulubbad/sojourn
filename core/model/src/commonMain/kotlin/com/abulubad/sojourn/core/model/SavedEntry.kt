package com.abulubad.sojourn.core.model

import kotlin.time.Instant

/**
 * A saved stay and the search it was saved from (ScreenSaved card: cards keep the dates of the search they were saved
 * from). [dates] is null when that search had none; [savedAt] orders the Saved tab, newest first.
 */
data class SavedEntry(val stayId: StayId, val dates: DateRange?, val guests: Guests, val savedAt: Instant)
