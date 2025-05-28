package com.davidbugayov.financeanalyzer.presentation.transaction.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.achievements.AchievementsUiViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.base.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.base.defaultTransactionEventFactory
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

/**
 * Экран добавления новой транзакции
 * Использует BaseTransactionScreen для отображения UI
 * @param navController контроллер навигации для передачи флага достижения
 */
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImport: (() -> Unit)? = null,
    navController: NavController,
    achievementsUiViewModel: AchievementsUiViewModel
) {
    val context = LocalContext.current
    val viewModel: AddTransactionViewModel = koinViewModel(parameters = { parametersOf(achievementsUiViewModel) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        Timber.d("AddTransactionScreen: Screen opened, onNavigateToImport is ${if (onNavigateToImport != null) "provided" else "null"}")
        
        AnalyticsUtils.logScreenView(
            screenName = "add_transaction",
            screenClass = "AddTransactionScreen"
        )
    }

    LaunchedEffect(state.forceExpense) {
        if (state.forceExpense) {
            viewModel.onEvent(BaseTransactionEvent.ForceSetExpenseType, context)
        } else {
            viewModel.onEvent(BaseTransactionEvent.ForceSetIncomeType, context)
        }
    }

    val achievementUnlocked by viewModel.achievementUnlocked.collectAsState()
    LaunchedEffect(achievementUnlocked) {
        if (achievementUnlocked) {
            navController.previousBackStackEntry?.savedStateHandle?.set("show_achievement_feedback", true)
            viewModel.resetAchievementUnlockedFlag()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // No need to reset navigateBackCallback as it's not used in the new implementation
        }
    }

    BaseTransactionScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        screenTitle = stringResource(R.string.new_transaction_title),
        buttonText = stringResource(R.string.add_button_text),
        isEditMode = false,
        eventFactory = defaultTransactionEventFactory(false),
        submitEvent = BaseTransactionEvent.Submit,
        onNavigateToImport = onNavigateToImport
    )
}