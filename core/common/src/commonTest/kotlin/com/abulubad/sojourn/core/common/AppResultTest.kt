package com.abulubad.sojourn.core.common

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class AppResultTest {

    @Test
    fun `map transforms only success`() {
        assertEquals(AppResult.Success(4), AppResult.Success(2).map { it * 2 })
        val failure: AppResult<Int> = AppResult.Failure(AppError.Offline)
        assertEquals(failure, failure.map { it * 2 })
    }

    @Test
    fun `fold picks the matching branch`() {
        assertEquals("ok 1", AppResult.Success(1).fold({ "ok $it" }, { "err" }))
        assertEquals("err", AppResult.Failure(AppError.NotFound("x")).fold({ "ok" }, { "err" }))
        assertNull(AppResult.Failure(AppError.Offline).getOrNull())
        assertEquals(AppError.Offline, AppResult.Failure(AppError.Offline).errorOrNull())
    }

    @Test
    fun `runCatchingApp wraps exceptions but not cancellation`() {
        val wrapped = runCatchingApp<Int> { error("boom") }
        val error = assertIs<AppError.Unexpected>(wrapped.errorOrNull())
        assertEquals("boom", error.cause?.message)
        assertFailsWith<CancellationException> { runCatchingApp { throw CancellationException("stop") } }
    }
}
