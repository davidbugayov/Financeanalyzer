package com.davidbugayov.financeanalyzer.feature.statistics.di

import com.davidbugayov.financeanalyzer.presentation.chart.statistic.viewmodel.EnhancedFinanceChartViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Модуль DI для feature statistics
 */
val statisticsModule = module {
    viewModel { EnhancedFinanceChartViewModel() }
}
