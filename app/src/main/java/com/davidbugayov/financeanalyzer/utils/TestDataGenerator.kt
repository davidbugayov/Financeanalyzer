package com.davidbugayov.financeanalyzer.utils

import androidx.compose.ui.graphics.toArgb
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.ui.theme.ExpenseColorInt
import com.davidbugayov.financeanalyzer.ui.theme.IncomeColorInt
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

/**
 * Утилитарный класс для генерации тестовых данных транзакций.
 * В String не выносить!
 */
object TestDataGenerator {
    
    private val expenseCategories = listOf(
        "Продукты", "Транспорт", "Развлечения", "Здоровье", 
        "Одежда", "Рестораны", "Коммунальные платежи", "Другое"
    )
    
    private val incomeCategories = listOf(
        "Зарплата", "Фриланс", "Подарки", "Инвестиции", "Другое"
    )
    
    /**
     * Генерирует список тестовых транзакций
     * @param count Количество транзакций для генерации (по умолчанию 100)
     * @return Список сгенерированных транзакций за последний календарный месяц
     */
    fun generateTransactions(count: Int = 100): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val calendar = Calendar.getInstance()

        // Получаем текущую дату
        val endDate = calendar.timeInMillis
        
        // Устанавливаем начальную дату (1 месяц назад)
        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.timeInMillis
        
        for (i in 0 until count) {
            // Генерируем случайную дату в диапазоне последнего месяца
            val randomDate = Random.nextLong(startDate, endDate)
            val date = Date(randomDate)
            
            // Определяем, будет ли это расход или доход (70% расходов, 30% доходов)
            val isExpense = Random.nextDouble() < 0.7
            
            // Выбираем случайную категорию
            val category = if (isExpense) {
                expenseCategories.random()
            } else {
                incomeCategories.random()
            }
            
            // Генерируем случайную сумму
            val amount = if (isExpense) {
                when (category) {
                    "Продукты" -> Random.nextDouble(100.0, 5000.0)
                    "Транспорт" -> Random.nextDouble(50.0, 1000.0)
                    "Развлечения" -> Random.nextDouble(500.0, 3000.0)
                    "Здоровье" -> Random.nextDouble(200.0, 10000.0)
                    "Одежда" -> Random.nextDouble(1000.0, 15000.0)
                    "Рестораны" -> Random.nextDouble(500.0, 5000.0)
                    "Коммунальные платежи" -> Random.nextDouble(1000.0, 8000.0)
                    else -> Random.nextDouble(100.0, 3000.0)
                }
            } else {
                when (category) {
                    "Зарплата" -> Random.nextDouble(30000.0, 150000.0)
                    "Фриланс" -> Random.nextDouble(5000.0, 50000.0)
                    "Подарки" -> Random.nextDouble(1000.0, 10000.0)
                    "Инвестиции" -> Random.nextDouble(1000.0, 20000.0)
                    else -> Random.nextDouble(1000.0, 5000.0)
                }
            }
            
            // Создаем транзакцию и добавляем в список
            val source = if (Random.nextBoolean()) "Сбер" else if (Random.nextBoolean()) "Т-Банк" else "Наличные"
            transactions.add(
                Transaction(
                    id = i.toString(),
                    amount = Money(amount.toInt().toDouble()), // Округляем до целых
                    category = category,
                    isExpense = isExpense,
                    date = date,
                    note = if (Random.nextBoolean()) "Примечание к транзакции" else null,
                    source = source,
                    sourceColor = ColorUtils.getSourceColorByName(source)?.toArgb()
                        ?: if (isExpense) ExpenseColorInt else IncomeColorInt
                )
            )
        }
        
        // Сортируем по дате (от новых к старым)
        return transactions.sortedByDescending { it.date }
    }
} 