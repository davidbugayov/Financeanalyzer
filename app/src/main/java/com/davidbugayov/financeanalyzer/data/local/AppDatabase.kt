package com.davidbugayov.financeanalyzer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.davidbugayov.financeanalyzer.data.converter.Converters
import com.davidbugayov.financeanalyzer.data.dao.SavingGoalDao
import com.davidbugayov.financeanalyzer.data.dao.TransactionDao
import com.davidbugayov.financeanalyzer.domain.model.GoalAlert
import com.davidbugayov.financeanalyzer.domain.model.SavingGoal
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.TransactionTag

@Database(
    entities = [
        Transaction::class,
        TransactionTag::class,
        SavingGoal::class,
        GoalAlert::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingGoalDao(): SavingGoalDao
} 