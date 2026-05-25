package com.example.domain.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Production-side implementation of [DispatcherProvider]. Lives next to the interface in
 * `:domain` so the impl can be swapped via the same module boundary and so non-`:app`
 * test harnesses don't have to pull in `:app` just to get a real dispatcher.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
