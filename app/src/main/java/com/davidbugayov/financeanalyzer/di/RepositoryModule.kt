package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.data.repository.DebtRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.SubcategoryMapper
import com.davidbugayov.financeanalyzer.data.repository.SubcategoryRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.TransactionMapper
import com.davidbugayov.financeanalyzer.data.repository.UnifiedTransactionRepositoryImpl
import com.davidbugayov.financeanalyzer.data.repository.WalletRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepository
import com.davidbugayov.financeanalyzer.domain.repository.AchievementsRepositoryImpl
import com.davidbugayov.financeanalyzer.domain.repository.DebtRepository
import com.davidbugayov.financeanalyzer.domain.repository.ITransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.SubcategoryRepository
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.UnifiedTransactionRepository
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import org.koin.dsl.module

/**
 * Модуль для предоставления репозиториев.
 * Отделен от основного модуля для лучшей модульности и поддержки.
 */
val repositoryModule =
    module {
        // Мапперы для преобразования моделей
        single { TransactionMapper() }
        single { SubcategoryMapper }

        // Унифицированный репозиторий транзакций
        single { UnifiedTransactionRepositoryImpl(get(), get()) }
        single<UnifiedTransactionRepository> { get<UnifiedTransactionRepositoryImpl>() }

        // Предоставляем UnifiedTransactionRepository как реализацию TransactionRepository и ITransactionRepository
        // для обратной совместимости
        single<TransactionRepository> { get<UnifiedTransactionRepositoryImpl>() }
        single<ITransactionRepository> { get<UnifiedTransactionRepositoryImpl>() }

        // Другие репозитории
        single<WalletRepository> { WalletRepositoryImpl(get(), get()) }
        single<DebtRepository> { DebtRepositoryImpl(get()) }

        // Репозиторий подкатегорий
        single<SubcategoryRepository> { SubcategoryRepositoryImpl(get()) }

        // Репозиторий достижений
        single<AchievementsRepository> { AchievementsRepositoryImpl(get()) }
    }
