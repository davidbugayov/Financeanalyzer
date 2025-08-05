package com.davidbugayov.financeanalyzer.domain.model

/**
 * Модель для представления подкатегории транзакции
 *
 * @property id Уникальный идентификатор подкатегории
 * @property name Название подкатегории
 * @property categoryId ID родительской категории
 * @property count Количество использований подкатегории (для сортировки по популярности)
 * @property isCustom Флаг, указывающий, является ли подкатегория кастомной (созданной пользователем)
 */
data class Subcategory(
    val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val count: Int = 0,
    val isCustom: Boolean = false,
) {

    companion object {

        /**
         * Создает подкатегорию
         */
        fun create(name: String, categoryId: Long, isCustom: Boolean = false, count: Int = 0): Subcategory {
            return Subcategory(
                name = name,
                categoryId = categoryId,
                count = count,
                isCustom = isCustom,
            )
        }
    }
}
