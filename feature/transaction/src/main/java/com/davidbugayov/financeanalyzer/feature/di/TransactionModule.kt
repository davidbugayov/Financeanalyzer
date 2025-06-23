package com.davidbugayov.financeanalyzer.feature.di

import com.davidbugayov.financeanalyzer.feature.export_import.ExportImportViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for transaction feature.
 */
val transactionModule = module {
    // ViewModels
    viewModel { ExportImportViewModel(get()) }
} 