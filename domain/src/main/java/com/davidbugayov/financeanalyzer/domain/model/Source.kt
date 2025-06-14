package com.davidbugayov.financeanalyzer.domain.model

/**
 * Модель для представления источника транзакции
 *
 * @property id Уникальный идентификатор источника
 * @property name Название источника
 * @property color Цвет источника
 * @property isCustom Флаг, указывающий, является ли источник кастомным (созданным пользователем)
 */
data class Source(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val isCustom: Boolean = false,
) 