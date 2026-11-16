package com.abulubad.sojourn.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.sojourn.core.designsystem.resources.Res
import com.abulubad.sojourn.core.designsystem.resources.alexandria_medium
import com.abulubad.sojourn.core.designsystem.resources.alexandria_regular
import com.abulubad.sojourn.core.designsystem.resources.alexandria_semibold
import com.abulubad.sojourn.core.designsystem.resources.readexpro_medium
import com.abulubad.sojourn.core.designsystem.resources.readexpro_regular
import com.abulubad.sojourn.core.designsystem.resources.readexpro_semibold
import org.jetbrains.compose.resources.Font

/**
 * The two families (brand book): Alexandria for display, titles and the price; Readex Pro for everything else.
 * Both cover Latin and Arabic, so one stack serves both languages. Bundled as static instances at the three
 * weights the tokens use (400, 500, 600); see `core/designsystem/THIRD_PARTY.md`.
 */
object SojournFonts {

    @Composable
    fun alexandria(): FontFamily = FontFamily(
        Font(Res.font.alexandria_regular, FontWeight.Normal),
        Font(Res.font.alexandria_medium, FontWeight.Medium),
        Font(Res.font.alexandria_semibold, FontWeight.SemiBold),
    )

    @Composable
    fun readexPro(): FontFamily = FontFamily(
        Font(Res.font.readexpro_regular, FontWeight.Normal),
        Font(Res.font.readexpro_medium, FontWeight.Medium),
        Font(Res.font.readexpro_semibold, FontWeight.SemiBold),
    )
}
