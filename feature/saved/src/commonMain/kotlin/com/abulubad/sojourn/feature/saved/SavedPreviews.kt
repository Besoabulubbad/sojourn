package com.abulubad.sojourn.feature.saved

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SavedScreenPreview() = SojournPreview(SojournVariants.EnLight) { SavedSamples.Screen(SavedSamples.stays) }

@Preview
@Composable
fun SavedScreenEmptyArabicPreview() = SojournPreview(SojournVariants.ArDark) { SavedSamples.Screen(SavedSamples.empty) }
