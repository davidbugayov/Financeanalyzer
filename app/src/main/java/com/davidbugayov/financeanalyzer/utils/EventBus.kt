package com.davidbugayov.financeanalyzer.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Шина событий для передачи уведомлений между компонентами приложения.
 * Использует Kotlin Flow для асинхронной передачи событий.
 */
object EventBus {
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    /**
     * Отправляет событие всем подписчикам
     * @param event Событие для отправки
     */
    suspend fun emit(event: Event) {
        _events.emit(event)
    }
}

/**
 * Sealed класс для представления различных событий в приложении
 */
sealed class Event {

    /** Событие добавления новой транзакции */
    object TransactionAdded : Event()

    /** Событие удаления транзакции */
    object TransactionDeleted : Event()

    /** Событие обновления существующей транзакции */
    object TransactionUpdated : Event()
} 