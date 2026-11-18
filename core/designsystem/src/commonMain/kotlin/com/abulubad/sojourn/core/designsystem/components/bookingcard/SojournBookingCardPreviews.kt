package com.abulubad.sojourn.core.designsystem.components.bookingcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournBookingCardPreview() = SojournPreview(SojournVariants.EnLight) { SojournBookingCardSamples.Upcoming() }

@Preview
@Composable
fun SojournBookingCardArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournBookingCardSamples.Upcoming() }
