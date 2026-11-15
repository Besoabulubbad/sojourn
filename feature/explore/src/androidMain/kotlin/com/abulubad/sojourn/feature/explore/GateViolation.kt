package com.abulubad.sojourn.feature.explore

import java.util.Locale

/** Deliberate architecture violation: platform locale APIs belong to core:localization only (ADR 0009). */
internal fun deviceLanguage(): String = Locale.getDefault().language
