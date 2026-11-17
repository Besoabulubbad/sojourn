package com.abulubad.sojourn.core.designsystem.components.continuerow

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournContinueRowPreview() = SojournPreview(SojournVariants.EnLight) { SojournContinueRowSamples.Searches() }

@Preview
@Composable
fun SojournContinueRowArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournContinueRowSamples.Searches() }
