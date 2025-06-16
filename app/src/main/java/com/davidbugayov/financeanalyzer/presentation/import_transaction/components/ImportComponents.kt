package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.components.ErrorContent
import com.davidbugayov.financeanalyzer.presentation.import_transaction.model.ImportState

/**
 * Components for import transactions screen
 */

/**
 * Section for displaying import progress
 */
@Composable
fun ImportProgressSection(
    progress: Int,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Импорт транзакций",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress.toFloat() / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, textAlign = TextAlign.Center)
    }
}

/**
 * Section for displaying import results
 */
@Composable
fun ImportResultContent(
    successCount: Int,
    skippedCount: Int,
    successMessage: String,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Импорт завершен",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = successMessage,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text(text = "Закрыть")
            }
        }
    }
}

/**
 * Section for displaying import results from state
 */
@Composable
fun ImportResultsSection(
    state: ImportState,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.isLoading) {
            ImportProgressSection(
                progress = state.progress,
                message = state.progressMessage,
            )
        }

        if (state.successCount > 0) {
            ImportResultContent(
                successCount = state.successCount,
                skippedCount = state.skippedCount,
                successMessage = state.successMessage,
                onDismiss = onDismiss,
            )
        }

        if (state.error != null) {
            ErrorContent(
                error = state.error,
                onRetry = onDismiss,
            )
        }
    }
}
