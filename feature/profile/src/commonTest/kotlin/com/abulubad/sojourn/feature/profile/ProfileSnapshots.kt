package com.abulubad.sojourn.feature.profile

import com.abulubad.sojourn.core.testing.SnapshotCase

/**
 * The Profile tab's frames (ScreenProfile card) on Android and iOS: Profile in the full matrix, Language, Appearance,
 * Rewards with points (full matrix) and before any, and Help.
 */
val profileSnapshots: List<SnapshotCase> = listOf(
    SnapshotCase("profile", fullMatrix = true) { ProfileSamples.Profile() },
    SnapshotCase("profile_language") { ProfileSamples.Language() },
    SnapshotCase("profile_appearance") { ProfileSamples.Appearance() },
    SnapshotCase("rewards", fullMatrix = true) { ProfileSamples.Rewards(ProfileSamples.rewardsContent) },
    SnapshotCase("rewards_empty") { ProfileSamples.Rewards(ProfileSamples.rewardsEmpty) },
    SnapshotCase("help") { ProfileSamples.Help() },
)
