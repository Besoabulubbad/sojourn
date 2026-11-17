package com.abulubad.sojourn.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.abulubad.sojourn.core.localization.resources.Res
import com.abulubad.sojourn.core.localization.resources.area_alfama
import com.abulubad.sojourn.core.localization.resources.area_bab_el_oued
import com.abulubad.sojourn.core.localization.resources.area_bastille
import com.abulubad.sojourn.core.localization.resources.area_belem
import com.abulubad.sojourn.core.localization.resources.area_camden
import com.abulubad.sojourn.core.localization.resources.area_canal_saint_martin
import com.abulubad.sojourn.core.localization.resources.area_chiado
import com.abulubad.sojourn.core.localization.resources.area_constantine_centre
import com.abulubad.sojourn.core.localization.resources.area_el_biar
import com.abulubad.sojourn.core.localization.resources.area_el_kantara
import com.abulubad.sojourn.core.localization.resources.area_front_de_mer
import com.abulubad.sojourn.core.localization.resources.area_graca
import com.abulubad.sojourn.core.localization.resources.area_hydra
import com.abulubad.sojourn.core.localization.resources.area_kings_cross
import com.abulubad.sojourn.core.localization.resources.area_le_marais
import com.abulubad.sojourn.core.localization.resources.area_les_falaises
import com.abulubad.sojourn.core.localization.resources.area_montmartre
import com.abulubad.sojourn.core.localization.resources.area_notting_hill
import com.abulubad.sojourn.core.localization.resources.area_paddington
import com.abulubad.sojourn.core.localization.resources.area_principe_real
import com.abulubad.sojourn.core.localization.resources.area_saint_germain
import com.abulubad.sojourn.core.localization.resources.area_shoreditch
import com.abulubad.sojourn.core.localization.resources.area_sidi_el_houari
import com.abulubad.sojourn.core.localization.resources.area_sidi_mabrouk
import com.abulubad.sojourn.core.localization.resources.area_sidi_yahia
import com.abulubad.sojourn.core.localization.resources.area_south_bank
import com.abulubad.sojourn.core.localization.resources.city_algiers
import com.abulubad.sojourn.core.localization.resources.city_constantine
import com.abulubad.sojourn.core.localization.resources.city_lisbon
import com.abulubad.sojourn.core.localization.resources.city_london
import com.abulubad.sojourn.core.localization.resources.city_oran
import com.abulubad.sojourn.core.localization.resources.city_paris
import com.abulubad.sojourn.core.localization.resources.list_pair
import com.abulubad.sojourn.core.localization.resources.list_series
import com.abulubad.sojourn.core.model.CityId
import org.jetbrains.compose.resources.StringResource
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

/**
 * An area's name from its catalogue key ("area_paddington" → "Paddington" / "بادينغتون"), or null when the key has no
 * translation yet; callers then show the city alone rather than a raw key.
 */
@Composable
fun localizedAreaName(key: String): String? = areaNames[key]?.let { stringResource(it) }

private val areaNames: Map<String, StringResource> = mapOf(
    "area_paddington" to Res.string.area_paddington,
    "area_shoreditch" to Res.string.area_shoreditch,
    "area_le_marais" to Res.string.area_le_marais,
    "area_hydra" to Res.string.area_hydra,
    "area_notting_hill" to Res.string.area_notting_hill,
    "area_camden" to Res.string.area_camden,
    "area_south_bank" to Res.string.area_south_bank,
    "area_kings_cross" to Res.string.area_kings_cross,
    "area_montmartre" to Res.string.area_montmartre,
    "area_saint_germain" to Res.string.area_saint_germain,
    "area_canal_saint_martin" to Res.string.area_canal_saint_martin,
    "area_bastille" to Res.string.area_bastille,
    "area_alfama" to Res.string.area_alfama,
    "area_chiado" to Res.string.area_chiado,
    "area_principe_real" to Res.string.area_principe_real,
    "area_belem" to Res.string.area_belem,
    "area_graca" to Res.string.area_graca,
    "area_el_biar" to Res.string.area_el_biar,
    "area_bab_el_oued" to Res.string.area_bab_el_oued,
    "area_sidi_yahia" to Res.string.area_sidi_yahia,
    "area_sidi_mabrouk" to Res.string.area_sidi_mabrouk,
    "area_constantine_centre" to Res.string.area_constantine_centre,
    "area_el_kantara" to Res.string.area_el_kantara,
    "area_front_de_mer" to Res.string.area_front_de_mer,
    "area_sidi_el_houari" to Res.string.area_sidi_el_houari,
    "area_les_falaises" to Res.string.area_les_falaises,
)
