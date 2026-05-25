package com.example.autoai.base

import androidx.lifecycle.ViewModel
import com.example.autoai.navigation.IAppNavigator
import com.example.autoai.navigation.Route
import com.example.domain.model.app.DataError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<S : Any, E : Any, SE : Any>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _sideEffects = Channel<SE>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    // Per-action debounce timestamps so two debounced actions on the same screen don't
    // share a gate. The default key keeps the old single-gate behavior for any caller
    // that doesn't supply one (legacy behaviour for one-debounced-action screens).
    private val lastEventTimes = mutableMapOf<String, Long>()

    abstract fun onEvent(event: E)

    protected fun setState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun emitSideEffect(sideEffect: SE) {
        _sideEffects.trySend(sideEffect)
    }

    /**
     * @param key identifies which logical action is being debounced. Supply distinct
     * keys for distinct actions on the same screen (e.g. "send" vs "save") so they
     * don't share a 1-second window. Defaults to a single shared key for backward
     * compatibility with existing callers.
     */
    protected fun withDebounce(
        threshold: Long = 1000L,
        key: String = DEFAULT_DEBOUNCE_KEY,
        action: () -> Unit,
    ) {
        val currentTime = System.currentTimeMillis()
        val last = lastEventTimes[key] ?: 0L
        if (currentTime - last >= threshold) {
            lastEventTimes[key] = currentTime
            action()
        }
    }

    private companion object {
        const val DEFAULT_DEBOUNCE_KEY = "default"
    }
}