package com.davidbugayov.financeanalyzer.feature.transaction.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.feature.transaction.base.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.feature.transaction.base.defaultTransactionEventFactory
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * Экран добавления новой транзакции
 * Использует BaseTransactionScreen для отображения UI
 */
@Composable
fun AddTransactionScreen(
    category: String? = null,
) {
    val context = LocalContext.current
    val viewModel: AddTransactionViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

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

    BaseTransactionScreen(
        viewModel = viewModel,
        onNavigateBack = viewModel::onNavigateBack,
        screenTitle = stringResource(R.string.new_transaction_title),
        buttonText = stringResource(R.string.add_button_text),
        isEditMode = false,
        eventFactory = defaultTransactionEventFactory(false),
        submitEvent = BaseTransactionEvent.Submit,
        onNavigateToImport = viewModel::onNavigateToImport,
    )
}
