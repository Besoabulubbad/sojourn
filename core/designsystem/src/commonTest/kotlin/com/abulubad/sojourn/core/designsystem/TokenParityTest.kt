package com.abulubad.sojourn.core.designsystem

import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.tokens.SojournColors
import com.abulubad.sojourn.core.designsystem.tokens.SojournMotion
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.designsystem.tokens.darkSojournColors
import com.abulubad.sojourn.core.designsystem.tokens.darkSojournElevation
import com.abulubad.sojourn.core.designsystem.tokens.lightSojournColors
import com.abulubad.sojourn.core.designsystem.tokens.lightSojournElevation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The generated objects expose every token exactly once and both themes agree on the token set (ADR 0008). */
class TokenParityTest {

    @Test
    fun `both colour themes expose every token name`() {
        val light = lightSojournColors()
        val dark = darkSojournColors()
        assertEquals(SojournColors.tokenNames, light.all.keys.toList())
        assertEquals(SojournColors.tokenNames, dark.all.keys.toList())
        assertEquals(SojournColors.tokenNames.size, SojournColors.tokenNames.toSet().size, "no duplicate names")
        assertTrue(SojournColors.tokenNames.containsAll(listOf("bg", "surface", "ink", "brand", "on-brand", "save")))
    }

    @Test
    fun `dark is a full theme not an inversion`() {
        val light = lightSojournColors()
        val dark = darkSojournColors()
        val same = SojournColors.tokenNames.filter { light.all.getValue(it) == dark.all.getValue(it) }
        // Saffron and its text are the only tokens shared between themes by design.
        assertEquals(listOf("accent", "on-accent"), same)
    }

    @Test
    fun `spacing radii sizes shadows and motion are complete`() {
        assertEquals(
            listOf("space-1", "space-2", "space-3", "space-4", "space-5", "space-6", "space-8", "space-10"),
            SojournSpacing.all.keys.toList(),
        )
        assertEquals(
            listOf("radius-xs", "radius-sm", "radius-md", "radius-lg", "radius-xl", "radius-full"),
            SojournShapes.all.keys.toList(),
        )
        assertEquals(
            listOf("touch-min", "icon", "icon-sm", "bar-top", "bar-bottom", "booking-bar"),
            SojournSizes.all.keys.toList(),
        )
        assertEquals(listOf("shadow-1", "shadow-2", "shadow-3"), lightSojournElevation().all.keys.toList())
        assertEquals(lightSojournElevation().all.keys, darkSojournElevation().all.keys)
        assertEquals(
            listOf("dur-instant", "dur-fast", "dur-base", "dur-slow", "dur-slower"),
            SojournMotion.durations.keys.toList(),
        )
        assertEquals(listOf("ease-standard", "ease-emphasized", "ease-exit"), SojournMotion.easings.keys.toList())
    }

    @Test
    fun `nothing runs longer than slower and exits are shorter`() {
        assertEquals(SojournMotion.slower, SojournMotion.durations.values.max())
        assertTrue(SojournMotion.durations.values.zipWithNext().all { (a, b) -> a < b }, "durations are ordered")
    }

    @Test
    fun `only back and the chevrons mirror`() {
        val mirrored = SojournIcon.all.filter { it.autoMirror }
        assertEquals(
            listOf(SojournIcon.Back, SojournIcon.Forward, SojournIcon.ChevronStart, SojournIcon.ChevronEnd),
            mirrored,
        )
        assertEquals(SojournIcon.all.size, SojournIcon.all.toSet().size, "one icon per meaning")
    }
}
