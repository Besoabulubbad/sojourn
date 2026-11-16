package com.abulubad.sojourn.tooling.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * The design system's engineering standards as lint (ADR 0014). Each rule names the standard it enforces; where it
 * applies (features and `core:ui`, or everywhere but the inset owners) is set in `config/detekt/detekt.yml`.
 */
class SojournRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = RULE_SET_ID

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            TokensOnly(config),
            MotionTokensOnly(config),
            StartEndOnly(config),
            NoTextLiterals(config),
            InsetsOnlyInOwners(config),
        ),
    )

    companion object {
        const val RULE_SET_ID = "sojourn"
    }
}
