package com.abulubad.sojourn.core.designsystem.components.citycard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.abulubad.sojourn.core.designsystem.components.internal.PhotoSlot
import com.abulubad.sojourn.core.designsystem.theme.SojournTheme
import com.abulubad.sojourn.core.designsystem.tokens.SojournShapes
import com.abulubad.sojourn.core.designsystem.tokens.SojournSpacing
import com.abulubad.sojourn.core.localization.localizedCityName
import com.abulubad.sojourn.core.localization.rememberSojournFormatter
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.city_card_summary
import com.abulubad.sojourn.core.localization.resources.price_from_month
import com.abulubad.sojourn.core.localization.resources.price_from_night
import com.abulubad.sojourn.core.localization.resources.stays
import com.abulubad.sojourn.core.model.City
import com.abulubad.sojourn.core.model.PriceBasis
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A city on Explore and in the search's Where step (CityCard card): its photo, its name from the localized
 * catalogue (S-05), and "214 stays · from £1,950 a month". The caller sets the width (148 dp in Explore's row).
 */
@Composable
fun SojournCityCard(city: City, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = SojournTheme.colors
    val formatter = rememberSojournFormatter()
    val stays = pluralStringResource(Res.plurals.stays, city.stayCount, formatter.count(city.stayCount))
    val from = stringResource(
        if (city.fromPrice.per == PriceBasis.Month) Res.string.price_from_month else Res.string.price_from_night,
        formatter.money(city.fromPrice.amount),
    )
    Column(
        // Only the photo is rounded: clipping the whole card would cut the text at its corners.
        modifier.clickable(role = Role.Button, indication = ripple(), interactionSource = null, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(SojournSpacing.space2),
    ) {
        PhotoSlot(
            city.photo,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(SojournCityCardDefaults.PhotoHeight).clip(SojournShapes.lg),
        )
        BasicText(localizedCityName(city.id), style = SojournTheme.type.titleSm.copy(color = c.ink))
        BasicText(
            stringResource(Res.string.city_card_summary, stays, from),
            style = SojournTheme.type.bodySm.copy(color = c.inkMuted),
        )
    }
}
