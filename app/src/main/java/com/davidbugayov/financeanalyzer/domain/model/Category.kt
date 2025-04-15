package com.davidbugayov.financeanalyzer.domain.model

/**
 * Модель для представления категории транзакции
 *
 * @property id Уникальный идентификатор категории
 * @property name Название категории
 * @property isExpense Флаг, указывающий является ли категория для расходов (true) или для доходов (false)
 * @property count Количество использований категории (для сортировки по популярности)
 * @property isCustom Флаг, указывающий, является ли категория кастомной (созданной пользователем)
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val isExpense: Boolean,
    val count: Int = 0,
    val isCustom: Boolean = false
) {
    companion object {
        /**
         * Создает категорию для расходов
         */
        fun expense(name: String, isCustom: Boolean = false, count: Int = 0): Category {
            return Category(
                name = name,
                isExpense = true,
                count = count,
                isCustom = isCustom
            )
        }
        
        /**
         * Создает категорию для доходов
         */
        fun income(name: String, isCustom: Boolean = false, count: Int = 0): Category {
            return Category(
                name = name,
                isExpense = false,
                count = count, 
                isCustom = isCustom
            )
        }
        
        /**
         * Предопределенные категории расходов
         */
        val EXPENSE_CATEGORIES = listOf(
            expense("Продукты"),
            expense("Транспорт"),
            expense("Развлечения"),
            expense("Здоровье"),
            expense("Кафе и рестораны"),
            expense("Одежда"),
            expense("Коммунальные услуги"),
            expense("Подписки"),
            expense("Образование"),
            expense("Переводы"),
            expense("Прочее")
        )
        
        /**
         * Предопределенные категории доходов
         */
        val INCOME_CATEGORIES = listOf(
            income("Зарплата"),
            income("Фриланс"),
            income("Подарки"),
            income("Проценты"),
            income("Переводы"),
            income("Прочее")
        )
    }
} 