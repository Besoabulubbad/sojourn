package com.abulubad.sojourn.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.abulubad.sojourn.core.designsystem.tokens.SojournColors
import com.abulubad.sojourn.core.designsystem.tokens.darkSojournColors
import com.abulubad.sojourn.core.designsystem.tokens.lightSojournColors
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * C-02: every text pair named in the tokens' usage notes meets WCAG 4.5:1; control borders, icons and the focus
 * ring meet 3:1 — in both themes. Runs on every change to `design/tokens.json` because the colours are generated.
 */
class ContrastTest {

    private class Pair(val foreground: String, val background: String, val minimum: Double)

    /** Foreground token on background token → required ratio, straight from the usage notes. */
    private val pairs: List<Pair> = buildList {
        for (bg in listOf("bg", "surface", "surface-sunken")) {
            add(Pair("ink", bg, TEXT))
            add(Pair("ink-muted", bg, TEXT))
            add(Pair("focus", bg, GRAPHIC))
        }
        for (bg in listOf("bg", "surface")) {
            add(Pair("ink-subtle", bg, TEXT))
            add(Pair("brand-ink", bg, TEXT))
            add(Pair("accent-ink", bg, TEXT))
            add(Pair("danger", bg, TEXT))
            add(Pair("border-control", bg, GRAPHIC))
            add(Pair("save", bg, GRAPHIC))
        }
        add(Pair("on-brand", "brand", TEXT))
        add(Pair("brand-ink", "brand-tint", TEXT))
        add(Pair("ink", "brand-tint", TEXT))
        add(Pair("on-accent", "accent", TEXT))
        add(Pair("ink", "accent-tint", TEXT))
        add(Pair("danger", "danger-tint", TEXT))
        add(Pair("on-danger", "danger-fill", TEXT))
    }

    @Test
    fun `light theme pairs meet their minimum contrast`() = assertPairs("light", lightSojournColors())

    @Test
    fun `dark theme pairs meet their minimum contrast`() = assertPairs("dark", darkSojournColors())

    private fun assertPairs(theme: String, colors: SojournColors) {
        val failures = pairs.mapNotNull { pair ->
            val ratio = contrast(colors.all.getValue(pair.foreground), colors.all.getValue(pair.background))
            val label = "$theme: ${pair.foreground} on ${pair.background}"
            if (ratio < pair.minimum) "$label = ${ratio.format()} < ${pair.minimum}" else null
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    private fun contrast(foreground: Color, background: Color): Double {
        val fg = foreground.compositeOver(background)
        val l1 = fg.luminance() + OFFSET
        val l2 = background.luminance() + OFFSET
        return maxOf(l1, l2) / minOf(l1, l2)
    }

    /** WCAG 2.x relative luminance from sRGB. */
    private fun Color.luminance(): Double {
        fun channel(c: Float): Double {
            val v = c.toDouble()
            return if (v <= LINEAR_THRESHOLD) v / LINEAR_DIVISOR else ((v + GAMMA_OFFSET) / GAMMA_DIVISOR).pow(GAMMA)
        }
        return R_WEIGHT * channel(red) + G_WEIGHT * channel(green) + B_WEIGHT * channel(blue)
    }

    private fun Double.format(): String = ((this * HUNDRED).toInt() / HUNDRED).toString()

    private companion object {
        const val TEXT = 4.5
        const val GRAPHIC = 3.0
        const val OFFSET = 0.05
        const val LINEAR_THRESHOLD = 0.04045
        const val LINEAR_DIVISOR = 12.92
        const val GAMMA_OFFSET = 0.055
        const val GAMMA_DIVISOR = 1.055
        const val GAMMA = 2.4
        const val R_WEIGHT = 0.2126
        const val G_WEIGHT = 0.7152
        const val B_WEIGHT = 0.0722
        const val HUNDRED = 100.0
    }
}
