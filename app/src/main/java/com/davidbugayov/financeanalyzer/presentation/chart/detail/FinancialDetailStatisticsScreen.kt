package com.davidbugayov.financeanalyzer.presentation.chart.detail
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.chart.detail.components.ExpenseAnalysisCard
import com.davidbugayov.financeanalyzer.presentation.chart.detail.components.KeyMetricsCard
import com.davidbugayov.financeanalyzer.presentation.chart.detail.components.RecommendationsCard
import com.davidbugayov.financeanalyzer.presentation.chart.detail.components.TransactionsStatisticsCard
import com.davidbugayov.financeanalyzer.presentation.chart.detail.state.FinancialDetailStatisticsContract
import com.davidbugayov.financeanalyzer.presentation.chart.detail.viewmodel.FinancialDetailStatisticsViewModel
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Экран подробной финансовой статистики
 *
 * @param startDate Начало периода
 * @param endDate Конец периода
 * @param onNavigateBack Обработчик нажатия на кнопку "Назад"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialDetailStatisticsScreen(startDate: Long, endDate: Long, onNavigateBack: () -> Unit) {
    val viewModel: FinancialDetailStatisticsViewModel = koinViewModel { parametersOf(startDate, endDate) }
    val state = viewModel.state.collectAsState().value
    val metrics = viewModel.metrics.collectAsState().value
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Запускаем загрузку данных при первом запуске
    LaunchedEffect(Unit) {
        viewModel.handleIntent(FinancialDetailStatisticsContract.Intent.LoadData)
    }

    // Обработка эффектов (например, показ ошибок)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is FinancialDetailStatisticsContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant,
        ),
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.detailed_financial_statistics),
                showBackButton = true,
                onBackClick = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundGradient),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(dimensionResource(R.dimen.financial_statistics_card_padding)),
            ) {
                // Заголовок с периодом
                Text(
                    text = stringResource(R.string.financial_statistics_period),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.financial_statistics_spacer_large),
                    ),
                )
                KeyMetricsCard(
                    income = state.income,
                    expense = state.expense,
                    savingsRate = metrics.savingsRate,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.financial_statistics_spacer_large),
                    ),
                )
                TransactionsStatisticsCard(
                    metrics = metrics,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.financial_statistics_spacer_large),
                    ),
                )
                ExpenseAnalysisCard(
                    metrics = metrics,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.financial_statistics_spacer_large),
                    ),
                )
                RecommendationsCard(
                    metrics = metrics,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
