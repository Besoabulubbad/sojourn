package com.abulubad.sojourn.core.localization

import com.abulubad.sojourn.core.localization.SojournFormatter.Companion.FSI
import com.abulubad.sojourn.core.localization.SojournFormatter.Companion.PDI
import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Money
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class SojournFormatterTest {

    private val en = SojournFormatter(AppLocale.EN_GB)
    private val ar = SojournFormatter(AppLocale.AR)

    @Test
    fun `money is grouped and placed per language and isolated`() {
        val amount = Money.of(1383, Currency.GBP)
        assertEquals("$FSI£1,383$PDI", en.money(amount))
        assertEquals("${FSI}1,383 £$PDI", ar.money(amount))
    }

    @Test
    fun `ratings show one decimal in western digits`() {
        assertEquals("${FSI}4.8$PDI", en.rating(4.8))
        assertEquals("${FSI}5.0$PDI", ar.rating(5.0))
        assertEquals("${FSI}4.7$PDI", en.rating(4.66))
    }

    @Test
    fun `calendar headers and currency placement follow the language`() {
        assertEquals("March 2027", en.monthYear(YearMonth(2027, 3)))
        assertEquals("مارس 2027", ar.monthYear(YearMonth(2027, 3)))
        assertEquals("£", en.currencySymbol(Currency.GBP))
        assertEquals("د.ج", ar.currencySymbol(Currency.DZD))
        assertEquals(true, en.currencyLeads)
        assertEquals(false, ar.currencyLeads)
    }

    @Test
    fun `averages round half up to whole units`() {
        assertEquals("$FSI£92$PDI", en.moneyAverage(Money.of(1383, Currency.GBP), 15))
        assertEquals("${FSI}93 £$PDI", ar.moneyAverage(Money.of(1395, Currency.GBP), 15))
        assertEquals("$FSI£93$PDI", en.moneyAverage(Money.of(185, Currency.GBP), 2))
    }

    @Test
    fun `money shows minor units only when present`() {
        assertEquals("$FSI£92.50$PDI", en.money(Money.of(92, Currency.GBP, minorRemainder = 50)))
        assertEquals("$FSI€2,100$PDI", en.money(Money.of(2100, Currency.EUR)))
    }

    @Test
    fun `currencies without a sign take a spaced code`() {
        val dinars = Money.of(210000, Currency.DZD)
        assertEquals("${FSI}DA 210,000$PDI", en.money(dinars))
        assertEquals("${FSI}210,000 د.ج$PDI", ar.money(dinars))
    }

    @Test
    fun `negative money leads with a true minus sign rather than a hyphen`() {
        assertEquals("$FSI\u2212£276$PDI", en.money(-Money.of(276, Currency.GBP)))
        assertEquals("$FSI\u2212276 £$PDI", ar.money(-Money.of(276, Currency.GBP)))
    }

    @Test
    fun `date ranges use an en dash and repeat the month only when it changes`() {
        assertEquals("${FSI}12–27 Mar$PDI", en.dateRange(DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))))
        assertEquals("${FSI}12–27 مارس$PDI", ar.dateRange(DateRange(LocalDate(2026, 3, 12), LocalDate(2026, 3, 27))))
        assertEquals("${FSI}28 Feb – 4 Mar$PDI", en.dateRange(DateRange(LocalDate(2026, 2, 28), LocalDate(2026, 3, 4))))
        assertEquals(
            "${FSI}28 Dec 2026 – 4 Jan 2027$PDI",
            en.dateRange(DateRange(LocalDate(2026, 12, 28), LocalDate(2027, 1, 4))),
        )
    }

    @Test
    fun `counters and counts are isolated and use western digits in arabic`() {
        assertEquals("${FSI}1 / 13$PDI", ar.counter(1, 13))
        assertEquals("${FSI}3+$PDI", ar.plus(3))
        assertEquals("${FSI}1,383$PDI", ar.count(1383))
        assertEquals("$FSI-10%$PDI", ar.discountPercent(10))
    }

    @Test
    fun `dates and times`() {
        assertEquals("${FSI}26 Feb$PDI", en.date(LocalDate(2026, 2, 26)))
        assertEquals("${FSI}26 فبراير 2026$PDI", ar.date(LocalDate(2026, 2, 26), withYear = true))
        assertEquals("${FSI}15:00$PDI", en.time(LocalTime(15, 0)))
        assertEquals("${FSI}09:05$PDI", ar.time(LocalTime(9, 5)))
    }

    @Test
    fun `the week starts on monday in english and saturday in arabic`() {
        assertEquals(DayOfWeek.MONDAY, en.weekDays().first())
        assertEquals(DayOfWeek.SATURDAY, ar.weekDays().first())
        assertEquals(7, ar.weekDays().toSet().size)
        assertEquals("S", en.weekdayNarrow(DayOfWeek.SATURDAY))
        assertEquals("س", ar.weekdayNarrow(DayOfWeek.SATURDAY))
    }

    @Test
    fun `grouping handles small and large and negative numbers`() {
        assertEquals("0", SojournFormatter.group(0))
        assertEquals("999", SojournFormatter.group(999))
        assertEquals("1,000", SojournFormatter.group(1000))
        assertEquals("1,234,567", SojournFormatter.group(1_234_567))
        assertEquals("-12,345", SojournFormatter.group(-12_345))
    }
}
