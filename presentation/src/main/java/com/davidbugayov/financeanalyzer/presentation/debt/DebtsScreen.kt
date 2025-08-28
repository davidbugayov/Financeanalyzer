package com.davidbugayov.financeanalyzer.presentation.debt

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Debt
import com.davidbugayov.financeanalyzer.domain.model.DebtStatus
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import java.util.Locale
import kotlin.math.roundToInt
import org.koin.androidx.compose.koinViewModel

/**
 * Экран списка долгов с пустым состоянием и FAB для добавления долга.
 */
@Composable
fun debtsScreen(
    viewModel: DebtsViewModel = koinViewModel(),
    onAddDebt: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val showRepayForId = remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = true) { onNavigateBack() }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = UiR.string.debt_title),
                showBackButton = true,
                onBackClick = onNavigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDebt) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = UiR.string.debt_add))
            }
        },
    ) { padding ->
        filterRow(
            onAddDebt = onAddDebt,
            onFilterSelected = { /* TODO: connect to VM filter if needed */ },
        )
        when {
            state.isLoading -> loading()
            state.error != null -> errorText(state.error ?: "")
            state.debts.isEmpty() -> emptyState(onAddDebt = onAddDebt)
            else ->
                debtsList(
                    items = state.debts,
                    onRepayClick = { id -> showRepayForId.value = id },
                    contentPadding = padding,
                )
        }

        val repayId = showRepayForId.value
        if (repayId != null) {
            repayDebtDialog(
                debtId = repayId,
                onDismiss = { showRepayForId.value = null },
                onConfirm = { amount ->
                    viewModel.repay(repayId, amount)
                    showRepayForId.value = null
                },
            )
        }
    }
}

@Composable
private fun loading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { CircularProgressIndicator() }
}

@Composable
private fun errorText(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun emptyState(onAddDebt: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(id = UiR.string.debt_empty_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        Text(text = stringResource(id = UiR.string.debt_empty_subtitle), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
        Button(onClick = onAddDebt) { Text(text = stringResource(id = UiR.string.debt_add)) }
    }
}

@Composable
private fun debtsList(
    items: List<Debt>,
    onRepayClick: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(contentPadding = contentPadding) {
        items(items) { debt ->
            debtCard(debt = debt, onRepayClick = onRepayClick)
            Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        }
    }
}

@Composable
private fun debtCard(
    debt: Debt,
    onRepayClick: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(dimensionResource(id = UiR.dimen.card_corner_radius)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = UiR.dimen.card_elevation)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = UiR.dimen.card_content_padding))) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = debt.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                statusChip(debt = debt)
            }
            Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(text = debt.counterparty, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

            val dueText =
                debt.dueAt?.let { millis ->
                    val date = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(java.util.Date(millis))
                    stringResource(id = UiR.string.debt_due_date_short, date)
                } ?: stringResource(id = UiR.string.debt_due_not_set)

            Text(text = stringResource(id = UiR.string.debt_remaining) + ": " + debt.remaining.formatForDisplay())
            Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(text = dueText, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
            debtProgress(debt = debt)

            Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
            Button(onClick = { onRepayClick(debt.id) }) { Text(text = stringResource(id = UiR.string.debt_repay)) }
        }
    }
}

@Composable
private fun statusChip(debt: Debt) {
    val now = System.currentTimeMillis()
    val isOverdueByDate = debt.dueAt?.let { it < now } == true && debt.status == DebtStatus.ACTIVE
    val (labelId, colorId) =
        when {
            debt.status == DebtStatus.PAID -> UiR.string.debt_status_paid to UiR.color.success
            debt.status == DebtStatus.OVERDUE || isOverdueByDate -> UiR.string.debt_status_overdue to UiR.color.error
            debt.status == DebtStatus.WRITEOFF -> UiR.string.debt_status_writeoff to UiR.color.warning
            else -> UiR.string.debt_status_active to UiR.color.info
        }

    val bg = colorResource(id = colorId)
    val text = stringResource(id = labelId)
    val paddingH = dimensionResource(id = UiR.dimen.financial_health_badge_padding_horizontal)
    val paddingV = dimensionResource(id = UiR.dimen.financial_health_badge_padding_vertical)
    val corner = dimensionResource(id = UiR.dimen.financial_health_badge_corner_radius)

    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier =
            Modifier
                .background(color = bg, shape = RoundedCornerShape(corner))
                .padding(horizontal = paddingH, vertical = paddingV),
    )
}

@Composable
private fun debtProgress(debt: Debt) {
    val principal = debt.principal.toMajorDouble()
    val remaining = debt.remaining.toMajorDouble()
    val progress = if (principal > 0.0) (((principal - remaining) / principal).coerceIn(0.0, 1.0)).toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = UiR.dimen.financial_health_progress_height)),
            trackColor = colorResource(id = UiR.color.surface_variant),
        )
        Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
        Text(
            text = stringResource(id = UiR.string.progress_percentage, (progress * 100f).roundToInt()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun filterRow(
    onAddDebt: () -> Unit,
    onFilterSelected: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        Text(text = stringResource(id = UiR.string.debt_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        // Простейший набор кнопок-фильтров (MVP)
        androidx.compose.foundation.layout.Row {
            OutlinedButton(
                onClick = { onFilterSelected("ALL") },
            ) { Text(text = stringResource(id = UiR.string.debt_filter_all)) }
            Spacer(modifier = androidx.compose.ui.Modifier.height(0.dp))
            OutlinedButton(onClick = {
                onFilterSelected("ACTIVE")
            }) { Text(text = stringResource(id = UiR.string.debt_filter_active)) }
            Spacer(modifier = androidx.compose.ui.Modifier.height(0.dp))
            OutlinedButton(onClick = {
                onFilterSelected("OVERDUE")
            }) { Text(text = stringResource(id = UiR.string.debt_filter_overdue)) }
        }
        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
        androidx.compose.foundation.layout.Row {
            OutlinedButton(onClick = {
                onFilterSelected(DebtType.BORROWED.name)
            }) { Text(text = stringResource(id = UiR.string.debt_filter_borrowed)) }
            Spacer(modifier = androidx.compose.ui.Modifier.height(0.dp))
            OutlinedButton(onClick = {
                onFilterSelected(DebtType.LENT.name)
            }) { Text(text = stringResource(id = UiR.string.debt_filter_lent)) }
        }
        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
    }
}
