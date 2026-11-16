package com.abulubad.sojourn.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme

/**
 * Renders [content] exactly as one [SojournVariant] of the matrix: theme, language and direction from
 * `SojournTheme`, the font scale through `LocalDensity`, and the width as a hard constraint. Used by every
 * `@Preview` and by `SojournScreenshotTest`, so previews and baselines are the same pixels.
 */
@Composable
fun SojournPreview(variant: SojournVariant, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, variant.fontScale)) {
        SojournTheme(locale = variant.locale, darkTheme = variant.darkTheme) {
            Box(
                modifier = Modifier
                    .width(variant.widthDp.dp)
                    .background(SojournTheme.colors.bg),
            ) {
                content()
            }
        }
    }
}
