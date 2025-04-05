package com.davidbugayov.financeanalyzer.presentation.state

import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem
import java.util.Date

/**
 * Состояние экрана добавления транзакции.
 * Содержит все поля формы и состояния UI компонентов.
 * Следует принципу единственной ответственности (SRP) из SOLID.
 *
 * @property title Название транзакции
 * @property amount Сумма транзакции в виде строки
 * @property amountError Флаг ошибки в сумме транзакции
 * @property category Выбранная категория
 * @property categoryError Флаг ошибки в категории
 * @property note Дополнительное примечание к транзакции
 * @property isExpense Тип транзакции (true - расход, false - доход)
 * @property selectedDate Выбранная дата транзакции
 * @property isLoading Флаг загрузки при сохранении
 * @property error Текст ошибки (null если ошибок нет)
 * @property isSuccess Флаг успешного сохранения
 * @property successMessage Сообщение об успешном действии
 * @property showDatePicker Флаг отображения диалога выбора даты
 * @property showCategoryPicker Флаг отображения диалога выбора категории
 * @property showCustomCategoryDialog Флаг отображения диалога добавления категории
 * @property customCategory Текст новой пользовательской категории
 * @property showSourcePicker Флаг отображения диалога выбора источника
 * @property showCustomSourceDialog Флаг отображения диалога добавления источника
 * @property customSource Текст нового пользовательского источника
 * @property source Выбранный источник
 * @property sourceColor Цвет выбранного источника
 * @property showColorPicker Флаг отображения диалога выбора цвета
 * @property showCancelConfirmation Флаг отображения подтверждения отмены
 * @property expenseCategories Список категорий расходов
 * @property incomeCategories Список категорий доходов
 * @property sources Список доступных источников
 * @property categoryToDelete ID категории для удаления
 * @property sourceToDelete ID источника для удаления
 * @property showDeleteCategoryConfirmDialog Флаг отображения подтверждения удаления категории
 * @property showDeleteSourceConfirmDialog Флаг отображения подтверждения удаления источника
 * @property editMode Флаг редактирования транзакции
 * @property transactionToEdit Транзакция для редактирования
 * @property description Описание транзакции
 */
data class AddTransactionState(
    val title: String = "",
    val amount: String = "",
    val amountError: Boolean = false,
    val category: String = "",
    val categoryError: Boolean = false,
    val note: String = "",
    val isExpense: Boolean = true,
    val selectedDate: Date = Date(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String = "Операция выполнена успешно",
    val showDatePicker: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val showCustomCategoryDialog: Boolean = false,
    val customCategory: String = "",
    val showSourcePicker: Boolean = false,
    val showCustomSourceDialog: Boolean = false,
    val customSource: String = "",
    val source: String = "Сбер",
    val sourceColor: Int = 0xFF21A038.toInt(), // Цвет Сбера
    val showColorPicker: Boolean = false,
    val showCancelConfirmation: Boolean = false,
    val expenseCategories: List<CategoryItem> = emptyList(),
    val incomeCategories: List<CategoryItem> = emptyList(),
    val sources: List<Source> = emptyList(),
    val categoryToDelete: String? = null,
    val sourceToDelete: String? = null,
    val showDeleteCategoryConfirmDialog: Boolean = false,
    val showDeleteSourceConfirmDialog: Boolean = false,
    val editMode: Boolean = false,
    val transactionToEdit: Transaction? = null,
    val description: String = ""
) 