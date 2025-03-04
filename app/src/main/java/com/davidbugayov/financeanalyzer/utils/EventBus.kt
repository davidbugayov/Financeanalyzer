package com.davidbugayov.financeanalyzer.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: Event) {
        _events.emit(event)
    }
}

sealed class Event {
    object TransactionAdded : Event()
    object TransactionDeleted : Event()
    object TransactionUpdated : Event()
} 