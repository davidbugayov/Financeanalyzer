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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.R
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.presentation.components.TransactionItem
import com.davidbugayov.financeanalyzer.ui.paging.TransactionListItem
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

@Composable
fun TransactionPagingList(
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

    LazyColumn(modifier = Modifier.fillMaxWidth(), state = lazyState) {
        // Optional header
        headerContent?.let { header ->
            item(key = "header_content") {
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
                                .padding(vertical = dimensionResource(id = R.dimen.spacing_small)),
                    )
                }
                is TransactionListItem.Item -> {
                    val tx = model.transaction
                    TransactionItem(
                        transaction = tx,
                        categoriesViewModel = categoriesViewModel,
                        onClick = { onTransactionClick(tx) },
                        onTransactionLongClick = { onTransactionLongClick(tx) },
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
                    item("append_loading") {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(id = R.dimen.spacing_large)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                loadState.append is LoadState.Error -> {
                    val e = (loadState.append as LoadState.Error).error
                    Timber.e(e, "Paging append error")
                    CrashLoggerProvider.crashLogger.logException(e)
                }
            }
        }
        item("fab_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
