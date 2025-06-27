package com.davidbugayov.financeanalyzer

import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils

/**
 * Основной класс приложения для Google flavor
 */
class FinanceApp : BaseFinanceApp() {
    override fun initFlavor() {
        AnalyticsUtils.logAppOpen()
    }
}
