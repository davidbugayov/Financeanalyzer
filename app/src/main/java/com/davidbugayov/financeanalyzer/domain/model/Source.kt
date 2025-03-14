package com.davidbugayov.financeanalyzer.domain.model

/**
 * Доменная модель источника средств.
 * Представляет собой источник, откуда поступают или куда уходят деньги.
 *
 * @property id Уникальный идентификатор источника
 * @property name Название источника
 * @property color Цвет для визуального представления источника
 * @property isCustom Является ли источник пользовательским
 */
data class Source(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val isCustom: Boolean = false
) 