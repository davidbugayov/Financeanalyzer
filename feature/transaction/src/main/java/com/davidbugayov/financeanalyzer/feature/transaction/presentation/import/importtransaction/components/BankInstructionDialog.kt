package com.davidbugayov.financeanalyzer.presentation.importtransaction.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.davidbugayov.financeanalyzer.presentation.importtransaction.components.alfaBankInstructions
import com.davidbugayov.financeanalyzer.presentation.importtransaction.components.csvInstructions
import com.davidbugayov.financeanalyzer.presentation.importtransaction.components.ozonInstructions
import com.davidbugayov.financeanalyzer.presentation.importtransaction.components.sberbankInstructions
import com.davidbugayov.financeanalyzer.presentation.importtransaction.components.tinkoffInstructions
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Диалог с инструкциями по импорту для выбранного банка
 */
@Composable
fun bankInstructionDialog(
    bankName: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(UiR.dimen.space_medium)),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = dimensionResource(UiR.dimen.dialog_elevation),
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(UiR.dimen.import_dialog_content_padding)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(UiR.string.bank_instructions_title, bankName),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(dimensionResource(UiR.dimen.space_medium)))

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                ) {
                    bankInstructionsContent(bankName = bankName)
                }

                Spacer(modifier = Modifier.height(dimensionResource(UiR.dimen.space_medium)))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(UiR.string.got_it))
                }
            }
        }
    }
}

/**
 * Содержимое инструкций для выбранного банка
 */
@Composable
fun bankInstructionsContent(bankName: String) {
    when (bankName) {
        stringResource(UiR.string.bank_sberbank) -> sberbankInstructions()
        stringResource(UiR.string.bank_tinkoff) -> tinkoffInstructions()
        stringResource(UiR.string.bank_alfabank) -> alfaBankInstructions()
        stringResource(UiR.string.bank_ozon) -> ozonInstructions()
        stringResource(UiR.string.bank_csv) -> csvInstructions()
        else -> Text(text = stringResource(UiR.string.unavailable_instructions))
    }
}
