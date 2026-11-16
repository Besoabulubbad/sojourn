package com.abulubad.sojourn.core.designsystem.preview

import androidx.compose.runtime.Immutable
import com.abulubad.sojourn.core.localization.AppLocale

/**
 * One cell of the screenshot and preview matrix (ADR 0010, engineering standards "Test matrix"):
 * language × theme × font scale × width.
 */
@Immutable
data class SojournVariant(
    val locale: AppLocale,
    val darkTheme: Boolean,
    val fontScale: Float = 1f,
    val widthDp: Int = WIDTH_DEFAULT,
) {
    /** Stable file-name fragment: `ar_dark_f200_w320`. */
    val id: String
        get() = buildString {
            append(if (locale == AppLocale.AR) "ar" else "en")
            append(if (darkTheme) "_dark" else "_light")
            append("_f").append((fontScale * PERCENT).toInt())
            append("_w").append(widthDp)
        }

    /** The [id], so parameterized test names read `colors[en_light_f100_w412]`. */
    override fun toString(): String = id

    companion object {
        const val WIDTH_DEFAULT = 412
        const val WIDTH_NARROW = 320
        const val FONT_SCALE_LARGE = 2f
        private const val PERCENT = 100
    }
}

/** The named sets of variants tests and previews iterate. */
object SojournVariants {

    val EnLight = SojournVariant(AppLocale.EN_GB, darkTheme = false)
    val EnDark = SojournVariant(AppLocale.EN_GB, darkTheme = true)
    val ArLight = SojournVariant(AppLocale.AR, darkTheme = false)
    val ArDark = SojournVariant(AppLocale.AR, darkTheme = true)

    /** The four variants every screen ships in (brand book): EN/AR × light/dark at 100 %, 412 dp. */
    val Four: List<SojournVariant> = listOf(EnLight, EnDark, ArLight, ArDark)

    /** The full matrix: 2 languages × 2 themes × font 100 %/200 % × 320/412 dp = 16 cells. */
    val Matrix: List<SojournVariant> = Four.flatMap { base ->
        listOf(1f, SojournVariant.FONT_SCALE_LARGE).flatMap { scale ->
            listOf(SojournVariant.WIDTH_DEFAULT, SojournVariant.WIDTH_NARROW).map { width ->
                base.copy(fontScale = scale, widthDp = width)
            }
        }
    }
}
