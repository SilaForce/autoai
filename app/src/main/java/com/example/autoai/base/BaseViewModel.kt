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

    private var lastEventTime = 0L

    abstract fun onEvent(event: E)

    protected fun setState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun emitSideEffect(sideEffect: SE) {
        _sideEffects.trySend(sideEffect)
    }

    protected fun withDebounce(threshold: Long = 1000L, action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEventTime >= threshold) {
            lastEventTime = currentTime
            action()
        }
    }
}