package com.davidbugayov.financeanalyzer.domain.usecase.widgets

/**
 * Contract for refreshing home-screen widgets.
 * Domain layer doesn't know how it's done; platform-specific implementation lives in :feature:widget.
 */
interface WidgetRefresher {
    fun refresh()
} 