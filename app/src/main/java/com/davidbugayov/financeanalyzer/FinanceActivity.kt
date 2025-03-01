package com.davidbugayov.financeanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.davidbugayov.financeanalyzer.presentation.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.SharedViewModel
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class FinanceActivity : ComponentActivity() {
    private val viewModel: SharedViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanceAnalyzerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AddTransactionScreen(
                        viewModel = viewModel,
                        onTransactionAdded = {
                            // Можно добавить навигацию к графику после добавления транзакции
                        }
                    )
                }
            }
        }
    }
}