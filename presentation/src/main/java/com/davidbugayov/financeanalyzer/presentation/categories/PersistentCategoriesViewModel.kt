package com.davidbugayov.financeanalyzer.presentation.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.davidbugayov.financeanalyzer.data.preferences.CategoryPreferences
import com.davidbugayov.financeanalyzer.presentation.categories.model.CategoryProvider
import com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Реализация CategoriesViewModel, которая загружает и сохраняет кастомные категории
 * в CategoryPreferences, а также загружает дефолтные категории.
 */
class PersistentCategoriesViewModel(
    application: Application,
) : AndroidViewModel(application), CategoriesViewModel {
    private val categoryPreferences = CategoryPreferences.getInstance(application)

    private val _expenseCategories = MutableStateFlow<List<UiCategory>>(emptyList())
    override val expenseCategories: StateFlow<List<UiCategory>> = _expenseCategories

    private val _incomeCategories = MutableStateFlow<List<UiCategory>>(emptyList())
    override val incomeCategories: StateFlow<List<UiCategory>> = _incomeCategories

    init {
        loadCategories()
    }

    /**
     * Загружает все категории: дефолтные и кастомные
     */
    private fun loadCategories() {
        // Загружаем дефолтные категории
        val defaultExpenseCategories = CategoryProvider.getDefaultExpenseCategories(getApplication())
        val defaultIncomeCategories = CategoryProvider.getDefaultIncomeCategories(getApplication())

        // Загружаем кастомные категории из preferences
        val customExpenseCategories = categoryPreferences.loadExpenseCategories()
        val customIncomeCategories = categoryPreferences.loadIncomeCategories()

        // Конвертируем кастомные категории в UiCategory
        val customExpenseUiCategories =
            customExpenseCategories.map { customCategory ->
                UiCategory.custom(
                    name = customCategory.name,
                    isExpense = true,
                    icon = CategoryProvider.getIconByName(customCategory.iconName),
                    color =
                        customCategory.colorHex?.let { hex -> CategoryProvider.parseColorFromHex(hex) }
                            ?: CategoryProvider.generateCategoryColorFromPalette(true),
                )
            }

        val customIncomeUiCategories =
            customIncomeCategories.map { customCategory ->
                UiCategory.custom(
                    name = customCategory.name,
                    isExpense = false,
                    icon = CategoryProvider.getIconByName(customCategory.iconName),
                    color =
                        customCategory.colorHex?.let { hex -> CategoryProvider.parseColorFromHex(hex) }
                            ?: CategoryProvider.generateCategoryColorFromPalette(false),
                )
            }

        // Объединяем дефолтные и кастомные категории
        val allExpenseCategories = defaultExpenseCategories + customExpenseUiCategories
        val allIncomeCategories = defaultIncomeCategories + customIncomeUiCategories

        _expenseCategories.value = allExpenseCategories
        _incomeCategories.value = allIncomeCategories

        Timber.d(
            "Загружено ${allExpenseCategories.size} категорий расходов (${customExpenseUiCategories.size} кастомных)",
        )
        Timber.d("Загружено ${allIncomeCategories.size} категорий доходов (${customIncomeUiCategories.size} кастомных)")
    }

    override fun addCustomCategory(
        name: String,
        isExpense: Boolean,
        icon: androidx.compose.ui.graphics.vector.ImageVector?,
    ) {
        val newCategory =
            UiCategory.custom(
                name = name,
                isExpense = isExpense,
                icon = icon,
                color = CategoryProvider.generateCategoryColorFromPalette(isExpense),
            )

        // Добавляем в UI
        if (isExpense) {
            _expenseCategories.update { it + newCategory }
        } else {
            _incomeCategories.update { it + newCategory }
        }

        // Сохраняем в preferences
        val customCategoryData =
            CategoryPreferences.CustomCategoryData(
                name = name,
                iconName = icon?.let { CategoryProvider.getIconName(it) } ?: "category",
                colorHex = CategoryProvider.colorToHex(newCategory.color),
                isExpense = isExpense,
            )

        if (isExpense) {
            categoryPreferences.addExpenseCategory(customCategoryData)
        } else {
            categoryPreferences.addIncomeCategory(customCategoryData)
        }

        Timber.d("Добавлена кастомная категория: $name (${if (isExpense) "расход" else "доход"})")
    }

    override fun deleteExpenseCategory(name: String) {
        _expenseCategories.update { list -> list.filterNot { it.name == name } }
        categoryPreferences.removeExpenseCategory(name)
        Timber.d("Удалена категория расходов: $name")
    }

    override fun deleteIncomeCategory(name: String) {
        _incomeCategories.update { list -> list.filterNot { it.name == name } }
        categoryPreferences.removeIncomeCategory(name)
        Timber.d("Удалена категория доходов: $name")
    }
}
