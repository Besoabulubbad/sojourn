package com.abulubad.sojourn.core.designsystem.components.actionbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_next
import com.abulubad.sojourn.core.localization.resources.action_pay
import com.abulubad.sojourn.core.localization.resources.action_search
import com.abulubad.sojourn.core.localization.resources.nights
import com.abulubad.sojourn.core.localization.resources.search_any_time
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Money
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The search flow's footers (When with a range chosen, When before one is, and Who) and checkout's. */
object SojournActionBarSamples {

    @Composable
    fun Footers(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        val next = stringResource(Res.string.action_next)
        val anyTime = stringResource(Res.string.search_any_time)
        Column(
            modifier.padding(vertical = SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            SojournActionBar(
                primary = next,
                onPrimary = {},
                secondary = anyTime,
                note = pluralStringResource(Res.plurals.nights, NIGHTS, formatter.count(NIGHTS)),
            )
            SojournActionBar(primary = next, onPrimary = {}, primaryEnabled = false, secondary = anyTime)
            SojournActionBar(primary = stringResource(Res.string.action_search), onPrimary = {})
        }
    }

    /** Checkout's footer: "Pay £1,383" across the bar, then while paying. */
    @Composable
    fun Pay(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        val pay = stringResource(Res.string.action_pay, formatter.money(Money.of(PAY_TOTAL, Currency.GBP)))
        Column(
            modifier.padding(vertical = SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space4),
        ) {
            SojournActionBar(primary = pay, onPrimary = {}, fillPrimary = true)
            SojournActionBar(primary = pay, onPrimary = {}, fillPrimary = true, primaryLoading = true)
        }
    }

    private const val NIGHTS = 15
    private const val PAY_TOTAL = 1_383L
}
