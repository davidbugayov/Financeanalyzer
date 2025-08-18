package com.davidbugayov.financeanalyzer.ui.utils

import android.content.Context
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Локализация дефолтных категорий без зависимости от presentation-модуля.
 * Для кастомных категорий возвращает исходное имя.
 */
object DefaultCategoryLocalization {
    private val synonyms: Map<String, List<String>> = mapOf(
        "food" to listOf("продукты", "еда", "food", "groceries", "products", "product", "食品", "食物"),
        "transport" to listOf("транспорт", "transport", "交通", "运输"),
        "entertainment" to listOf("развлечения", "entertainment", "娱乐"),
        "restaurant" to listOf("рестораны", "ресторан", "restaurants", "restaurant", "餐厅", "饭店"),
        "health" to listOf("здоровье", "health", "健康"),
        "clothing" to listOf("одежда", "clothing", "clothes", "cloth", "衣服", "服装"),
        "housing" to listOf("жилье", "жильё", "housing", "home", "住房", "住宅"),
        "communication" to listOf("связь", "communication", "connection", "通讯", "通信"),
        "pet" to listOf("питомцы", "pets", "pet", "宠物"),
        "services" to listOf("услуги", "services", "服务"),
        "charity" to listOf("благотворительность", "charity", "慈善"),
        "credit" to listOf("кредиты", "кредит", "credit", "loan", "loans", "贷款"),
        "utilities" to listOf("коммунальные", "коммунальные платежи", "utilities", "utility", "公用事业"),
        "education" to listOf("образование", "учеба", "учёба", "education", "study", "教育", "学习"),
        "shopping" to listOf("покупки", "шопинг", "shopping", "购物"),
        "subscription" to listOf("подписка", "подписки", "subscription", "subscriptions", "订阅"),
        "cafe" to listOf("кафе", "cafe", "咖啡馆", "咖啡店"),
        "salary" to listOf("зарплата", "salary", "工资"),
        "freelance" to listOf("фриланс", "freelance", "自由职业"),
        "gifts" to listOf("подарки", "gifts", "礼物"),
        "interest" to listOf("проценты", "interest", "利息"),
        "rental" to listOf("аренда", "rental", "rent", "租金"),
        "other_income" to listOf("другие доходы", "other income", "其他收入"),
        "other_expense" to listOf("другие расходы", "прочие", "other expenses", "other expense", "其他支出"),
        "other" to listOf("другое", "other", "其他"),
        "transfer" to listOf("перевод", "переводы", "transfer", "transfers", "转账")
    )

    private fun keyFor(name: String): String? {
        val lower = name.trim().lowercase()
        return synonyms.entries.firstOrNull { entry ->
            entry.value.any { syn -> lower == syn.lowercase() }
        }?.key
    }

    /**
     * Возвращает локализованное имя дефолтной категории. Если это не дефолтная категория,
     * возвращает исходное имя.
     */
    fun displayName(context: Context, rawName: String): String {
        return when (keyFor(rawName)) {
            "food" -> context.getString(R.string.category_food)
            "transport" -> context.getString(R.string.category_transport)
            "entertainment" -> context.getString(R.string.category_entertainment)
            "restaurant" -> context.getString(R.string.category_restaurant)
            "health" -> context.getString(R.string.category_health)
            "clothing" -> context.getString(R.string.category_clothing)
            "housing" -> context.getString(R.string.category_housing)
            "communication" -> context.getString(R.string.category_communication)
            "pet" -> context.getString(R.string.category_pet)
            "services" -> context.getString(R.string.category_services)
            "charity" -> context.getString(R.string.category_charity)
            "credit" -> context.getString(R.string.category_credit)
            "utilities" -> context.getString(R.string.category_utilities)
            "education" -> context.getString(R.string.category_education)
            "shopping" -> context.getString(R.string.category_shopping)
            "subscription" -> context.getString(R.string.category_subscription)
            "cafe" -> context.getString(R.string.category_cafe)
            "salary" -> context.getString(R.string.category_salary)
            "freelance" -> context.getString(R.string.category_freelance)
            "gifts" -> context.getString(R.string.category_gifts)
            "interest" -> context.getString(R.string.category_interest)
            "rental" -> context.getString(R.string.category_rental)
            "other_income" -> context.getString(R.string.category_other_income)
            "other_expense" -> context.getString(R.string.category_other_expense)
            "other" -> context.getString(R.string.category_other)
            "transfer" -> context.getString(R.string.category_transfer)
            else -> rawName
        }
    }
}


