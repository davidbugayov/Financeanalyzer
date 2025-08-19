package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.CategoryRandomPalette
import com.davidbugayov.financeanalyzer.ui.theme.DefaultCategoryColor
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseChartPalette
import com.davidbugayov.financeanalyzer.ui.theme.IncomeChartPalette
import com.davidbugayov.financeanalyzer.ui.theme.expenseCategoryColorsMap
import com.davidbugayov.financeanalyzer.ui.theme.incomeCategoryColorsMap
import com.davidbugayov.financeanalyzer.ui.theme.toHexString

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
    ): Color =
        if (isExpense) {
            expenseCategoryColorsMap.getValue(key)
        } else {
            incomeCategoryColorsMap.getValue(key)
        }

    fun getDefaultExpenseCategories(context: Context): List<UiCategory> =
        defaultCategories.filter { it.isExpense }.mapIndexed { idx, meta ->
            val name = getCategoryNameByKey(context, meta.categoryKey)
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
            val name = getCategoryNameByKey(context, meta.categoryKey)
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

    private fun getCategoryNameByKey(
        context: Context,
        key: String,
    ): String =
        when (key) {
            "food" -> context.getString(UiR.string.category_food)
            "transport" -> context.getString(UiR.string.category_transport)
            "entertainment" -> context.getString(UiR.string.category_entertainment)
            "restaurant" -> context.getString(UiR.string.category_restaurant)
            "health" -> context.getString(UiR.string.category_health)
            "clothing" -> context.getString(UiR.string.category_clothing)
            "housing" -> context.getString(UiR.string.category_housing)
            "communication" -> context.getString(UiR.string.category_communication)
            "pet" -> context.getString(UiR.string.category_pet)
            "services" -> context.getString(UiR.string.category_services)
            "charity" -> context.getString(UiR.string.category_charity)
            "credit" -> context.getString(UiR.string.category_credit)
            "transfer" -> context.getString(UiR.string.category_transfer)
            "other_expense" -> context.getString(UiR.string.category_other_expense)
            "salary" -> context.getString(UiR.string.category_salary)
            "freelance" -> context.getString(UiR.string.category_freelance)
            "gifts" -> context.getString(UiR.string.category_gifts)
            "interest" -> context.getString(UiR.string.category_interest)
            "rental" -> context.getString(UiR.string.category_rental)
            "other_income" -> context.getString(UiR.string.category_other_income)
            else -> context.getString(UiR.string.category_other)
        }

    /**
     * Генерирует цвет для новой категории, выбирая его из соответствующей палитры.
     * Если палитра пуста, возвращает цвет по умолчанию для категории "Прочее".
     */
    fun generateCategoryColorFromPalette(isExpense: Boolean): Color {
        val palette = if (isExpense) ExpenseChartPalette else IncomeChartPalette
        return palette.ifEmpty { listOf(DefaultCategoryColor) }.random()
    }

    /**
     * Генерирует случайный цвет категории из общей палитры,
     * исключая чисто белый и чисто черный.
     */
    fun generateRandomCategoryColor(): Color {
        val filtered =
            CategoryRandomPalette.filter { color ->
                val r = (color.red * 255).toInt()
                val g = (color.green * 255).toInt()
                val b = (color.blue * 255).toInt()
                !((r == 0 && g == 0 && b == 0) || (r == 255 && g == 255 && b == 255))
            }
        return filtered.ifEmpty { listOf(DefaultCategoryColor) }.random()
    }

    /**
     * Получает иконку по имени
     */
    fun getIconByName(name: String): androidx.compose.ui.graphics.vector.ImageVector =
        CategoryIconProvider.getIconByName(name)

    /**
     * Получает имя иконки из ImageVector
     */
    fun getIconName(icon: androidx.compose.ui.graphics.vector.ImageVector): String {
        // Простая реализация - возвращаем дефолтное имя
        // В реальном приложении здесь должна быть более сложная логика
        return "category"
    }

    /**
     * Парсит цвет из hex строки
     */
    fun parseColorFromHex(hex: String): Color =
        try {
            Color(hex.toColorInt())
        } catch (e: Exception) {
            DefaultCategoryColor
        }

    /**
     * Конвертирует цвет в hex строку
     */
    fun colorToHex(color: Color): String = color.toHexString()

    /**
     * Возвращает безопасный цвет категории: если чёрный/белый — подменяем на дефолтный.
     */
    fun ensureNonBlackWhite(color: Color): Color {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        val isBlack = r == 0 && g == 0 && b == 0
        val isWhite = r == 255 && g == 255 && b == 255
        return if (isBlack || isWhite) DefaultCategoryColor else color
    }
}
