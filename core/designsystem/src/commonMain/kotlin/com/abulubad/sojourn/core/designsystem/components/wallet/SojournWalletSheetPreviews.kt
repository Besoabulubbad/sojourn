package com.abulubad.sojourn.core.designsystem.components.wallet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournWalletSheetPreview() = SojournPreview(SojournVariants.EnLight) { SojournWalletSheetSamples.Pay() }

@Preview
@Composable
fun SojournWalletSheetArabicPreview() = SojournPreview(SojournVariants.ArDark) { SojournWalletSheetSamples.Pay() }
