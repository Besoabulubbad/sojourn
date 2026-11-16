package com.abulubad.sojourn.core.designsystem.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournButtonVariantsPreview() = SojournPreview(SojournVariants.EnLight) { SojournButtonSamples.Variants() }

@Preview
@Composable
fun SojournButtonStatesArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournButtonSamples.States() }
