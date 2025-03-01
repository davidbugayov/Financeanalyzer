package com.example.financeanalyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Finance Analyzer",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Overview") },
                    label = { Text("Overview") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    label = { Text("Add") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("Analytics") }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> OverviewScreen(modifier.padding(paddingValues))
            1 -> AddTransactionScreen(modifier.padding(paddingValues))
            2 -> AnalyticsScreen(modifier.padding(paddingValues))
        }
    }
}

@Composable
fun OverviewScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BalanceCard()
        Spacer(modifier = Modifier.height(16.dp))
        RecentTransactions()
    }
}

@Composable
fun BalanceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total Balance",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Text(
                formatCurrency(5240.50),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BalanceItem("Income", 7500.00, Icons.Default.TrendingUp, Color(0xFF4CAF50))
                BalanceItem("Expenses", 2259.50, Icons.Default.TrendingDown, Color(0xFFE91E63))
            }
        }
    }
}

@Composable
fun BalanceItem(title: String, amount: Double, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = title, tint = color)
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondary
        )
        Text(
            formatCurrency(amount),
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}

@Composable
fun RecentTransactions() {
    Text(
        "Recent Transactions",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    LazyColumn {
        items(getSampleTransactions()) { transaction ->
            TransactionItem(transaction)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    transaction.icon,
                    contentDescription = null,
                    tint = if (transaction.isExpense) Color(0xFFE91E63) else Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        transaction.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        transaction.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                formatCurrency(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = if (transaction.isExpense) Color(0xFFE91E63) else Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun AddTransactionScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Add New Transaction",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        // Add transaction form will be implemented here
    }
}

@Composable
fun AnalyticsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Analytics",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        // Analytics content will be implemented here
    }
}

data class Transaction(
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val icon: ImageVector
)

fun getSampleTransactions(): List<Transaction> = listOf(
    Transaction("Grocery Shopping", 156.50, "Food", true, Icons.Default.ShoppingCart),
    Transaction("Salary", 3000.00, "Income", false, Icons.Default.AttachMoney),
    Transaction("Restaurant", 89.90, "Food", true, Icons.Default.Restaurant),
    Transaction("Freelance Work", 500.00, "Income", false, Icons.Default.Work),
    Transaction("Gas", 45.00, "Transport", true, Icons.Default.LocalGasStation)
)

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
} 