package com.davidbugayov.financeanalyzer.domain.usecase.widgets

/**
 * Triggers widget refresh via injected [WidgetRefresher].
 */
class UpdateWidgetsUseCase(
    private val refresher: WidgetRefresher,
) {
    operator fun invoke() {
        refresher.refresh()
    }
} 