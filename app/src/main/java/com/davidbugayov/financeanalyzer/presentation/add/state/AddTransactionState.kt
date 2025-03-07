package com.davidbugayov.financeanalyzer.presentation.add.state

import java.util.Date

/**
 * Состояние экрана добавления транзакции.
 * Содержит все поля формы и состояния UI компонентов.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @property title Название транзакции
 * @property amount Сумма транзакции в виде строки
 * @property category Выбранная категория
 * @property note Дополнительное примечание к транзакции
 * @property isExpense Тип транзакции (true - расход, false - доход)
 * @property selectedDate Выбранная дата транзакции
 * @property isLoading Флаг загрузки при сохранении
 * @property error Текст ошибки (null если ошибок нет)
 * @property isSuccess Флаг успешного сохранения
 * @property showDatePicker Флаг отображения диалога выбора даты
 * @property showCategoryPicker Флаг отображения диалога выбора категории
 * @property showCustomCategoryDialog Флаг отображения диалога добавления категории
 * @property customCategory Текст новой пользовательской категории
 */
data class AddTransactionState(
    val title: String = "",
    val amount: String = "",
    val category: String = "",
    val note: String = "",
    val isExpense: Boolean = true,
    val selectedDate: Date = Date(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val showDatePicker: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showCustomCategoryDialog: Boolean = false,
    val customCategory: String = ""
) 