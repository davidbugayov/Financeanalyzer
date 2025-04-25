package com.davidbugayov.financeanalyzer.presentation.export_import

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.components.ExportButton
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    onNavigateBack: () -> Unit,
    onImportClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Экспорт и импорт данных",
                showBackButton = true,
                onBackClick = onNavigateBack,
                titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Секция импорта
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                    .heightIn(min = 200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.profile_section_padding))
                ) {
                    Text(
                        text = "Экспорт данных",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Экспортируйте все транзакции в CSV файл для использования в Excel или других программах.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ExportButton(
                        onClick = { action ->
                            viewModel.onEvent(ProfileEvent.ExportTransactionsToCSV(action), context)
                        },
                        isExporting = state.isExporting,
                        showFilePath = state.exportedFilePath,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.profile_section_padding))
                    .heightIn(min = 200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.profile_section_padding))
                ) {
                    Text(
                        text = "Импорт транзакций",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                    Text(
                        text = "Импортируйте транзакции из CSV-файлов или банковских выписок. Поддерживаются Сбербанк, Т-Банк, Альфа-Банк и Озон Банк.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    Button(
                        onClick = onImportClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.padding(end = dimensionResource(R.dimen.spacing_small))
                        )
                        Text(
                            text = "Импортировать транзакции",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
} 