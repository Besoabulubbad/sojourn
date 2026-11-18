package com.abulubad.sojourn.feature.bookings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun BookingsScreenPreview() = SojournPreview(SojournVariants.EnLight) { BookingsSamples.Tab(BookingsSamples.upcoming) }

@Preview
@Composable
fun BookingsScreenArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    BookingsSamples.Tab(BookingsSamples.upcoming)
}

@Preview
@Composable
fun BookingDetailsScreenPreview() = SojournPreview(SojournVariants.EnDark) {
    BookingsSamples.Details(BookingsSamples.details)
}
