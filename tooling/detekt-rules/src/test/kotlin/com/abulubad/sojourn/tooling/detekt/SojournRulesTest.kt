package com.abulubad.sojourn.tooling.detekt

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Each rule against a snippet that must fail and one that must pass. */
class SojournRulesTest {

    private fun Rule.findings(code: String, fileName: String = "FeatureScreen.kt") =
        lint(compileContentForTest(code.trimIndent(), fileName))

    @Test
    fun `TokensOnly flags raw colours and unit literals but not tokens`() {
        val rule = TokensOnly(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                val c = Color(0xFF0F4C45)
                val p = 16.dp
                val s = 14.sp
                val n = -2.dp
            }
            """,
        )
        assertEquals(4, bad.size)
        val good = rule.findings(
            """
            fun f() {
                val c = SojournTheme.colors.brand
                val p = SojournSpacing.space4
                val q = p * 2
                val padding = contentPadding.dp
                val none = 0.dp
            }
            """,
        )
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `MotionTokensOnly flags literal durations`() {
        val rule = MotionTokensOnly(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                tween<Float>(300)
                tween<Float>(durationMillis = 250, easing = SojournMotion.standard)
                fadeIn(animationSpec = tween(delayMillis = 100, durationMillis = SojournMotion.fast))
            }
            """,
        )
        assertEquals(3, bad.size)
        val good = rule.findings("fun f() { tween<Float>(SojournMotion.base, easing = SojournMotion.standard) }")
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `StartEndOnly flags absolute and left right APIs and manual mirroring`() {
        val rule = StartEndOnly(Config.empty)
        val bad = rule.findings(
            """
            fun f() {
                Modifier.absoluteOffset(x = x).absolutePadding(left = p)
                val a = AbsoluteAlignment.TopLeft
                val t = TextAlign.Left
                val r = Arrangement.Absolute.Right
                Modifier.graphicsLayer(scaleX = -1f)
                Modifier.graphicsLayer { scaleX = -1f }
            }
            """,
        )
        assertTrue(bad.size >= 6, bad.joinToString("\n") { it.message })
        val good = rule.findings(
            """
            fun f() {
                Modifier.offset(x = x).padding(start = p)
                val a = Alignment.TopStart
                val t = TextAlign.Start
                Modifier.graphicsLayer { scaleX = 1.12f }
            }
            """,
        )
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `NoTextLiterals flags literals templates and concatenation into text`() {
        val rule = NoTextLiterals(Config.empty)
        val bad = rule.findings(
            """
            @Composable fun f(n: Int, name: String) {
                Text("Show stays")
                Text(text = "${'$'}n nights")
                BasicText(name + " stays")
                SojournBodyText("Hello")
            }
            """,
        )
        assertEquals(4, bad.size)
        val good = rule.findings(
            """
            @Composable fun f(n: Int) {
                Text(stringResource(Res.string.action_reserve))
                Text(text = pluralStringResource(Res.plurals.nights, n, formatter.count(n)))
                SojournButton(onClick = {}, label = "x")
            }
            """,
        )
        assertTrue(good.isEmpty(), good.toString())
    }

    @Test
    fun `InsetsOnlyInOwners allows the owner files only`() {
        val code = """
            fun f() {
                Modifier.statusBarsPadding().imePadding()
                val i = WindowInsets.safeDrawing
            }
        """
        assertEquals(3, InsetsOnlyInOwners(Config.empty).findings(code, "ResultsScreen.kt").size)
        assertTrue(InsetsOnlyInOwners(Config.empty).findings(code, "SojournScaffold.kt").isEmpty())
        assertTrue(InsetsOnlyInOwners(Config.empty).findings(code, "/repo/core/ui/src/SojournScaffold.kt").isEmpty())
        val custom = InsetsOnlyInOwners(TestConfig("allowedFiles" to listOf("Custom.kt")))
        assertTrue(custom.findings(code, "Custom.kt").isEmpty())
    }

    @Test
    fun `the provider registers every rule under the sojourn rule set`() {
        val ruleSet = SojournRuleSetProvider().instance(Config.empty)
        assertEquals("sojourn", ruleSet.id)
        assertEquals(
            listOf("TokensOnly", "MotionTokensOnly", "StartEndOnly", "NoTextLiterals", "InsetsOnlyInOwners"),
            ruleSet.rules.map { it.ruleId },
        )
    }
}
