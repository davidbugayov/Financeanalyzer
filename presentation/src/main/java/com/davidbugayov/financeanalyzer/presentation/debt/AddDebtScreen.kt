package com.davidbugayov.financeanalyzer.presentation.debt

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.model.Currency
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.DebtType
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar
import java.util.Calendar
import org.koin.androidx.compose.koinViewModel

/**
 * Форма добавления долга (MVP): заголовок, контрагент, сумма, тип.
 */
@Composable
fun addDebtScreen(
    viewModel: DebtsViewModel = koinViewModel(),
    onSaved: () -> Unit = {},
) {
    val title = remember { mutableStateOf("") }
    val counterparty = remember { mutableStateOf("") }
    val amountStr = remember { mutableStateOf("") }
    val isBorrowed = remember { mutableStateOf(true) }
    val dueAt = remember { mutableStateOf<Long?>(null) }

    val titleError = title.value.isBlank()
    val counterpartyError = counterparty.value.isBlank()
    val amountValue = amountStr.value.replace(',', '.').toDoubleOrNull() ?: 0.0
    val amountError = amountValue <= 0.0
    val isValid = !titleError && !counterpartyError && !amountError

    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = UiR.string.debt_add),
                showBackButton = true,
                onBackClick = onSaved,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = dimensionResource(id = UiR.dimen.space_medium)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = title.value,
                onValueChange = { title.value = it },
                label = { Text(stringResource(id = UiR.string.debt_field_title)) },
                isError = titleError,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = counterparty.value,
                onValueChange = { counterparty.value = it },
                label = { Text(stringResource(id = UiR.string.debt_counterparty)) },
                isError = counterpartyError,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = amountStr.value,
                onValueChange = { amountStr.value = it },
                label = { Text(stringResource(id = UiR.string.debt_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountError,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isBorrowed.value,
                    onClick = { isBorrowed.value = true },
                    label = { Text(text = stringResource(id = UiR.string.debt_type_borrowed)) },
                )
                FilterChip(
                    selected = !isBorrowed.value,
                    onClick = { isBorrowed.value = false },
                    label = { Text(text = stringResource(id = UiR.string.debt_type_lent)) },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text =
                        dueAt.value?.let {
                            java.text.SimpleDateFormat("dd.MM.yyyy").format(java.util.Date(it))
                        } ?: stringResource(id = UiR.string.debt_due_not_set),
                )
                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    val dialog =
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                                dueAt.value = calendar.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                        )
                    dialog.show()
                }) {
                    Text(text = stringResource(id = UiR.string.select_date_button))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.createDebt(
                        title = title.value,
                        counterparty = counterparty.value,
                        type = if (isBorrowed.value) DebtType.BORROWED else DebtType.LENT,
                        amount = Money(amountValue, Currency.RUB),
                        dueAt = dueAt.value,
                        note = null,
                    )
                    onSaved()
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = UiR.string.save))
            }
        }
    }
}
