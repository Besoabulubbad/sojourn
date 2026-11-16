package com.abulubad.sojourn.core.platform

import com.abulubad.sojourn.core.model.Currency
import com.abulubad.sojourn.core.model.Money
import com.abulubad.sojourn.core.model.QuoteId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StubPaymentSheetTest {
    @Test
    fun `the stub authorizes the quote it was given`() = runTest {
        val result = StubPaymentSheet().pay(PaymentRequest(QuoteId("q1"), Money.of(1383, Currency.GBP)))
        assertEquals(PaymentResult.Authorized("stub-q1"), result)
    }
}
