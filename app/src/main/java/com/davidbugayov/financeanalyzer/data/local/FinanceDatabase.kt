package com.davidbugayov.financeanalyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.davidbugayov.financeanalyzer.data.local.converter.DateConverter
import com.davidbugayov.financeanalyzer.data.local.converter.ListConverter
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.dao.BudgetDao
import com.davidbugayov.financeanalyzer.data.local.dao.SavingGoalDao
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Budget
import com.davidbugayov.financeanalyzer.domain.model.SavingGoal

@Database(
    entities = [
        Transaction::class,
        Budget::class,
        SavingGoal::class
    ],
    version = 1
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingGoalDao(): SavingGoalDao
} 