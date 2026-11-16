package com.abulubad.sojourn.tooling.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/** Base for the Sojourn rules: one issue per rule, reported with the standard's id in the message. */
abstract class SojournRule(config: Config, private val description: String) : Rule(config) {
    override val issue: Issue = Issue(javaClass.simpleName, Severity.Style, description, Debt.FIVE_MINS)

    protected fun report(element: KtElement, message: String) {
        report(CodeSmell(issue, Entity.from(element), message))
    }
}

/**
 * C-01: colours, sizes and type come from tokens. Flags `Color(…)` construction and `.dp` / `.sp` / `.em` on a
 * number literal (`16.dp`). Use `SojournTheme.colors.*`, `SojournSpacing.*`, `SojournSizes.*`, `SojournTheme.type.*`.
 */
class TokensOnly(config: Config) : SojournRule(
    config,
    "Raw colours, dp and sp literals are not allowed; use tokens (C-01).",
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == "Color") {
            report(expression, "Raw Color(…) — use a SojournTheme.colors token (C-01).")
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val unit = (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        val receiver = expression.receiverExpression
        // Zero is the absence of a value, not a design decision: `0.dp` is allowed.
        val isZero = receiver.text.trimEnd('f', 'F').toDoubleOrNull() == 0.0
        if (unit in UNITS && receiver.isNumberLiteral() && !isZero) {
            report(
                expression,
                "Literal ${expression.text} — use SojournSpacing, SojournSizes or SojournTheme.type (C-01).",
            )
        }
    }

    private companion object {
        val UNITS = setOf("dp", "sp", "em")
    }
}

/**
 * M-01: durations and easings come from `SojournMotion`. Flags a number literal passed as `durationMillis` or
 * `delayMillis`, or as the first argument of `tween(…)`.
 */
class MotionTokensOnly(config: Config) : SojournRule(
    config,
    "Literal animation durations are not allowed; use SojournMotion (M-01).",
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        expression.valueArguments.forEachIndexed { index, argument ->
            val name = argument.getArgumentName()?.asName?.asString()
            val isTween = expression.calleeExpression?.text == "tween"
            val isDuration = name in MILLIS_PARAMETERS || (name == null && index == 0 && isTween)
            if (isDuration && argument.getArgumentExpression().isNumberLiteral()) {
                report(argument, "Literal ${argument.text} ms — use a SojournMotion duration (M-01).")
            }
        }
    }

    private companion object {
        val MILLIS_PARAMETERS = setOf("durationMillis", "delayMillis")
    }
}

/**
 * L-02: layout uses start and end only. Flags the absolute (left/right) APIs and manual mirroring with
 * `scaleX = -1f`; directional icons mirror inside `SojournIcon`.
 */
class StartEndOnly(config: Config) : SojournRule(
    config,
    "Use start/end, never left/right or manual mirroring (L-02).",
) {

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        val name = (expression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (name in ABSOLUTE_APIS) {
            report(expression, "$name is left/right-absolute — use the start/end equivalent (L-02).")
        }
        val parent = expression.parent
        val onDirectionalType = parent is KtDotQualifiedExpression &&
            parent.receiverExpression.text in DIRECTIONAL_RECEIVERS
        if (name in LEFT_RIGHT && onDirectionalType) {
            report(expression, "${parent.text} — use Start/End so the layout mirrors in Arabic (L-02).")
        }
    }

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        val isScaleX = argument.getArgumentName()?.asName?.asString() == "scaleX"
        if (isScaleX && argument.getArgumentExpression().isNegativeOne()) {
            report(
                argument,
                "scaleX = -1f mirrors by hand — mark the icon autoMirror in SojournIcon instead (L-02, L-05).",
            )
        }
    }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val assignsScaleX = expression.operationReference.text == "=" && expression.left?.text == "scaleX"
        if (assignsScaleX && expression.right.isNegativeOne()) {
            report(
                expression,
                "scaleX = -1f mirrors by hand — mark the icon autoMirror in SojournIcon instead (L-02, L-05).",
            )
        }
    }

    private companion object {
        val ABSOLUTE_APIS = setOf(
            "AbsoluteAlignment",
            "absoluteOffset",
            "absolutePadding",
            "AbsoluteRoundedCornerShape",
            "AbsoluteCutCornerShape",
        )
        val LEFT_RIGHT = setOf("Left", "Right", "Absolute")
        val DIRECTIONAL_RECEIVERS = setOf("TextAlign", "Arrangement", "Alignment")
    }
}

