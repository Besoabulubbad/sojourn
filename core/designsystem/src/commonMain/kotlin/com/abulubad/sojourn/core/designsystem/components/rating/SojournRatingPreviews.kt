package com.abulubad.sojourn.core.designsystem.components.rating

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing

@Preview
@Composable
fun SojournRatingPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournRating(SojournRatingSamples.typical, Modifier.padding(SojournSpacing.space4))
}

@Preview
@Composable
fun SojournRatingCompactArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournRating(SojournRatingSamples.typical, Modifier.padding(SojournSpacing.space4), showCount = false)
}
