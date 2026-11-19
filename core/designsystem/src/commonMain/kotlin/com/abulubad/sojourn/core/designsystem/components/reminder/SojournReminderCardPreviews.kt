package com.abulubad.sojourn.core.designsystem.components.reminder

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournReminderCardPreview() = SojournPreview(SojournVariants.EnLight) { SojournReminderCardSamples.States() }

@Preview
@Composable
fun SojournReminderCardArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournReminderCardSamples.States() }
