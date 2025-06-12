package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.TransactionMapper
import com.davidbugayov.financeanalyzer.data.repository.TransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.UnifiedTransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.WalletRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.UnifiedTransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import org.koin.dsl.module

/**
 * Модуль для предоставления репозиториев.
 * Отделен от основного модуля для лучшей модульности и поддержки.
 */
val repositoryModule = module {
    // Маппер для преобразования моделей
    single { TransactionMapper() }

    // Репозитории
    single { TransactionRepositoryImpl(get()) }
    single<TransactionRepository> { get<TransactionRepositoryImpl>() }
    single<ITransactionRepository> { get<TransactionRepositoryImpl>() }

    // Унифицированный репозиторий транзакций
    single { UnifiedTransactionRepositoryImpl(get(), get()) }
    single<UnifiedTransactionRepository> { get<UnifiedTransactionRepositoryImpl>() }

    // Другие репозитории
    single<WalletRepository> { WalletRepositoryImpl(get(), get()) }
    single { AchievementsRepository() }
}
