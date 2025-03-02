package com.davidbugayov.financeanalyzer.di

import android.content.Context
import androidx.room.Room
import com.davidbugayov.financeanalyzer.data.local.FinanceDatabase
import com.davidbugayov.financeanalyzer.data.local.dao.TransactionDao
import com.davidbugayov.financeanalyzer.data.local.dao.BudgetDao
import com.davidbugayov.financeanalyzer.data.local.dao.SavingGoalDao

fun provideDatabase(context: Context): FinanceDatabase {
    return Room.databaseBuilder(
        context,
        FinanceDatabase::class.java,
        "finance_database"
    ).build()
}

fun provideTransactionDao(database: FinanceDatabase): TransactionDao = database.transactionDao()

fun provideBudgetDao(database: FinanceDatabase): BudgetDao = database.budgetDao()

fun provideSavingGoalDao(database: FinanceDatabase): SavingGoalDao = database.savingGoalDao() 