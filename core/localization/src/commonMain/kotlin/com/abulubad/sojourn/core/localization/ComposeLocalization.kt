package com.abulubad.sojourn.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.city_algiers
import com.abulubad.sojourn.core.localization.resources.city_constantine
import com.abulubad.sojourn.core.localization.resources.city_lisbon
import com.abulubad.sojourn.core.localization.resources.city_london
import com.abulubad.sojourn.core.localization.resources.city_oran
import com.abulubad.sojourn.core.localization.resources.city_paris
import com.abulubad.sojourn.core.localization.resources.list_pair
import com.abulubad.sojourn.core.localization.resources.list_series
import com.abulubad.sojourn.core.model.CityId
import org.jetbrains.compose.resources.stringResource

/** The [SojournFormatter] for the current [LocalAppLocale]; composables format money, dates and counts through it. */
@Composable
fun rememberSojournFormatter(): SojournFormatter {
    val locale = LocalAppLocale.current
    return remember(locale) { SojournFormatter(locale) }
}

/**
 * Joins already-localized names into one phrase from resources (`utilities`, `cleaning fee and utilities`,
 * `city tax, cleaning fee and utilities`), so lists are never built by concatenation in code (S-02).
 */
@Composable
fun localizedList(items: List<String>): String = when (items.size) {
    0 -> ""
    1 -> items.single()
    else -> {
        val head = items.dropLast(1).reduce { acc, item -> stringResource(Res.string.list_series, acc, item) }
        stringResource(Res.string.list_pair, head, items.last())
    }
}

/**
 * A city's name in the current language, localized by id (S-05): the catalogue sends `london`, the app owns
 * "London" / "لندن". An id without a translation shows as-is, which `verifyPlurals`-style review should catch.
 */
@Composable
fun localizedCityName(id: CityId): String = when (id.value) {
    "london" -> stringResource(Res.string.city_london)
    "paris" -> stringResource(Res.string.city_paris)
    "lisbon" -> stringResource(Res.string.city_lisbon)
    "algiers" -> stringResource(Res.string.city_algiers)
    "constantine" -> stringResource(Res.string.city_constantine)
    "oran" -> stringResource(Res.string.city_oran)
    else -> id.value
}
