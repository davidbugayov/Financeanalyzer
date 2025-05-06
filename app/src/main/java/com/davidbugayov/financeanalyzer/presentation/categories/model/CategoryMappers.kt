package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.utils.ColorUtils

object CategoryColorProvider {
    fun getColor(context: Context, category: Category): Color = when (category.name) {
        context.getString(R.string.category_food) -> Color(ColorUtils.getColor(context, R.color.category_food))
        context.getString(R.string.category_transport) -> Color(ColorUtils.getColor(context, R.color.category_transport))
        context.getString(R.string.category_entertainment) -> Color(ColorUtils.getColor(context, R.color.category_entertainment))
        context.getString(R.string.category_health) -> Color(ColorUtils.getColor(context, R.color.category_health))
        context.getString(R.string.category_shopping) -> Color(ColorUtils.getColor(context, R.color.category_shopping))
        context.getString(R.string.category_housing) -> Color(ColorUtils.getColor(context, R.color.category_housing))
        context.getString(R.string.category_utilities) -> Color(ColorUtils.getColor(context, R.color.category_utilities))
        context.getString(R.string.category_salary) -> Color(ColorUtils.getColor(context, R.color.category_salary))
        context.getString(R.string.category_other) -> Color(ColorUtils.getColor(context, R.color.category_other))
        else -> if (category.isExpense) Color(ColorUtils.getColor(context, R.color.category_other)) else Color(ColorUtils.getColor(context, R.color.category_salary))
    }
}

fun Category.toUiCategory(
    context: Context,
    isCustom: Boolean = false,
    count: Int = 0
): UiCategory = UiCategory(
    id = this.id,
    name = this.name,
    isExpense = this.isExpense,
    isCustom = isCustom,
    count = count,
    original = this,
    color = CategoryColorProvider.getColor(context, this),
    icon = null
) 