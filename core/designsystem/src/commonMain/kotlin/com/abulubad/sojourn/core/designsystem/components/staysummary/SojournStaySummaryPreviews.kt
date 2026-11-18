package com.abulubad.sojourn.core.designsystem.components.staysummary

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournStaySummaryPreview() = SojournPreview(SojournVariants.EnLight) { SojournStaySummarySamples.Stays() }

@Preview
@Composable
fun SojournStaySummaryArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournStaySummarySamples.Stays() }
