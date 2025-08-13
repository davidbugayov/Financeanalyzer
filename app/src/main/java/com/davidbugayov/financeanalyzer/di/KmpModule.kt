package com.davidbugayov.financeanalyzer.di

import com.davidbugayov.financeanalyzer.shared.SharedFacade
import com.davidbugayov.financeanalyzer.shared.repository.SubcategoryRepository as KmpSubcategoryRepository
import com.davidbugayov.financeanalyzer.shared.repository.TransactionRepository as KmpTransactionRepository
import com.davidbugayov.financeanalyzer.shared.repository.WalletRepository as KmpWalletRepository
import com.davidbugayov.financeanalyzer.utils.kmp.SharedSubcategoryRepositoryAdapter
import com.davidbugayov.financeanalyzer.utils.kmp.SharedTransactionRepositoryAdapter
import com.davidbugayov.financeanalyzer.utils.kmp.SharedWalletRepositoryAdapter
import org.koin.dsl.module

/**
 * DI-модуль для интеграции KMP SharedFacade в Android.
 */
val kmpModule =
    module {
        // Адаптируем репозитории под KMP интерфейсы
        single<KmpTransactionRepository> { SharedTransactionRepositoryAdapter(get()) }
        single<KmpSubcategoryRepository> { SharedSubcategoryRepositoryAdapter(get()) }
        single<KmpWalletRepository> { SharedWalletRepositoryAdapter(get()) }

        // SharedFacade с подключенными репозиториями и appScope
        single {
            SharedFacade(
                transactionRepository = get<KmpTransactionRepository>(),
                walletRepository = get<KmpWalletRepository>(),
                subcategoryRepository = get<KmpSubcategoryRepository>(),
                achievementsRepository = null,
                appScope = get(),
            )
        }
    }
