package com.abulubad.sojourn.buildlogic.tokens

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Runs the generator against the real `design/tokens.json` (path injected by the build) and checks the
 * one-property-per-token guarantee that ADR 0008 promises, so a renamed or malformed token fails here first.
 */
class DesignTokenCodegenTest {

    private val tokens = DesignTokens.parse(File(System.getProperty("sojourn.tokensFile")).readText())
    private val files = DesignTokenCodegen(tokens, "com.example.tokens").files()

    @Test
    fun `every colour token becomes one constructor property in both themes`() {
        val colors = files.getValue("SojournColors.kt")
        val properties = Regex("""^\s{4}val (\w+): Color,$""", RegexOption.MULTILINE).findAll(colors).count()
        assertEquals(tokens.colors.size, properties)
        tokens.themes.forEach { theme -> assertTrue("fun ${theme}SojournColors(): SojournColors" in colors, theme) }
        assertTrue("surfaceSunken" in colors && "onBrand" in colors, "kebab-case names map to lowerCamel")
    }

    @Test
    fun `colour values are emitted as Compose literals`() {
        val colors = files.getValue("SojournColors.kt")
        assertTrue("Color(0xFFFAF7F2)" in colors, "hex")
        assertTrue(Regex("""Color\(red = \d+, green = \d+, blue = \d+, alpha = \d+\)""").containsMatchIn(colors), "rgba")
    }

    @Test
    fun `every type style becomes a TextStyle with its numbers`() {
        val type = files.getValue("SojournType.kt")
        val properties = Regex("""^\s{4}val (\w+): TextStyle,$""", RegexOption.MULTILINE).findAll(type).count()
        assertEquals(tokens.typeStyles.size, properties)
        assertTrue("fun sojournType(display: FontFamily, text: FontFamily): SojournType" in type)
        assertTrue("letterSpacing = (-0.01).em" in type, "display letter spacing")
        assertTrue("fontFeatureSettings = \"tnum\"" in type, "tabular figures everywhere")
    }

    @Test
    fun `spacing sizes radii shadows durations and easings all map one to one`() {
        fun count(file: String, pattern: String) = Regex(pattern, RegexOption.MULTILINE).findAll(files.getValue(file)).count()
        assertEquals(tokens.spacing.size, count("SojournSpacing.kt", """^\s{4}val (\w+): Dp = \d+\.dp$"""))
        assertEquals(tokens.sizes.size, count("SojournSizes.kt", """^\s{4}val (\w+): Dp = \d+\.dp$"""))
        assertEquals(tokens.radius.size, count("SojournShapes.kt", """^\s{4}val (\w+): RoundedCornerShape = RoundedCornerShape\(\d+\.dp\)$"""))
        assertEquals(tokens.shadows.size, count("SojournElevation.kt", """^\s{4}val level\d: SojournShadow,$"""))
        assertEquals(tokens.durations.size, count("SojournMotion.kt", """^\s{4}const val (\w+): Int = \d+$"""))
        assertEquals(tokens.easings.size, count("SojournMotion.kt", """^\s{4}val (\w+): Easing = CubicBezierEasing\("""))
    }

    @Test
    fun `shadows map blur to elevation and keep the tint`() {
        val elevation = files.getValue("SojournElevation.kt")
        // shadow-3 light: 0 8px 24px rgba(27, 36, 33, 0.16) → 12dp, alpha 41/255
        assertTrue("level3 = SojournShadow(elevation = 12.dp, color = Color(red = 27, green = 36, blue = 33, alpha = 41))" in elevation)
    }

    @Test
    fun `motion strips the dur and ease prefixes`() {
        val motion = files.getValue("SojournMotion.kt")
        assertTrue("const val slower: Int = 500" in motion)
        assertTrue("val emphasized: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)" in motion)
    }
}
