package com.davidbugayov.financeanalyzer.presentation.components.paging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.transactionItem
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem
import timber.log.Timber

@Composable
fun transactionPagingList(
    items: LazyPagingItems<TransactionListItem>,
    categoriesViewModel: CategoriesViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
    listState: LazyListState? = null,
    /**
     * Optional content that will be displayed **before** the list of transactions and will
     * scroll together with the rest of the items. Handy for adding headers like summaries.
     */
    headerContent: (@Composable () -> Unit)? = null,
) {
    val lazyState = listState ?: rememberLazyListState()

    val headerContentKey = stringResource(UiR.string.header_content)
    val appendLoadingKey = stringResource(UiR.string.append_loading)
    val fabSpacerKey = stringResource(UiR.string.fab_spacer)
    val errorLoadingAdditionalData = stringResource(UiR.string.error_loading_additional_data)

    LazyColumn(modifier = Modifier.fillMaxWidth(), state = lazyState) {
        // Optional header
        headerContent?.let { header ->
            item(key = headerContentKey) {
                header()
            }
        }
        items(count = items.itemCount) { index ->
            when (val model = items[index]) {
                is TransactionListItem.Header -> {
                    Text(
                        text = model.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                    )
                }
                is TransactionListItem.Item -> {
                    val tx = model.transaction
                    transactionItem(
                        transaction = tx,
                        categoriesViewModel = categoriesViewModel,
                        onClick = { onTransactionClick(tx) },
                        onTransactionLongClick = { /* long tap убран */ },
                        animationDelay = 0L,
                        animated = false,
                    )
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    )
                }
                null -> Unit
            }
        }
        items.apply {
            when {
                loadState.append is LoadState.Loading -> {
                    item(key = appendLoadingKey) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                loadState.append is LoadState.Error -> {
                    val e = (loadState.append as LoadState.Error).error
                    Timber.e(e, errorLoadingAdditionalData)
                    CrashLoggerProvider.crashLogger.logException(e)
                }
            }
        }
        item(key = fabSpacerKey) { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
