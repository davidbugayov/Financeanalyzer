package com.davidbugayov.financeanalyzer.domain.usecase

import com.davidbugayov.financeanalyzer.domain.model.AppException.GenericAppException
import com.davidbugayov.financeanalyzer.domain.model.Money
import com.davidbugayov.financeanalyzer.domain.model.ProfileAnalyticsData
import com.davidbugayov.financeanalyzer.domain.model.Result
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class GetProfileAnalyticsUseCase(
    private val transactionRepository: ITransactionRepository,
    private val calculateBalanceMetricsUseCase: CalculateBalanceMetricsUseCase
) {
    // TODO: Рассмотреть возможность сделать SimpleDateFormat полем класса или передавать как зависимость,
    // если он будет использоваться в нескольких местах и требует специфичной конфигурации.
    // Для простоты пока оставим его локальным.

    suspend fun execute(): Result<ProfileAnalyticsData> = withContext(Dispatchers.Default) {
        try {
            // Предполагаем, что transactionRepository.loadTransactions() или аналогичный метод
            // возвращает List<Transaction> или выбрасывает исключение.
            // Если он возвращает Result<List<Transaction>>, логику нужно будет адаптировать.
            val transactions = transactionRepository.loadTransactions() // Убедитесь, что такой метод есть

            if (transactions.isEmpty()) {
                // Возвращаем данные по умолчанию для пустого списка транзакций
                return@withContext Result.Success(
                    ProfileAnalyticsData(
                        totalIncome = Money.zero(),
                        totalExpense = Money.zero(),
                        balance = Money.zero(),
                        savingsRate = 0.0,
                        totalTransactions = 0,
                        totalExpenseCategories = 0,
                        totalIncomeCategories = 0,
                        averageExpense = Money.zero().format(), // или "0" или соответствующая строка
                        totalSourcesUsed = 0,
                        dateRange = "Нет транзакций" // или ресурс строки
                    )
                )
            }

            val balanceMetrics = calculateBalanceMetricsUseCase(transactions)
            val totalIncome = balanceMetrics.income
            val totalExpense = balanceMetrics.expense
            val balance = totalIncome - totalExpense

            val savingsRate = if (!totalIncome.isZero()) {
                (balance.percentageOf(totalIncome)).coerceIn(0.0, 100.0)
            } else {
                0.0
            }

            val totalTransactionsCount = transactions.size

            val uniqueExpenseCategories = transactions
                .filter { it.isExpense }
                .map { it.category }
                .distinct()
                .size

            val uniqueIncomeCategories = transactions
                .filter { !it.isExpense }
                .map { it.category }
                .distinct()
                .size

            val expenseTransactions = transactions.filter { it.isExpense }
            val avgExpense = if (expenseTransactions.isNotEmpty()) {
                totalExpense / expenseTransactions.size
            } else {
                Money.zero()
            }
            val formattedAvgExpense = avgExpense.format()

            val uniqueSources = transactions
                .map { it.source }
                .distinct()
                .size

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
            val oldestDate = transactions.minByOrNull { it.date }?.date
            val newestDate = transactions.maxByOrNull { it.date }?.date
            val dateRangeString = if (oldestDate != null && newestDate != null) {
                "${dateFormat.format(oldestDate)} - ${dateFormat.format(newestDate)}"
            } else {
                "Все время" // Рассмотреть возможность использования строкового ресурса
            }

            Result.Success(
                ProfileAnalyticsData(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = balance,
                    savingsRate = savingsRate,
                    totalTransactions = totalTransactionsCount,
                    totalExpenseCategories = uniqueExpenseCategories,
                    totalIncomeCategories = uniqueIncomeCategories,
                    averageExpense = formattedAvgExpense,
                    totalSourcesUsed = uniqueSources,
                    dateRange = dateRangeString
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error executing GetProfileAnalyticsUseCase") // Логируем ошибку
            // Оборачиваем общее исключение в кастомное AppException
            // Замените GenericAppException на ваш конкретный подтип AppException, если есть
            Result.Error(GenericAppException("Failed to load profile analytics", e))
        }
    }
} 