package com.abulubad.sojourn.core.ui

import app.cash.turbine.test
import com.abulubad.sojourn.core.common.AppError
import com.abulubad.sojourn.core.testing.runSojournTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SojournViewModelTest {

    private data class CounterState(val count: Int = 0) : UiState
    private sealed interface CounterIntent : UiIntent {
        data object Increment : CounterIntent
        data object Done : CounterIntent
    }
    private sealed interface CounterEffect : UiEffect {
        data class Finished(val total: Int) : CounterEffect
    }

    private class CounterViewModel : SojournViewModel<CounterState, CounterIntent, CounterEffect>(CounterState()) {
        override fun handle(intent: CounterIntent) = when (intent) {
            CounterIntent.Increment -> setState { copy(count = count + 1) }
            CounterIntent.Done -> sendEffect(CounterEffect.Finished(currentState.count))
        }
    }

    @Test
    fun `intents reduce state`() = runSojournTest {
        val vm = CounterViewModel()
        vm.state.test {
            assertEquals(0, awaitItem().count)
            vm.onIntent(CounterIntent.Increment)
            assertEquals(1, awaitItem().count)
        }
    }

    @Test
    fun `effects are delivered once`() = runSojournTest {
        val vm = CounterViewModel()
        vm.onIntent(CounterIntent.Increment)
        vm.onIntent(CounterIntent.Done)
        vm.effects.test {
            assertEquals(CounterEffect.Finished(1), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `offline errors keep cached data and other errors do not`() {
        val offline = ScreenState.fromError(AppError.Offline, cached = listOf(1))
        assertEquals(ScreenState.Offline(listOf(1)), offline)
        assertEquals(listOf(1), offline.dataOrNull)
        val error = ScreenState.fromError<List<Int>>(AppError.Remote(500))
        assertEquals(ScreenState.Error(AppError.Remote(500)), error)
        assertNull(error.dataOrNull)
        assertEquals(ScreenState.Empty, ScreenState.fromData(emptyList<Int>()) { it.isEmpty() })
    }
}
