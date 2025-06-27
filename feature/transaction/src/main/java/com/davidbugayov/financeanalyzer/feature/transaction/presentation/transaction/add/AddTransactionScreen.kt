package com.davidbugayov.financeanalyzer.feature.transaction.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.feature.transaction.base.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.feature.transaction.base.defaultTransactionEventFactory
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.ErrorTracker
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import android.os.SystemClock
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Экран добавления новой транзакции
 * Использует BaseTransactionScreen для отображения UI
 */
@Composable
fun AddTransactionScreen(
    category: String? = null,
    viewModel: AddTransactionViewModel = koinViewModel(),
    userEventTracker: UserEventTracker = koinInject(),
    errorTracker: ErrorTracker = koinInject()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Отслеживаем время загрузки экрана
    val screenLoadStartTime = remember { SystemClock.elapsedRealtime() }
    
    LaunchedEffect(key1 = category) {
        category?.let {
            viewModel.setCategory(it)
        }
    }

    LaunchedEffect(Unit) {
        Timber.d(
            "AddTransactionScreen: Screen opened",
        )

        AnalyticsUtils.logScreenView(
            screenName = "add_transaction",
            screenClass = "AddTransactionScreen",
        )

        // Отслеживаем открытие экрана для аналитики пользовательских событий
        userEventTracker.trackScreenOpen(PerformanceMetrics.Screens.ADD_TRANSACTION)
        
        // Логируем использование функции
        userEventTracker.trackFeatureUsage("add_transaction_view")
        
        try {
            // Инициализация экрана
            viewModel.initializeScreen()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации экрана добавления транзакции")
            
            // Отслеживаем ошибку
            errorTracker.trackException(e, isFatal = false, mapOf(
                "location" to "add_transaction_screen",
                "action" to "initialize_screen"
            ))
        }
        
        // Завершаем отслеживание загрузки экрана
        PerformanceMetrics.endScreenLoadTiming(PerformanceMetrics.Screens.ADD_TRANSACTION)
        
        // Дополнительно отслеживаем время загрузки экрана
        val loadTime = SystemClock.elapsedRealtime() - screenLoadStartTime
        AnalyticsUtils.logEvent(AnalyticsConstants.Events.SCREEN_LOAD, android.os.Bundle().apply {
            putString(AnalyticsConstants.Params.SCREEN_NAME, "add_transaction")
            putLong(AnalyticsConstants.Params.DURATION_MS, loadTime)
        })
    }

    LaunchedEffect(state.forceExpense) {
        if (state.forceExpense) {
            viewModel.onEvent(BaseTransactionEvent.ForceSetExpenseType, context)
        } else {
            viewModel.onEvent(BaseTransactionEvent.ForceSetIncomeType, context)
        }
    }

    /*
    val achievementUnlocked by viewModel.achievementUnlocked.collectAsState()
    LaunchedEffect(achievementUnlocked) {
        if (achievementUnlocked) {
            // This needs to be handled via NavigationManager result API
            viewModel.resetAchievementUnlockedFlag()
        }
    }
     */

    // Отслеживаем закрытие экрана
    DisposableEffect(Unit) {
        onDispose {
            // Отслеживаем закрытие экрана
            userEventTracker.trackScreenClose(PerformanceMetrics.Screens.ADD_TRANSACTION)
        }
    }

    // Обработчик ошибок валидации
    LaunchedEffect(viewModel.validationErrors) {
        viewModel.validationErrors.forEach { (field, message) ->
            errorTracker.trackValidationError(field, message)
        }
    }

    // Обработчик успешного добавления транзакции
    LaunchedEffect(viewModel.transactionAdded) {
        if (viewModel.transactionAdded) {
            userEventTracker.trackFeatureUsage("transaction_added", AnalyticsConstants.Values.RESULT_SUCCESS)
            
            AnalyticsUtils.logEvent(AnalyticsConstants.Events.TRANSACTION_ADDED, android.os.Bundle().apply {
                putString(AnalyticsConstants.Params.TRANSACTION_TYPE, viewModel.transactionType.name)
                putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, viewModel.amount)
                putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, viewModel.category)
            })
        }
    }

    BaseTransactionScreen(
        viewModel = viewModel,
        onNavigateBack = {
            userEventTracker.trackUserAction("navigate_back", mapOf(
                "screen" to "add_transaction",
                "has_unsaved_changes" to viewModel.hasUnsavedChanges.toString()
            ))
            viewModel.onNavigateBack()
        },
        screenTitle = stringResource(R.string.new_transaction_title),
        buttonText = stringResource(R.string.add_button_text),
        isEditMode = false,
        eventFactory = defaultTransactionEventFactory(false),
        submitEvent = {
            userEventTracker.trackUserAction("submit_transaction", mapOf(
                "transaction_type" to viewModel.transactionType.name,
                "has_category" to (viewModel.category.isNotBlank()).toString(),
                "has_note" to (viewModel.note.isNotBlank()).toString()
            ))
            BaseTransactionEvent.Submit
        },
        onNavigateToImport = viewModel::onNavigateToImport,
    )
}
