package com.davidbugayov.financeanalyzer.presentation.history.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem

/**
 * Компонент для отображения транзакции с поддержкой долгого нажатия.
 *
 * @param transaction Транзакция для отображения
 * @param onClick Callback, вызываемый при нажатии на транзакцию
 * @param onLongClick Callback, вызываемый при долгом нажатии на транзакцию
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionLongPressItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit,
    onLongClick: (Transaction) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(transaction) },
                onLongClick = { onLongClick(transaction) }
            )
    ) {
        TransactionItem(transaction = transaction)
    }
} 