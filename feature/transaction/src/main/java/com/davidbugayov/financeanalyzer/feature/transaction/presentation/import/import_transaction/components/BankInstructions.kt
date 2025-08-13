package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Инструкции для Сбербанка
 */
@Composable
fun SberbankInstructions() {
    Column {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(UiR.string.sberbank_instructions_title) + "\n\n")
                }
                append(stringResource(UiR.string.sberbank_instructions))
            },
        )

        Text(
            text = stringResource(UiR.string.sberbank_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Инструкции для Тинькофф
 */
@Composable
fun TinkoffInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(UiR.string.tinkoff_instructions_title) + "\n\n")
            }
            append(stringResource(UiR.string.tinkoff_instructions))
        },
    )
}

/**
 * Инструкции для Альфа-Банка
 */
@Composable
fun AlfaBankInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(UiR.string.alfabank_instructions_title_mobile) + "\n\n")
            }
            append(stringResource(UiR.string.alfabank_instructions_mobile) + "\n\n")

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(UiR.string.alfabank_instructions_title_web) + "\n\n")
            }
            append(stringResource(UiR.string.alfabank_instructions_web) + "\n\n")

            withStyle(
                style =
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                append(stringResource(UiR.string.alfabank_note_important) + " ")
            }
            append(stringResource(UiR.string.alfabank_note))
        },
    )
}

/**
 * Инструкции для Озон
 */
@Composable
fun OzonInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(UiR.string.ozon_instructions_title) + "\n\n")
            }
            append(stringResource(UiR.string.ozon_instructions))
        },
    )
}

/**
 * Инструкции по формату CSV
 */
@Composable
fun CSVInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(UiR.string.csv_instructions_title) + "\n\n")
            }

            append(stringResource(UiR.string.csv_format_instructions) + "\n\n")

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(UiR.string.csv_example_title) + "\n\n")
            }
            append(stringResource(UiR.string.csv_example) + "\n\n")

            withStyle(
                style =
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                append(stringResource(UiR.string.csv_note_title) + " ")
            }
            append(stringResource(UiR.string.csv_note))
        },
    )
}
