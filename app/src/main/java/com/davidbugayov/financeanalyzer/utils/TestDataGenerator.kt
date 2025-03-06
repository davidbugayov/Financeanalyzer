package com.davidbugayov.financeanalyzer.utils

import com.davidbugayov.financeanalyzer.domain.model.Transaction
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
    
    private val expenseTitles = mapOf(
        "Продукты" to listOf("Супермаркет", "Овощи и фрукты", "Мясо и рыба", "Хлебобулочные изделия", "Молочные продукты"),
        "Транспорт" to listOf("Бензин", "Такси", "Общественный транспорт", "Каршеринг", "Метро"),
        "Развлечения" to listOf("Кино", "Театр", "Концерт", "Боулинг", "Настольные игры"),
        "Здоровье" to listOf("Лекарства", "Стоматолог", "Врач", "Анализы", "Витамины"),
        "Одежда" to listOf("Куртка", "Обувь", "Джинсы", "Футболки", "Аксессуары"),
        "Рестораны" to listOf("Кафе", "Ресторан", "Фастфуд", "Доставка еды", "Бар"),
        "Коммунальные платежи" to listOf("Электричество", "Вода", "Газ", "Интернет", "Телефон"),
        "Другое" to listOf("Подарки", "Книги", "Канцтовары", "Бытовая химия", "Прочие расходы")
    )
    
    private val incomeTitles = mapOf(
        "Зарплата" to listOf("Основная работа", "Премия", "Аванс", "Бонус", "Отпускные"),
        "Фриланс" to listOf("Проект", "Консультация", "Разработка", "Дизайн", "Перевод"),
        "Подарки" to listOf("День рождения", "Новый год", "Юбилей", "Свадьба", "Праздник"),
        "Инвестиции" to listOf("Дивиденды", "Проценты по вкладу", "Доход от акций", "Аренда", "Продажа"),
        "Другое" to listOf("Возврат долга", "Налоговый вычет", "Страховка", "Выигрыш", "Прочие доходы")
    )
    
    /**
     * Генерирует список тестовых транзакций
     * @param count Количество транзакций для генерации
     * @return Список сгенерированных транзакций
     */
    fun generateTransactions(count: Int): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val calendar = Calendar.getInstance()
        
        // Устанавливаем начальную дату (30 дней назад)
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        val startDate = calendar.timeInMillis
        
        // Текущая дата
        val endDate = System.currentTimeMillis()
        
        for (i in 0 until count) {
            // Генерируем случайную дату в диапазоне последних 30 дней
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
            
            // Выбираем случайное название в зависимости от категории
            val titles = if (isExpense) {
                expenseTitles[category] ?: listOf("Неизвестно")
            } else {
                incomeTitles[category] ?: listOf("Неизвестно")
            }
            val title = titles.random()
            
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
            transactions.add(
                Transaction(
                    id = i.toLong(),
                    title = title,
                    amount = amount.toInt().toDouble(), // Округляем до целых
                    category = category,
                    isExpense = isExpense,
                    date = date,
                    note = if (Random.nextBoolean()) "Примечание к транзакции" else null
                )
            )
        }
        
        // Сортируем по дате (от новых к старым)
        return transactions.sortedByDescending { it.date }
    }
} 