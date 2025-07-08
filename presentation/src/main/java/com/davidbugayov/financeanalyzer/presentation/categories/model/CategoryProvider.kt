package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.presentation.R
import com.davidbugayov.financeanalyzer.ui.theme.DefaultCategoryColor
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseChartPalette
import com.davidbugayov.financeanalyzer.ui.theme.IncomeChartPalette
import com.davidbugayov.financeanalyzer.ui.theme.expenseCategoryColorsMap
import com.davidbugayov.financeanalyzer.ui.theme.incomeCategoryColorsMap

object CategoryProvider {
    data class CategoryMeta(
        val nameRes: Int,
        val iconName: String,
        val categoryKey: String,
        val isExpense: Boolean,
    )

    val defaultCategories =
        listOf(
            CategoryMeta(R.string.category_food, "ShoppingCart", "food", true),
            CategoryMeta(R.string.category_transport, "DirectionsCar", "transport", true),
            CategoryMeta(R.string.category_entertainment, "Movie", "entertainment", true),
            CategoryMeta(R.string.category_restaurant, "Restaurant", "restaurant", true),
            CategoryMeta(R.string.category_health, "LocalHospital", "health", true),
            CategoryMeta(R.string.category_clothing, "Checkroom", "clothing", true),
            CategoryMeta(R.string.category_housing, "Home", "housing", true),
            CategoryMeta(R.string.category_communication, "Phone", "communication", true),
            CategoryMeta(R.string.category_pet, "Pets", "pet", true),
            CategoryMeta(R.string.category_services, "Work", "services", true),
            CategoryMeta(R.string.category_charity, "Payments", "charity", true),
            CategoryMeta(R.string.category_credit, "CreditCard", "credit", true),
            CategoryMeta(R.string.category_transfer, "SwapHoriz", "transfer", true),
            CategoryMeta(R.string.category_other, "MoreHoriz", "other_expense", true),
            CategoryMeta(R.string.category_salary, "Payments", "salary", false),
            CategoryMeta(R.string.category_freelance, "Work", "freelance", false),
            CategoryMeta(R.string.category_gift, "Payments", "gifts", false),
            CategoryMeta(R.string.category_interest, "TrendingUp", "interest", false),
            CategoryMeta(R.string.category_rent, "Work", "rental", false),
            CategoryMeta(R.string.category_other, "MoreHoriz", "other_income", false),
        )

    private fun getCategoryColorByKey(
        key: String,
        isExpense: Boolean,
    ): Color {
        return if (isExpense) {
            expenseCategoryColorsMap.getValue(key)
        } else {
            incomeCategoryColorsMap.getValue(key)
        }
    }

    fun getDefaultExpenseCategories(context: Context): List<UiCategory> =
        defaultCategories.filter { it.isExpense }.mapIndexed { idx, meta ->
            val name = context.getString(meta.nameRes)
            val cat = Category.expense(name)
            UiCategory(
                id = idx + 1L,
                name = name,
                isExpense = true,
                isCustom = false,
                count = 0,
                original = cat,
                color = getCategoryColorByKey(meta.categoryKey, true),
                icon = CategoryIconProvider.getIconByName(meta.iconName),
            )
        }

    fun getDefaultIncomeCategories(context: Context): List<UiCategory> =
        defaultCategories.filter { !it.isExpense }.mapIndexed { idx, meta ->
            val name = context.getString(meta.nameRes)
            val cat = Category.income(name)
            UiCategory(
                id = idx + 1L,
                name = name,
                isExpense = false,
                isCustom = false,
                count = 0,
                original = cat,
                color = getCategoryColorByKey(meta.categoryKey, false),
                icon = CategoryIconProvider.getIconByName(meta.iconName),
            )
        }

    /**
     * Генерирует цвет для новой категории, выбирая его из соответствующей палитры.
     * Если палитра пуста, возвращает цвет по умолчанию для категории "Прочее".
     */
    fun generateCategoryColorFromPalette(isExpense: Boolean): Color {
        val palette = if (isExpense) ExpenseChartPalette else IncomeChartPalette
        return palette.ifEmpty { listOf(DefaultCategoryColor) }.random()
    }
}
