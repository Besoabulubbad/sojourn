package com.abulubad.sojourn.core.designsystem.components.listrow

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournListRowPreview() = SojournPreview(SojournVariants.EnLight) { SojournListRowSamples.Rows() }

@Preview
@Composable
fun SojournListRowArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournListRowSamples.Rows() }
