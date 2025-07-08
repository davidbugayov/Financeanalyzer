package com.davidbugayov.financeanalyzer.feature.transaction.presentation.transaction.add

import android.os.Bundle
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.ErrorTracker
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import com.davidbugayov.financeanalyzer.analytics.UserEventTracker
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.feature.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.feature.transaction.base.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.feature.transaction.base.defaultTransactionEventFactory
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
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
    forceExpense: Boolean? = null,
    viewModel: AddTransactionViewModel = koinViewModel(),
    userEventTracker: UserEventTracker = koinInject(),
    errorTracker: ErrorTracker = koinInject(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Отслеживаем время загрузки экрана
    val screenLoadStartTime = remember { SystemClock.elapsedRealtime() }

    LaunchedEffect(key1 = forceExpense) {
        forceExpense?.let { shouldForceExpense ->
            Timber.d("AddTransactionScreen: Setting forceExpense to $shouldForceExpense")
            viewModel.setForceExpense(shouldForceExpense)

            // Сразу отправляем событие для принудительной установки типа
            if (shouldForceExpense) {
                viewModel.onEvent(BaseTransactionEvent.ForceSetExpenseType, context)
            } else {
                viewModel.onEvent(BaseTransactionEvent.ForceSetIncomeType, context)
            }
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
            // После сброса полей применяем category, если он был передан
            category?.let { viewModel.setCategory(it) }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации экрана добавления транзакции")

            // Отслеживаем ошибку
            errorTracker.trackException(
                e,
                isFatal = false,
                mapOf(
                    "location" to "add_transaction_screen",
                    "action" to "initialize_screen",
                ),
            )
        }

        // Завершаем отслеживание загрузки экрана
        PerformanceMetrics.endScreenLoadTiming(PerformanceMetrics.Screens.ADD_TRANSACTION)

        // Дополнительно отслеживаем время загрузки экрана
        val loadTime = SystemClock.elapsedRealtime() - screenLoadStartTime
        AnalyticsUtils.logEvent(
            AnalyticsConstants.Events.SCREEN_LOAD,
            Bundle().apply {
                putString(AnalyticsConstants.Params.SCREEN_NAME, "add_transaction")
                putLong(AnalyticsConstants.Params.DURATION_MS, loadTime)
            },
        )
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

    // Обработчик успешного добавления транзакции
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            userEventTracker.trackFeatureUsage("transaction_added", AnalyticsConstants.Values.RESULT_SUCCESS)

            AnalyticsUtils.logEvent(
                AnalyticsConstants.Events.TRANSACTION_ADDED,
                Bundle().apply {
                    putString(AnalyticsConstants.Params.TRANSACTION_TYPE, if (state.isExpense) "EXPENSE" else "INCOME")
                    putString(AnalyticsConstants.Params.TRANSACTION_AMOUNT, state.amount)
                    putString(AnalyticsConstants.Params.TRANSACTION_CATEGORY, state.category)
                },
            )
        }
    }

    BaseTransactionScreen(
        viewModel = viewModel,
        onNavigateBack = {
            userEventTracker.trackUserAction(
                "navigate_back",
                mapOf(
                    "screen" to "add_transaction",
                    "has_unsaved_changes" to
                        (
                            state.amount.isNotBlank() ||
                                state.title.isNotBlank() ||
                                state.category.isNotBlank() ||
                                state.note.isNotBlank()
                        ).toString(),
                ),
            )
            viewModel.onNavigateBack()
        },
        screenTitle = stringResource(R.string.new_transaction_title),
        buttonText = stringResource(R.string.add_button_text),
        isEditMode = false,
        eventFactory = defaultTransactionEventFactory(false),
        submitEvent = BaseTransactionEvent.Submit,
        onNavigateToImport = viewModel::onNavigateToImport,
    )
}
