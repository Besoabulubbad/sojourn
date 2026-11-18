package com.abulubad.sojourn.core.testing

import com.abulubad.sojourn.core.common.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/** Every dispatcher is the test dispatcher, so `advanceUntilIdle()` drives all work. */
class TestDispatcherProvider(val dispatcher: TestDispatcher = StandardTestDispatcher()) : DispatcherProvider {
    override val main: CoroutineDispatcher get() = dispatcher
    override val io: CoroutineDispatcher get() = dispatcher
    override val default: CoroutineDispatcher get() = dispatcher
}

/**
 * `runTest` with `Dispatchers.Main` set to the test dispatcher for the duration, which `viewModelScope` needs on
 * every platform. The multiplatform stand-in for a JUnit `MainDispatcherRule`.
 *
 * Main is reset only after `runTest` has returned, which on Android and iOS (the targets; `runTest` blocks there)
 * is after its own clean-up: work a ViewModel queued after the body's last line still finds Main while it is
 * cancelled.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runSojournTest(
    dispatchers: TestDispatcherProvider = TestDispatcherProvider(),
    body: suspend TestScope.(TestDispatcherProvider) -> Unit,
): TestResult {
    Dispatchers.setMain(dispatchers.dispatcher)
    try {
        return runTest(dispatchers.dispatcher) { body(dispatchers) }
    } finally {
        Dispatchers.resetMain()
    }
}
