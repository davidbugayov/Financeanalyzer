package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.util.StringProvider

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
            CategoryMeta(0, "ShoppingCart", "food", true),
            CategoryMeta(0, "DirectionsCar", "transport", true),
            CategoryMeta(0, "Movie", "entertainment", true),
            CategoryMeta(0, "Restaurant", "restaurant", true),
            CategoryMeta(0, "LocalHospital", "health", true),
            CategoryMeta(0, "Checkroom", "clothing", true),
            CategoryMeta(0, "Home", "housing", true),
            CategoryMeta(0, "Phone", "communication", true),
            CategoryMeta(0, "Pets", "pet", true),
            CategoryMeta(0, "Work", "services", true),
            CategoryMeta(0, "Payments", "charity", true),
            CategoryMeta(0, "CreditCard", "credit", true),
            CategoryMeta(0, "SwapHoriz", "transfer", true),
            CategoryMeta(0, "MoreHoriz", "other_expense", true),
            CategoryMeta(0, "Payments", "salary", false),
            CategoryMeta(0, "Work", "freelance", false),
            CategoryMeta(0, "Payments", "gifts", false),
            CategoryMeta(0, "TrendingUp", "interest", false),
            CategoryMeta(0, "Work", "rental", false),
            CategoryMeta(0, "MoreHoriz", "other_income", false),
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
            val name = getCategoryNameByKey(meta.categoryKey)
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
            val name = getCategoryNameByKey(meta.categoryKey)
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

    private fun getCategoryNameByKey(key: String): String {
        return when (key) {
            "food" -> StringProvider.categoryFood
            "transport" -> StringProvider.categoryTransport
            "entertainment" -> StringProvider.categoryEntertainment
            "restaurant" -> StringProvider.categoryRestaurant
            "health" -> StringProvider.categoryHealth
            "clothing" -> StringProvider.categoryClothing
            "housing" -> StringProvider.categoryHousing
            "communication" -> StringProvider.categoryCommunication
            "pet" -> StringProvider.categoryPet
            "services" -> StringProvider.categoryServices
            "charity" -> StringProvider.categoryCharity
            "credit" -> StringProvider.categoryCredit
            "transfer" -> StringProvider.categoryTransfer
            "other_expense" -> StringProvider.categoryOtherExpense
            "salary" -> StringProvider.categorySalary
            "freelance" -> StringProvider.categoryFreelance
            "gifts" -> StringProvider.categoryGifts
            "interest" -> StringProvider.categoryInterest
            "rental" -> StringProvider.categoryRental
            "other_income" -> StringProvider.categoryOtherIncome
            else -> StringProvider.categoryOther
        }
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
