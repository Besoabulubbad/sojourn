package com.abulubad.sojourn.core.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * "Now" and "today", injected so use cases and tests control time. Wraps the standard library's [Clock] with the
 * time zone the app reasons in.
 */
interface AppClock {
    val timeZone: TimeZone
    fun now(): Instant
    fun today(): LocalDate = now().todayIn(timeZone)
}

/** The device clock in the device time zone. */
class SystemAppClock : AppClock {
    override val timeZone: TimeZone get() = TimeZone.currentSystemDefault()
    override fun now(): Instant = Clock.System.now()
}

private fun Instant.todayIn(timeZone: TimeZone): LocalDate = FixedInstantClock(this).todayIn(timeZone)

private class FixedInstantClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
