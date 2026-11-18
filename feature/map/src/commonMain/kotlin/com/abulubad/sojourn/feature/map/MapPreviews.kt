package com.abulubad.sojourn.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun MapScreenPreview() = SojournPreview(SojournVariants.EnLight) { MapSamples.Screen(MapSamples.selected) }

@Preview
@Composable
fun MapScreenArabicPreview() = SojournPreview(SojournVariants.ArDark) { MapSamples.Screen(MapSamples.moved) }
