package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Category
import androidx.core.content.ContextCompat
import kotlin.random.Random

object CategoryProvider {
    data class CategoryMeta(
        val nameRes: Int,
        val iconName: String,
        val colorRes: Int,
        val isExpense: Boolean
    )

    val defaultCategories = listOf(
        CategoryMeta(R.string.category_food, "ShoppingCart", R.color.category_food, true),
        CategoryMeta(R.string.category_transport, "DirectionsCar", R.color.category_transport, true),
        CategoryMeta(R.string.category_entertainment, "Movie", R.color.category_entertainment, true),
        CategoryMeta(R.string.category_restaurant, "Restaurant", R.color.category_restaurant, true),
        CategoryMeta(R.string.category_health, "LocalHospital", R.color.category_health, true),
        CategoryMeta(R.string.category_clothing, "Checkroom", R.color.category_clothing, true),
        CategoryMeta(R.string.category_housing, "Home", R.color.category_housing, true),
        CategoryMeta(R.string.category_communication, "Phone", R.color.category_communication, true),
        CategoryMeta(R.string.category_pet, "Pets", R.color.category_pet, true),
        CategoryMeta(R.string.category_services, "Work", R.color.category_services, true),
        CategoryMeta(R.string.category_charity, "Payments", R.color.category_charity, true),
        CategoryMeta(R.string.category_credit, "CreditCard", R.color.category_credit, true),
        CategoryMeta(R.string.category_transfer, "SwapHoriz", R.color.category_transfer, true),
        CategoryMeta(R.string.category_other, "MoreHoriz", R.color.category_other, true),
        CategoryMeta(R.string.category_salary, "Payments", R.color.category_salary, false),
        CategoryMeta(R.string.category_freelance, "Work", R.color.category_freelance, false),
        CategoryMeta(R.string.category_gift, "Payments", R.color.category_gift, false),
        CategoryMeta(R.string.category_interest, "TrendingUp", R.color.category_interest, false),
        CategoryMeta(R.string.category_rent, "Work", R.color.category_rent, false),
        CategoryMeta(R.string.category_other, "MoreHoriz", R.color.category_other, false)
    )

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
                color = getColor(context, meta.colorRes),
                icon = CategoryIconProvider.getIconByName(meta.iconName)
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
                color = getColor(context, meta.colorRes),
                icon = CategoryIconProvider.getIconByName(meta.iconName)
            )
        }

    fun getColor(context: Context, colorRes: Int): Color =
        Color(ContextCompat.getColor(context, colorRes))

    fun generateRandomCategoryColor(): Color {
        val hue = Random.nextFloat() * 360f
        val saturation = 0.6f + Random.nextFloat() * 0.4f // 0.6 - 1.0
        val value = 0.7f + Random.nextFloat() * 0.3f      // 0.7 - 1.0
        return Color.hsv(hue, saturation, value)
    }
} 