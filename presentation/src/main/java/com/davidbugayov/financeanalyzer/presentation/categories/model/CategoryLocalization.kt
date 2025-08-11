package com.davidbugayov.financeanalyzer.presentation.categories.model

import android.content.Context
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Утилита для нормализации и локализации названий дефолтных категорий.
 * Если в данных сохранено название на другом языке, функция вернёт
 * локализованное имя согласно текущей локали.
 */
object CategoryLocalization {

    // Карта: ключ категории -> список известных синонимов на RU/EN/ZH
    private val synonyms: Map<String, List<String>> = mapOf(
        "food" to listOf("продукты", "еда", "food", "groceries", "食品"),
        "transport" to listOf("транспорт", "transport", "交通"),
        "entertainment" to listOf("развлечения", "entertainment", "娱乐"),
        "restaurant" to listOf("рестораны", "ресторан", "restaurants", "restaurant", "餐厅"),
        "health" to listOf("здоровье", "health", "健康"),
        "clothing" to listOf("одежда", "clothing", "clothes", "衣服"),
        "housing" to listOf("жилье", "жильё", "housing", "home", "住房"),
        "communication" to listOf("связь", "communication", "通讯"),
        "pet" to listOf("питомцы", "pets", "pet", "宠物"),
        "services" to listOf("услуги", "services", "服务"),
        "charity" to listOf("благотворительность", "charity", "慈善"),
        "credit" to listOf("кредиты", "кредит", "credit", "贷款"),
        "transfer" to listOf("перевод", "transfers", "transfer", "transfer\u044b", "转账"),
        "other_expense" to listOf("другие расходы", "прочие", "other expenses", "other expense", "其他支出"),
        "salary" to listOf("зарплата", "salary", "工资"),
        "freelance" to listOf("фриланс", "freelance", "自由职业"),
        "gifts" to listOf("подарки", "gifts", "礼物"),
        "interest" to listOf("проценты", "interest", "利息"),
        "rental" to listOf("аренда", "rental", "rent", "租金"),
        "other_income" to listOf("другие доходы", "other income", "其他收入"),
        "other" to listOf("другое", "other", "其他"),
    )

    private fun keyFor(name: String): String? {
        val lower = name.trim().lowercase()
        return synonyms.entries.firstOrNull { entry ->
            entry.value.any { syn -> lower == syn.lowercase() }
        }?.key
    }

    fun displayName(context: Context, rawName: String): String {
        val key = keyFor(rawName)
        return if (key != null) {
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
        } else {
            rawName
        }
    }
}


