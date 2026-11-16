package com.abulubad.sojourn.core.localization

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList

/**
 * Asks Android to set the per-app language (API 33+) so keyboards, system dialogs and the share sheet follow the
 * in-app choice. Below API 33 the in-app override alone applies; the app itself always reads [AppLocale].
 */
class AndroidLocaleSync(private val context: Context) : PlatformLocaleSync {
    override fun apply(locale: AppLocale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val manager = context.getSystemService(LocaleManager::class.java)
            manager?.applicationLocales = LocaleList.forLanguageTags(locale.tag)
        }
    }
}
