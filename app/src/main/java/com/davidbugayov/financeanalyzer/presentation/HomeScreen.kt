package com.davidbugayov.financeanalyzer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToChart: () -> Unit,
    onNavigateToAddTransaction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onNavigateToChart) {
            Text("View Financial Charts")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToAddTransaction) {
            Text("Add Transaction")
        }
    }
}