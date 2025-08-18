package com.davidbugayov.financeanalyzer.shared.util

/**
 * Утилиты для корректного округления процентов до целых так,
 * чтобы итоговая сумма составляла ровно 100%.
 * Алгоритм — метод наибольших остатков (Largest Remainder Method).
 */
object PercentRounding {
    /**
     * Принимает исходные доли/проценты (необязательно нормализованные)
     * и возвращает список целых процентов, сумма которых равна 100.
     */
    fun roundToHundred(values: List<Double>): List<Int> {
        if (values.isEmpty()) return emptyList()

        val safeValues = values.map { if (it.isNaN() || it.isInfinite() || it < 0) 0.0 else it }
        val total = safeValues.sum()
        if (total <= 0.0) return List(safeValues.size) { 0 }

        // Нормализуем к 100
        val scaled = safeValues.map { it * (100.0 / total) }

        val floors = scaled.map { kotlin.math.floor(it).toInt() }.toMutableList()
        var remainder = 100 - floors.sum()

        if (remainder == 0) return floors

        // Остатки и индексы для распределения оставшихся процентов
        val indicesByRemainder = scaled
            .mapIndexed { index, v -> index to (v - kotlin.math.floor(v)) }
            .sortedWith(compareByDescending<Pair<Int, Double>> { it.second }
                .thenByDescending { scaled[it.first] })

        var i = 0
        while (remainder > 0 && i < indicesByRemainder.size) {
            val targetIndex = indicesByRemainder[i].first
            floors[targetIndex] = floors[targetIndex] + 1
            remainder--
            i++
        }

        return floors
    }

    fun roundToHundredFloats(values: List<Float>): List<Int> = roundToHundred(values.map { it.toDouble() })
}


