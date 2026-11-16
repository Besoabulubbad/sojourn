package com.abulubad.sojourn.core.designsystem.components.stepper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.guests_adults
import com.abulubad.sojourn.core.localization.resources.guests_children
import com.abulubad.sojourn.core.localization.resources.guests_children_hint
import org.jetbrains.compose.resources.stringResource

/** The Stepper card's example: adults at 2, children at their lower bound (minus disabled). */
object SojournStepperSamples {

    val AdultsRange = 1..16
    val ChildrenRange = 0..10

    @Composable
    fun Guests(modifier: Modifier = Modifier) {
        var adults by remember { mutableIntStateOf(2) }
        var children by remember { mutableIntStateOf(0) }
        Column(
            modifier.padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            SojournStepper(adults, { adults = it }, AdultsRange, stringResource(Res.string.guests_adults))
            SojournStepper(
                children,
                { children = it },
                ChildrenRange,
                stringResource(Res.string.guests_children),
                hint = stringResource(Res.string.guests_children_hint),
            )
        }
    }
}
