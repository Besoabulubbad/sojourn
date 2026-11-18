package com.abulubad.sojourn.core.designsystem.components.divider

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournDividerPreview() = SojournPreview(SojournVariants.EnLight) { SojournDividerSamples.Sections() }

@Preview
@Composable
fun SojournDividerArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournDividerSamples.Sections() }
