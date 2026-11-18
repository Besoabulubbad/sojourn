package com.abulubad.sojourn.core.navigation

import com.abulubad.sojourn.core.model.CityId
import com.abulubad.sojourn.core.model.DateRange
import com.abulubad.sojourn.core.model.Guests
import com.abulubad.sojourn.core.model.SearchQuery
import com.abulubad.sojourn.core.model.StayLength
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchArgsMappingTest {

    @Test
    fun `a dated and a flexible search round-trip through the arguments`() {
        val dated = SearchQuery(
            cityId = CityId("london"),
            dates = DateRange(LocalDate(2027, 3, 12), LocalDate(2027, 3, 27)),
            guests = Guests(adults = 2, children = 1),
        )
        val flexible = SearchQuery(length = StayLength.Month, startMonth = YearMonth(2027, 3))
        assertEquals(dated, dated.toArgs().toQuery())
        assertEquals(flexible, flexible.toArgs().toQuery())
        assertEquals("2027-03", flexible.toArgs().startMonth)
    }

    @Test
    fun `arguments this build cannot read are dropped - not thrown`() {
        val args = SearchArgs(
            checkIn = "2027-03-27",
            checkOut = "2027-03-12",
            adults = 0,
            children = -1,
            length = "Fortnight",
            startMonth = "March",
        )
        assertEquals(SearchQuery(), args.toQuery())
        assertEquals(SearchQuery(), SearchArgs(checkIn = "soon", checkOut = "2027-03-12").toQuery())
    }
}
