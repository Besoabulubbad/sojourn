package com.abulubad.sojourn.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun ProfileScreenPreview() = SojournPreview(SojournVariants.EnLight) { ProfileSamples.Profile() }

@Preview
@Composable
fun ProfileScreenArabicPreview() = SojournPreview(SojournVariants.ArDark) { ProfileSamples.Profile() }

@Preview
@Composable
fun RewardsScreenPreview() = SojournPreview(SojournVariants.EnLight) {
    ProfileSamples.Rewards(ProfileSamples.rewardsContent)
}
