package com.abulubad.sojourn.core.designsystem.components.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme

/**
 * A full-width `hairline` rule between sections of one surface, such as the filter sheet's price, rooms and
 * amenities (ScreenFilters card). Decorative: screen readers skip it, as the sections carry headings.
 */
@Composable
fun SojournDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(SojournDividerDefaults.Thickness).background(SojournTheme.colors.hairline))
}
