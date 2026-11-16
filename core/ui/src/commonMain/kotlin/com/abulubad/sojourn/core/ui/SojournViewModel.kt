package com.abulubad.sojourn.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Marker for a screen's immutable state (`XxxUiState`). */
interface UiState

/** Marker for what the user did (`XxxIntent`). */
interface UiIntent

/** Marker for one-off outputs: navigation, snackbars, haptics (`XxxEffect`). Never state. */
interface UiEffect

/**
 * The MVI base every screen ViewModel extends (ADR 0006). One [state] stream, one [onIntent] entry point,
 * one-off [effects] delivered exactly once.
 *
 * ```
 * class ResultsViewModel(searchStays: SearchStays) :
 *     SojournViewModel<ResultsUiState, ResultsIntent, ResultsEffect>(ResultsUiState()) {
 *     override fun handle(intent: ResultsIntent) = when (intent) { … }
 * }
 * ```
 */
abstract class SojournViewModel<S : UiState, I : UiIntent, E : UiEffect>(initial: S) : ViewModel() {

    private val mutableState = MutableStateFlow(initial)
    private val effectChannel = Channel<E>(Channel.BUFFERED)

    /** The screen's state. Collected by `XxxRoute`, passed to the stateless `XxxScreen`. */
    val state: StateFlow<S> = mutableState.asStateFlow()

    /** One-off effects; each is received by one collector once. */
    val effects: Flow<E> = effectChannel.receiveAsFlow()

    /** The only way the UI talks to the ViewModel. */
    fun onIntent(intent: I) = handle(intent)

    /** Reduces an intent to state changes and effects. */
    protected abstract fun handle(intent: I)

    /** Atomically replaces the state with [reduce] applied to the current one. */
    protected fun setState(reduce: S.() -> S) = mutableState.update(reduce)

    /** The current state, for reducers that branch on it. */
    protected val currentState: S get() = mutableState.value

    /** Queues a one-off effect. */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}

/**
 * Collects a ViewModel's [effects] for as long as the caller is composed. Used by every `XxxRoute`:
 * `CollectEffects(vm.effects) { effect -> when (effect) { … } }`.
 */
@Composable
fun <E : UiEffect> CollectEffects(effects: Flow<E>, onEffect: suspend (E) -> Unit) {
    val handler = rememberUpdatedState(onEffect)
    LaunchedEffect(effects) {
        effects.collect { handler.value(it) }
    }
}
