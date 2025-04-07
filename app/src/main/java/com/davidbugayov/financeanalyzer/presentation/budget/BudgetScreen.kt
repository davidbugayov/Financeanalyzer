package com.davidbugayov.financeanalyzer.presentation.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.BudgetCategory
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.CenteredLoadingIndicator
import org.koin.androidx.compose.koinViewModel

@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactions: (String) -> Unit,
    viewModel: BudgetViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.budget),
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier.padding(8.dp)
                    )
                },
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO: Open add category dialog
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_budget_category)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CenteredLoadingIndicator(
                    message = stringResource(R.string.loading_data)
                )
            } else if (state.categories.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.budget_no_categories),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.budget_add_first_category),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // List of budget categories
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(state.categories) { category ->
                        BudgetCategoryItem(
                            category = category,
                            onNavigateToTransactions = onNavigateToTransactions
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetCategoryItem(
    category: BudgetCategory,
    onNavigateToTransactions: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Баланс: ${category.limit} ₽",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (category.spent / category.limit).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (category.spent < category.limit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        // Потратить
                    }
                ) {
                    Text(text = stringResource(R.string.budget_spent))
                }

                TextButton(
                    onClick = {
                        onNavigateToTransactions(category.id)
                    }
                ) {
                    Text(text = stringResource(R.string.budget_transactions))
                }
            }
        }
    }
} 