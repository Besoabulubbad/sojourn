package com.abulubad.sojourn.core.designsystem.components.skeleton

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournSkeletonPreview() = SojournPreview(SojournVariants.EnLight) { SojournSkeletonSamples.StayCard() }

@Preview
@Composable
fun SojournSkeletonArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournSkeletonSamples.StayCard() }
