package com.abulubad.sojourn.core.designsystem.components.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.SampleListings
import com.abulubad.sojourn.core.designsystem.components.sheet.SojournSheetDetent
import com.abulubad.sojourn.core.designsystem.components.sheet.rememberSojournSheetState
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.action_pay
import org.jetbrains.compose.resources.stringResource

/** The demo wallet over checkout, at full height, for the card's £1,383. */
object SojournWalletSheetSamples {

    @Composable
    fun Pay(modifier: Modifier = Modifier) {
        val formatter = rememberSojournFormatter()
        val total = SampleListings.canalTotal
        Box(modifier.fillMaxWidth().height(HEIGHT).background(SojournTheme.colors.bg)) {
            SojournWalletSheet(
                state = rememberSojournSheetState(SojournSheetDetent.Full, setOf(SojournSheetDetent.Full)),
                price = total,
                payLabel = stringResource(Res.string.action_pay, formatter.money(total.amount)),
                onPay = {},
                onDismiss = {},
            )
        }
    }

    private val HEIGHT: Dp = 640.dp
}
