package com.abulubad.sojourn.core.localization

import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Money
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

/**
 * The only formatter for money, dates, ranges, counts and counters (L-04, P-06, S-04, ADR 0009).
 *
 * - Western digits in both languages; thousands grouped with a comma (brand book).
 * - Money: `£1,383` in English, `1,383 £` in Arabic; minor units shown only when non-zero.
 * - Date ranges use an en dash: `12–27 Mar`, `12–27 مارس`; the month repeats only when it changes.
 * - Every value with its own direction (money, counters, "3+", references) is wrapped in a bidi isolate
 *   (U+2068 … U+2069) so it reads correctly inside Arabic sentences.
 *
 * Month and weekday names are tables, not platform calls: the output is identical on Android, iOS and the JVM,
 * which is what the screenshot matrix needs.
 */
class SojournFormatter(private val locale: AppLocale) {

    /** `£1,383` / `1,383 £`, isolated. */
    fun money(amount: Money): String = isolate(moneyPlain(amount))

    /**
     * An all-in average such as the nightly figure under a total: [total] / [units], rounded half up to whole major
     * units (`£1,383` over 15 nights → `£92`). Only for figures labelled as averages; totals use [money].
     */
    fun moneyAverage(total: Money, units: Int): String {
        require(units > 0) { "An average needs at least one unit" }
        val perUnit = total.minor.toDouble() / units
        val step = total.currency.minorUnitsPerMajor
        val rounded = kotlin.math.floor(perUnit / step + HALF).toLong() * step
        return money(Money(rounded, total.currency))
    }

    /** A guest rating on the five-point scale with one decimal: `4.8`, `5.0`. Isolated. */
    fun rating(score: Double): String {
        val tenths = kotlin.math.round(score * TENTHS).toInt()
        return isolate("${tenths / TENTHS_INT}.${tenths % TENTHS_INT}")
    }

    /** `1,383` — grouped, isolated. For counts inserted into sentences via plural resources. */
    fun count(value: Int): String = isolate(group(value.toLong()))

    /** `1 / 13` — the gallery counter, isolated so it does not reverse. */
    fun counter(index: Int, total: Int): String = isolate("${group(index.toLong())} / ${group(total.toLong())}")

    /** `3+` — "and more", isolated. */
    fun plus(value: Int): String = isolate("${group(value.toLong())}+")

    /** `-10%` — a discount, isolated. Discounts are shown with a minus sign (brand book). */
    fun discountPercent(percent: Int): String = isolate("-$percent%")

    /** A booking reference, shown as written and never mirrored. */
    fun reference(value: String): String = isolate(value)

    /** `12 Mar` or, when [withYear], `12 Mar 2026`. */
    fun date(date: LocalDate, withYear: Boolean = false): String =
        isolate(
            buildString {
                append(date.day).append(' ').append(monthShort(date.month))
                if (withYear) append(' ').append(date.year)
            },
        )

    /** `12–27 Mar`, `28 Feb – 4 Mar`, `28 Dec 2026 – 4 Jan 2027`. */
    fun dateRange(range: DateRange): String {
        val start = range.start
        val end = range.end
        val text = when {
            start.year != end.year ->
                "${start.day} ${monthShort(
                    start.month,
                )} ${start.year} $EN_DASH_SPACED${end.day} ${monthShort(end.month)} ${end.year}"
            start.month != end.month ->
                "${start.day} ${monthShort(start.month)} $EN_DASH_SPACED${end.day} ${monthShort(end.month)}"
            else -> "${start.day}$EN_DASH${end.day} ${monthShort(end.month)}"
        }
        return isolate(text)
    }

    /** `15:00` — 24-hour clock in both languages (brand book: the clock never mirrors). */
    fun time(time: LocalTime): String = isolate("${time.hour.pad()}:${time.minute.pad()}")

    /** Short month name in the current language. */
    fun monthShort(month: Month): String = when (locale) {
        AppLocale.EN_GB -> MONTHS_EN_SHORT[month.ordinal]
        AppLocale.AR -> MONTHS_AR[month.ordinal]
    }

    /** Full month name in the current language, for calendar headers. */
    fun monthLong(month: Month): String = when (locale) {
        AppLocale.EN_GB -> MONTHS_EN_LONG[month.ordinal]
        AppLocale.AR -> MONTHS_AR[month.ordinal]
    }

    /** Narrow weekday label for calendar columns; the week starts on [AppLocale.weekStart] (L-04). */
    fun weekdayNarrow(day: DayOfWeek): String = when (locale) {
        AppLocale.EN_GB -> WEEKDAYS_EN_NARROW[day.ordinal]
        AppLocale.AR -> WEEKDAYS_AR_NARROW[day.ordinal]
    }

    /** The seven days in display order, starting from the locale's first day of week. */
    fun weekDays(): List<DayOfWeek> {
        val days = DayOfWeek.entries
        val start = locale.weekStart.ordinal
        return List(days.size) { days[(start + it) % days.size] }
    }

    private fun moneyPlain(amount: Money): String {
        val sign = if (amount.isNegative) "-" else ""
        val major = group(if (amount.isNegative) -amount.major else amount.major)
        val minor = amount.minorRemainder
        val number = if (minor == 0L) major else "$major.${minor.toString().padStart(2, '0')}"
        val symbol = symbol(amount.currency)
        return when (locale) {
            AppLocale.EN_GB -> if (symbol.isSign) "$sign${symbol.text}$number" else "$sign${symbol.text} $number"
            AppLocale.AR -> "$sign$number ${symbol.text}"
        }
    }

    private fun symbol(currency: Currency): CurrencySymbol = when (currency) {
        Currency.GBP -> CurrencySymbol("£", isSign = true)
        Currency.EUR -> CurrencySymbol("€", isSign = true)
        Currency.DZD -> when (locale) {
            AppLocale.EN_GB -> CurrencySymbol("DA", isSign = false)
            AppLocale.AR -> CurrencySymbol("د.ج", isSign = false)
        }
    }

    private class CurrencySymbol(val text: String, val isSign: Boolean)

    private fun Int.pad(): String = toString().padStart(2, '0')

    companion object {
        /** First strong isolate … pop directional isolate (UAX #9). */
        const val FSI: Char = '⁨'
        const val PDI: Char = '⁩'
        private const val EN_DASH = "–"
        private const val EN_DASH_SPACED = "– "
        private const val GROUP_SIZE = 3
        private const val HALF = 0.5
        private const val TENTHS = 10.0
        private const val TENTHS_INT = 10

        /** Wraps [text] in a bidi isolate (S-04). */
        fun isolate(text: String): String = "$FSI$text$PDI"

        /** Groups thousands with a comma: `1383` → `1,383`. Negative numbers keep their sign. */
        fun group(value: Long): String {
            val digits = (if (value < 0) -value else value).toString()
            val grouped = digits.reversed().chunked(GROUP_SIZE).joinToString(",").reversed()
            return if (value < 0) "-$grouped" else grouped
        }

        private val MONTHS_EN_SHORT =
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        private val MONTHS_EN_LONG = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        )
        private val MONTHS_AR = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر",
        )
        private val WEEKDAYS_EN_NARROW = listOf("M", "T", "W", "T", "F", "S", "S")
        private val WEEKDAYS_AR_NARROW = listOf("ن", "ث", "ر", "خ", "ج", "س", "ح")
    }
}
