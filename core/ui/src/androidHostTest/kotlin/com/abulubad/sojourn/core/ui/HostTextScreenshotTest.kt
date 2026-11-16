package com.abulubad.sojourn.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.testing.SojournScreenshotTest
import com.abulubad.sojourn.core.testing.StayFixtures
import org.junit.Test

/** S-06: host text keeps its own direction whatever the UI language. */
class HostTextScreenshotTest(variant: SojournVariant) : SojournScreenshotTest(variant) {

    @Test
    fun hostTextKeepsItsDirection() = snapshot("host_text") {
        Column(
            modifier = Modifier.fillMaxWidth().padding(SojournSpacing.space4),
            verticalArrangement = Arrangement.spacedBy(SojournSpacing.space3),
        ) {
            HostText(StayFixtures.canalStudio.title, Modifier.fillMaxWidth())
            HostText(StayFixtures.arabicTitleStay.title, Modifier.fillMaxWidth())
        }
    }
}
