package com.abulubad.sojourn.core.localization

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.datetime.DayOfWeek

/**
 * The single source of language and direction for the whole app (L-01, ADR 0009). Strings, layout direction,
 * number, date and plural formatting all derive from the current value; nothing reads the device locale.
 *
 * Week start follows the locale's calendar convention (L-04): Monday for British English, Saturday for Arabic.
 */
enum class AppLocale(val tag: String, val layoutDirection: LayoutDirection, val weekStart: DayOfWeek) {
    EN_GB("en-GB", LayoutDirection.Ltr, DayOfWeek.MONDAY),
    AR("ar", LayoutDirection.Rtl, DayOfWeek.SATURDAY),
    ;

    val isRtl: Boolean get() = layoutDirection == LayoutDirection.Rtl

    companion object {
        val Default: AppLocale = EN_GB

        /** Resolves a stored or system tag (`ar`, `ar-DZ`, `en-US`) to a supported locale; English otherwise. */
        fun fromTag(tag: String?): AppLocale = when (tag?.substringBefore('-')?.lowercase()) {
            "ar" -> AR
            else -> Default
        }
    }
}

/** The current [AppLocale]; provided by `SojournTheme` through `ProvideAppLocale`. */
val LocalAppLocale = staticCompositionLocalOf { AppLocale.Default }
