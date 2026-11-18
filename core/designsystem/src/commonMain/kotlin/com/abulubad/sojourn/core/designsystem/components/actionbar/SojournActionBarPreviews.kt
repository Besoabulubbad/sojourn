package com.abulubad.sojourn.core.designsystem.components.actionbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournActionBarPreview() = SojournPreview(SojournVariants.EnLight) { SojournActionBarSamples.Footers() }

@Preview
@Composable
fun SojournActionBarArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournActionBarSamples.Footers() }
