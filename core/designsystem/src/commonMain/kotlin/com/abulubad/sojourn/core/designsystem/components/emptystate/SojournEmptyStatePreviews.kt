package com.abulubad.sojourn.core.designsystem.components.emptystate

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournEmptyStatePreview() = SojournPreview(SojournVariants.EnLight) { SojournEmptyStateSamples.NoMatches() }

@Preview
@Composable
fun SojournEmptyStateArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournEmptyStateSamples.NoMatches() }
