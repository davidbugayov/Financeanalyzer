package com.davidbugayov.financeanalyzer.feature.di

import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * Initializer for the transaction module.
 * This class is responsible for initializing the transaction module and loading its Koin modules.
 */
object TransactionModuleInitializer {

    private val transactionModule = module {
        // Empty for now, will be filled with actual dependencies later
    }

    /**
     * Initialize the transaction module.
     */
    fun initialize() {
        loadKoinModules(transactionModule)
    }
}
