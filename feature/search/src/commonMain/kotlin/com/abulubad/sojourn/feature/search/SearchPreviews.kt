package com.abulubad.sojourn.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SearchWherePreview() = SojournPreview(SojournVariants.EnLight) { SearchSamples.Screen(SearchSamples.where) }

@Preview
@Composable
fun SearchWhenDatesArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SearchSamples.Screen(SearchSamples.whenDates)
}

@Preview
@Composable
fun SearchWhenFlexiblePreview() = SojournPreview(SojournVariants.EnDark) {
    SearchSamples.Screen(SearchSamples.whenFlexible)
}

@Preview
@Composable
fun SearchWhoPreview() = SojournPreview(SojournVariants.EnLight) { SearchSamples.Screen(SearchSamples.whoAnyTime) }
