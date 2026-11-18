package com.abulubad.sojourn.core.designsystem.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournMapPreview() = SojournPreview(SojournVariants.EnLight) { SojournMapSamples.CityAndArea() }

@Preview
@Composable
fun SojournMapArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournMapSamples.CityAndArea() }
