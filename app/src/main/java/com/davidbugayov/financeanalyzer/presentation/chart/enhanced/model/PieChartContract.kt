package com.davidbugayov.financeanalyzer.presentation.chart.enhanced.model

import androidx.compose.ui.graphics.Color
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import kotlinx.datetime.LocalDate
import com.davidbugayov.financeanalyzer.presentation.chart.enhanced.PieChartData

/**
 * Контракт для взаимодействия между View и ViewModel круговой диаграммы
 * в соответствии с паттерном MVI
 */
class PieChartContract {

    /**
     * Интенты (намерения), отправляемые из View в ViewModel
     */
    sealed class Intent {
        /**
         * Загрузить данные о доходах
         */
        object LoadIncomeData : Intent()
        
        /**
         * Загрузить данные о расходах
         */
        object LoadExpenseData : Intent()
        
        /**
         * Загрузить данные о категориях (произвольные)
         */
        object LoadData : Intent()
        
        /**
         * Выбрать категорию
         */
        data class SelectCategory(val category: Category?, val isIncome: Boolean) : Intent()
        
        /**
         * Очистить выбор категории
         */
        object ClearSelection : Intent()
        
        /**
         * Очистить сообщение об ошибке
         */
        object ClearError : Intent()
    }

    /**
     * События, отправляемые из ViewModel в View
     */
    sealed class Event {
        /**
         * Данные загружены
         */
        object DataLoaded : Event()
        
        /**
         * Произошла ошибка
         */
        data class Error(val message: String) : Event()
        
        /**
         * Категория выбрана
         */
        data class CategorySelected(val category: Category, val isIncome: Boolean) : Event()
    }

    /**
     * Состояние ViewModel
     */
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val incomePieChartData: List<PieChartData> = emptyList(),
        val expensePieChartData: List<PieChartData> = emptyList(),
        val selectedCategory: Category? = null,
        val isIncomeSelected: Boolean = true
    ) {
        val hasIncomeData: Boolean get() = incomePieChartData.isNotEmpty()
        val hasExpenseData: Boolean get() = expensePieChartData.isNotEmpty()
    }
}

/**
 * Данные для элемента пирографа
 */
data class PieChartContractData(
    val id: String,
    val name: String,
    val amount: Float,
    val percentage: Float,
    val color: Color,
    val count: Int = 0,
    val startAngle: Float = 0f,
    val sweepAngle: Float = 0f,
    val category: Category? = null,
    val transactions: List<Transaction> = emptyList()
) {
    val formattedAmount: String
        get() = String.format("%.2f", amount)
        
    val formattedPercentage: String
        get() = String.format("%.1f%%", percentage)
} 