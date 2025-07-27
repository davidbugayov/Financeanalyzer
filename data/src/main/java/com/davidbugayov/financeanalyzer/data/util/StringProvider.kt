package com.davidbugayov.financeanalyzer.data.util

import android.content.Context
import com.davidbugayov.financeanalyzer.data.R

/**
 * Утилитный класс для получения строковых ресурсов в модуле data
 */
object StringProvider {
    
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    
    fun getString(@androidx.annotation.StringRes resId: Int): String {
        return context?.getString(resId) ?: "String not found"
    }
    
    fun getString(@androidx.annotation.StringRes resId: Int, vararg formatArgs: Any): String {
        return context?.getString(resId, *formatArgs) ?: "String not found"
    }
    
    // Значения по умолчанию
    val defaultSource: String get() = getString(R.string.default_source)
    
    // Логирование
    fun logWalletMigrationCompleted(count: Int): String = getString(R.string.log_wallet_migration_completed, count)
    fun logErrorGettingWallets(transactionId: String): String = getString(R.string.log_error_getting_wallets, transactionId)
    val logTransactionRepositoryNotSet: String get() = getString(R.string.log_transaction_repository_not_set)
    fun logErrorGettingTransaction(transactionId: String): String = getString(R.string.log_error_getting_transaction, transactionId)
    fun logErrorStringConversion(value: String): String = getString(R.string.log_error_string_conversion, value)
    
    // Ошибки миграции
    val errorMigration1_2: String get() = getString(R.string.error_migration_1_2)
    val errorMigration2_3: String get() = getString(R.string.error_migration_2_3)
    val errorMigration3_4: String get() = getString(R.string.error_migration_3_4)
    val errorMigration4_5: String get() = getString(R.string.error_migration_4_5)
    val errorMigration5_6: String get() = getString(R.string.error_migration_5_6)
    val errorMigration6_7: String get() = getString(R.string.error_migration_6_7)
    val errorMigration7_8: String get() = getString(R.string.error_migration_7_8)
    val errorMigration8_9: String get() = getString(R.string.error_migration_8_9)
    val errorMigration9_10: String get() = getString(R.string.error_migration_9_10)
    
    // Успешные миграции
    val logMigration14_15Completed: String get() = getString(R.string.log_migration_14_15_completed)
    val logMigration15_14Completed: String get() = getString(R.string.log_migration_15_14_completed)
    val logMigration15_16Completed: String get() = getString(R.string.log_migration_15_16_completed)
    val logMigration16_15Completed: String get() = getString(R.string.log_migration_16_15_completed)
} 