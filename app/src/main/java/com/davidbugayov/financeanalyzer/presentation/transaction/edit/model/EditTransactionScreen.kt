package com.davidbugayov.financeanalyzer.presentation.transaction.edit.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.BaseTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.model.CategoryItem
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.utils.AnalyticsUtils
import java.util.Date
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.defaultTransactionEventFactory

/**
 * Экран редактирования существующей транзакции
 */
@Composable
fun EditTransactionScreen(
    viewModel: EditTransactionViewModel = koinViewModel(),
    categoriesViewModel: CategoriesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    transactionId: String? = null
) {
    // Get context at the Composable level
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AnalyticsUtils.logScreenView(
            screenName = "edit_transaction",
            screenClass = "EditTransactionScreen"
        )
        transactionId?.let {
            if (it.isNotEmpty()) {
                viewModel.loadTransactionForEdit(it)
            }
        }
    }

    BaseTransactionScreen(
        viewModel = viewModel,
        categoriesViewModel = categoriesViewModel,
        onNavigateBack = onNavigateBack,
        screenTitle = "Редактирование транзакции",
        buttonText = "Сохранить",
        isEditMode = true,
        eventFactory = defaultTransactionEventFactory(true),
        submitEvent = BaseTransactionEvent.SubmitEdit
    )
} 