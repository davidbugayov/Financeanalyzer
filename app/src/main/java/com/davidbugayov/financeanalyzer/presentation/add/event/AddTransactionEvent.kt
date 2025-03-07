package com.davidbugayov.financeanalyzer.presentation.add.event

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import java.util.Date

/**
 * События для экрана добавления транзакции.
 * Определяет все возможные действия пользователя и изменения состояния.
 * Следует принципу открытости/закрытости (OCP) из SOLID.
 */
sealed class AddTransactionEvent {

    /** Установка названия транзакции */
    data class SetTitle(val title: String) : AddTransactionEvent()

    /** Установка суммы транзакции */
    data class SetAmount(val amount: String) : AddTransactionEvent()

    /** Выбор категории транзакции */
    data class SetCategory(val category: String) : AddTransactionEvent()

    /** Добавление примечания к транзакции */
    data class SetNote(val note: String) : AddTransactionEvent()

    /** Изменение типа транзакции (расход/доход) */
    data class SetExpenseType(val isExpense: Boolean) : AddTransactionEvent()

    /** Установка даты транзакции */
    data class SetDate(val date: Date) : AddTransactionEvent()

    /** Сохранение новой транзакции */
    data class AddTransaction(val transaction: Transaction) : AddTransactionEvent()

    /** Сброс состояния успешного сохранения */
    data object ResetSuccess : AddTransactionEvent()

    /** Сброс состояния ошибки */
    data object ResetError : AddTransactionEvent()

    /** Показать диалог выбора даты */
    data object ShowDatePicker : AddTransactionEvent()

    /** Скрыть диалог выбора даты */
    data object HideDatePicker : AddTransactionEvent()

    /** Показать диалог выбора категории */
    data object ShowCategoryPicker : AddTransactionEvent()

    /** Скрыть диалог выбора категории */
    data object HideCategoryPicker : AddTransactionEvent()

    /** Показать диалог добавления новой категории */
    data object ShowCustomCategoryDialog : AddTransactionEvent()

    /** Скрыть диалог добавления новой категории */
    data object HideCustomCategoryDialog : AddTransactionEvent()

    /** Установка текста новой категории в диалоге */
    data class SetCustomCategory(val category: String) : AddTransactionEvent()

    /** Добавление новой пользовательской категории */
    data class AddCustomCategory(val category: String) : AddTransactionEvent()
} 