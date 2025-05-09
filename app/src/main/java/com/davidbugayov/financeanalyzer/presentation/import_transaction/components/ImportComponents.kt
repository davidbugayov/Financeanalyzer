package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportTransactionsState

/**
 * Компонент с инструкциями по импорту
 */
@Composable
fun ImportInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = dimensionResource(R.dimen.padding_large))
                    .size(dimensionResource(R.dimen.icon_size))
            )

            Column {
                Text(
                    text = stringResource(R.string.how_to_import),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                Text(
                    text = stringResource(R.string.import_instructions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Отображение списка поддерживаемых банков
 */
@Composable
fun BanksList(onBankClick: (String) -> Unit = {}) {
    // Получаем строковые ресурсы заранее
    val sberbankName = stringResource(R.string.bank_sberbank)
    val tinkoffName = stringResource(R.string.bank_tinkoff)
    val alfabankName = stringResource(R.string.bank_alfabank)
    val ozonName = stringResource(R.string.bank_ozon)
    val csvName = stringResource(R.string.bank_csv)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_small))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_small))
        ) {
            BankImportCard(
                bankName = sberbankName,
                tintColor = colorResource(id = R.color.bank_sberbank),
                modifier = Modifier.weight(1f),
                onClick = { onBankClick(sberbankName) }
            )

            BankImportCard(
                bankName = tinkoffName,
                tintColor = colorResource(id = R.color.bank_tinkoff),
                modifier = Modifier.weight(1f),
                onClick = { onBankClick(tinkoffName) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_small))
        ) {
            BankImportCard(
                bankName = alfabankName,
                tintColor = colorResource(id = R.color.bank_alfabank),
                modifier = Modifier.weight(1f),
                onClick = { onBankClick(alfabankName) }
            )

            BankImportCard(
                bankName = ozonName,
                tintColor = colorResource(id = R.color.bank_ozon),
                modifier = Modifier.weight(1f),
                onClick = { onBankClick(ozonName) }
            )
        }

        // Инструкция по импорту CSV файлов
        CSVImportCard(onLearnMoreClick = { onBankClick(csvName) })
    }
}

/**
 * Карточка с информацией о CSV-импорте
 */
@Composable
fun CSVImportCard(onLearnMoreClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large))
        ) {
            Text(
                text = stringResource(R.string.import_csv_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

            Text(
                text = stringResource(R.string.csv_requirements_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            Text(
                text = stringResource(R.string.csv_requirements),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

            TextButton(
                onClick = onLearnMoreClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.more_details))
            }
        }
    }
}

/**
 * Отображение результатов импорта
 */
@Composable
fun ImportResultsSection(state: ImportTransactionsState) {
    if (state.isLoading || state.isImportCompleted) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    state.isLoading && state.progress > 0 -> {
                        Text(
                            text = stringResource(R.string.import_in_progress),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))

                        LinearProgressIndicator(
                            progress = { state.progress.toFloat() / state.totalCount },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                        Text(
                            text = stringResource(
                                R.string.import_progress_count,
                                state.progress,
                                state.totalCount
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                        Text(
                            text = state.currentStep,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    state.isLoading -> {
                        Text(
                            text = stringResource(R.string.import_in_progress),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))

                        CircularProgressIndicator()
                    }

                    state.error == null -> {
                        Text(
                            text = stringResource(R.string.import_success),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_medium)))

                        Text(
                            text = stringResource(R.string.imported_count, state.successCount),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (state.skippedCount > 0) {
                            Text(
                                text = stringResource(R.string.skipped_count, state.skippedCount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                        Text(
                            text = stringResource(
                                R.string.import_total_amount,
                                state.totalAmount.amount.toInt()
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    else -> {
                        Text(
                            text = stringResource(R.string.import_error),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.empty_state_height)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.select_file_prompt),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Диалог с инструкциями по получению выписки из банка
 */
@Composable
fun BankInstructionDialog(
    bankName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = true
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(dimensionResource(R.dimen.spacing_small)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal))
                ) {
                    Text(
                        text = stringResource(R.string.bank_instructions_title, bankName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    
                    // Содержимое инструкции
                    BankInstructionsContent(bankName)
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    
                    // Кнопка закрытия
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.got_it))
                    }
                }
            }
        }
    }
}

/**
 * Содержимое инструкции для конкретного банка
 */
@Composable
fun BankInstructionsContent(bankName: String) {
    Column {
        when (bankName) {
            stringResource(R.string.bank_sberbank) -> SberbankInstructions()
            stringResource(R.string.bank_tinkoff) -> TinkoffInstructions()
            stringResource(R.string.bank_alfabank) -> AlfaBankInstructions()
            stringResource(R.string.bank_ozon) -> OzonInstructions()
            stringResource(R.string.bank_csv) -> CSVInstructions()
            else -> Text(text = stringResource(R.string.unavailable_instructions))
        }
    }
}

/**
 * Диалог запроса разрешений
 */
@Composable
fun PermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = true
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(dimensionResource(R.dimen.spacing_small)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal))
                ) {
                    Text(
                        text = stringResource(R.string.import_permission_required),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    
                    Text(
                        text =  stringResource(R.string.permission_message_android15),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                    
                    // Кнопки действий
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                        
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                        
                        Button(onClick = onOpenSettings) {
                            Text(stringResource(R.string.open_settings))
                        }
                    }
                }
            }
        }
    }
} 