/**
 * S-01 / S-02: user-facing text comes from resources, never literals, templates or concatenation. Flags a string
 * literal, string template or `+` concatenation passed as the text of `Text`, `BasicText` or a `Sojourn*Text`.
 */
class NoTextLiterals(config: Config) : SojournRule(config, "UI text comes from string resources (S-01, S-02).") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text.orEmpty()
        val isSojournText = callee.startsWith("Sojourn") && callee.endsWith("Text")
        if (callee !in TEXT_COMPOSABLES && !isSojournText) return
        val arguments = expression.valueArguments
        val textArgument = arguments.firstOrNull { it.getArgumentName()?.asName?.asString() == "text" }
            ?: arguments.firstOrNull()?.takeIf { it.getArgumentName() == null }
        val value = textArgument?.getArgumentExpression()
        if (textArgument != null && (value is KtStringTemplateExpression || value?.isStringConcatenation() == true)) {
            report(
                textArgument,
                "Text from a literal or concatenation — use stringResource / pluralStringResource (S-01, S-02).",
            )
        }
    }

    private fun KtExpression.isStringConcatenation(): Boolean {
        if (this !is KtBinaryExpression || operationReference.text != "+") return false
        return left is KtStringTemplateExpression ||
            right is KtStringTemplateExpression ||
            left?.isStringConcatenation() == true
    }

    private companion object {
        val TEXT_COMPOSABLES = setOf("Text", "BasicText")
    }
}

/**
 * I-01: system insets are consumed only by the scaffold and the docked/overlay components that own them. Flags inset
 * modifiers and `WindowInsets` anywhere else; the owner files are configured as `allowedFiles`.
 */
class InsetsOnlyInOwners(config: Config) : SojournRule(
    config,
    "Only the inset-owning components consume system insets (I-01).",
) {

    private val allowedFiles: List<String> by config(DEFAULT_OWNERS)

    override fun visitKtFile(file: KtFile) {
        // detekt hands rules the path of the file in a real run and the bare name in tests.
        if (file.name.substringAfterLast('/').substringAfterLast('\\') in allowedFiles) return
        super.visitKtFile(file)
    }

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        val name = (expression as? KtNameReferenceExpression)?.getReferencedName() ?: return
        if (name in INSET_APIS) {
            report(
                expression,
                "$name outside an inset owner — let SojournScaffold and the bars handle insets (I-01, I-03).",
            )
        }
    }

    private companion object {
        val DEFAULT_OWNERS = listOf(
            "SojournScaffold.kt",
            "SojournTopBar.kt",
            "SojournBottomNav.kt",
            "SojournBookingBar.kt",
            "SojournSheet.kt",
        )
        val INSET_APIS = setOf(
            "WindowInsets", "windowInsetsPadding", "statusBarsPadding", "navigationBarsPadding", "systemBarsPadding",
            "imePadding", "safeDrawingPadding", "safeContentPadding", "safeGesturesPadding", "displayCutoutPadding",
            "consumeWindowInsets", "windowInsetsTopHeight", "windowInsetsBottomHeight",
        )
    }
}

private fun KtExpression?.isNumberLiteral(): Boolean = when (this) {
    is KtConstantExpression -> text.firstOrNull()?.isDigit() == true
    is KtPrefixExpression -> operationReference.text == "-" && baseExpression.isNumberLiteral()
    else -> false
}

private fun KtExpression?.isNegativeOne(): Boolean =
    this is KtPrefixExpression && operationReference.text == "-" &&
        baseExpression?.text?.trimEnd('f', 'F')?.toDoubleOrNull() == 1.0
