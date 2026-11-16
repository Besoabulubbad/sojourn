package com.abulubad.sojourn.core.designsystem

import com.abulubad.sojourn.core.designsystem.preview.SojournVariant
import com.abulubad.sojourn.core.designsystem.showcase.SojournTokenSheet
import com.abulubad.sojourn.core.designsystem.showcase.TokenSection
import com.abulubad.sojourn.core.testing.SojournScreenshotTest
import org.junit.Test

/**
 * Phase 2 exit: `SojournTheme` renders every token in the four shipped variants (EN/AR × light/dark).
 * One image per token family so nothing is clipped by the device height; the baselines double as the
 * showcase swatch sheet.
 */
class TokenSheetScreenshotTest(variant: SojournVariant) : SojournScreenshotTest(variant) {

    @Test fun colors() = snapshot("tokens_color") { SojournTokenSheet(TokenSection.Color) }

    @Test fun type() = snapshot("tokens_type") { SojournTokenSheet(TokenSection.Type) }

    @Test fun spacing() = snapshot("tokens_spacing") { SojournTokenSheet(TokenSection.Spacing) }

    @Test fun shapes() = snapshot("tokens_shape") { SojournTokenSheet(TokenSection.Shape) }

    @Test fun icons() = snapshot("tokens_icons") { SojournTokenSheet(TokenSection.Icons) }
}
