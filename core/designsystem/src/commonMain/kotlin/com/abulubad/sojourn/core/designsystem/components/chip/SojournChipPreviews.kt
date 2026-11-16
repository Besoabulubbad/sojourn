package com.abulubad.sojourn.core.designsystem.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournChipRowPreview() = SojournPreview(SojournVariants.EnLight) { SojournChipSamples.Row() }

@Preview
@Composable
fun SojournChipRowArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournChipSamples.Row() }
