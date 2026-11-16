package com.abulubad.sojourn.core.designsystem.components.keyfacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.abulubad.sojourn.core.designsystem.components.internal.ShareRow
import com.abulubad.sojourn.core.designsystem.icon.SojournIcon
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSizes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.bathrooms
import com.abulubad.sojourn.core.localization.resources.bedrooms
import com.abulubad.sojourn.core.localization.resources.beds
import com.abulubad.sojourn.core.localization.resources.facts_separator
import com.abulubad.sojourn.core.localization.resources.facts_sleeps
import com.abulubad.sojourn.core.localization.resources.facts_studio
import com.abulubad.sojourn.core.model.StayFacts
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The four facts under a listing's title (KeyFacts card): type, beds, bathrooms, sleeps. One icon per meaning —
 * house, bed, bath, people — and counts only; yes-or-no items belong in amenities.
 *
 * A studio reads "Studio", never "0 bedrooms" (N-01), and every count comes from a plural resource. The tiles share
 * the width and, with large text, keep whole words and wrap to a second row.
 */
@Composable
fun SojournKeyFacts(facts: StayFacts, modifier: Modifier = Modifier) {
    ShareRow(gap = SojournSpacing.space2, modifier = modifier.fillMaxWidth()) {
        keyFacts(facts).forEach { (icon, label) -> FactTile(icon, label) }
    }
}

/** The facts as one line, for cards: "Studio · 1 bed · 1 bath · Sleeps 2". */
@Composable
internal fun factsSummary(facts: StayFacts): String =
    keyFacts(facts).joinToString(stringResource(Res.string.facts_separator)) { it.second }

@Composable
private fun keyFacts(facts: StayFacts): List<Pair<SojournIcon, String>> {
    val formatter = rememberSojournFormatter()
    val type = if (facts.isStudio) {
        stringResource(Res.string.facts_studio)
    } else {
        pluralStringResource(Res.plurals.bedrooms, facts.bedrooms, formatter.count(facts.bedrooms))
    }
    return listOf(
        SojournIcon.Home to type,
        SojournIcon.Bed to pluralStringResource(Res.plurals.beds, facts.beds, formatter.count(facts.beds)),
        SojournIcon.Bath to pluralStringResource(
            Res.plurals.bathrooms,
            facts.bathrooms,
            formatter.count(facts.bathrooms),
        ),
        SojournIcon.Guests to stringResource(Res.string.facts_sleeps, formatter.count(facts.sleeps)),
    )
}

@Composable
private fun FactTile(icon: SojournIcon, label: String) {
    val c = SojournTheme.colors
    Column(
        Modifier
            .border(1.dp, c.hairline, SojournShapes.md)
            .background(c.surface, SojournShapes.md)
            .padding(SojournSpacing.space2)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(SojournKeyFactsDefaults.LabelGap),
        horizontalAlignment = Alignment.Start,
    ) {
        SojournIcon(icon, contentDescription = null, tint = c.inkMuted, size = SojournSizes.iconSm)
        BasicText(label, style = SojournTheme.type.labelSm.copy(color = c.ink))
    }
}
