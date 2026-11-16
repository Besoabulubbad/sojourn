package com.abulubad.sojourn.core.designsystem.components.policy

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.abulubad.sojourn.core.designsystem.preview.SojournPreview
import com.abulubad.sojourn.core.designsystem.preview.SojournVariants

@Preview
@Composable
fun SojournPolicyRowPreview() = SojournPreview(SojournVariants.EnLight) {
    SojournPolicyRowSamples.Policies()
}

@Preview
@Composable
fun SojournPolicyRowArabicPreview() = SojournPreview(SojournVariants.ArDark) {
    SojournPolicyRowSamples.Policies()
}
