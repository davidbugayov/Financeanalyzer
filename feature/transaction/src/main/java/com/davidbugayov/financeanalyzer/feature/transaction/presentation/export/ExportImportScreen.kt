package com.davidbugayov.financeanalyzer.feature.transaction.presentation.export

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.davidbugayov.financeanalyzer.ui.R as UiR
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.davidbugayov.financeanalyzer.shared.model.ExportAction

/**
 * Экран экспорта/импорта транзакций.
 * Позволяет пользователю экспортировать транзакции в CSV-файл или импортировать их из различных источников.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun exportImportScreen(
    onNavigateBack: () -> Unit,
    onImportClick: () -> Unit,
    viewModel: ExportImportViewModel = koinViewModel(),
) {
    val isExporting by viewModel.isExporting.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val exportError by viewModel.exportError.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Обработка результатов экспорта
    LaunchedEffect(exportResult) {
        exportResult?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearExportMessages()
            }
        }
    }

    LaunchedEffect(exportError) {
        exportError?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearExportMessages()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(UiR.string.export_import_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(UiR.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Export Card
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(10.dp, shape = RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 14.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = stringResource(UiR.string.export_section_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = stringResource(UiR.string.export_section_description_friendly),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = { showExportDialog = true },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                            enabled = !isExporting,
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(stringResource(UiR.string.export_button_friendly))
                        }
                    }
                }

                // Import Card
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(10.dp, shape = RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 14.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text =
                                    stringResource(
                                        UiR.string.import_transactions_title,
                                    ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = stringResource(UiR.string.import_section_description_friendly),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = onImportClick,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                            enabled = !isExporting,
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(stringResource(UiR.string.import_button_friendly))
                        }
                    }
                }
            }

            // Export Action Dialog
            if (showExportDialog) {
                modernCsvExportDialog(
                    onShare = { viewModel.exportTransactions(ExportAction.SHARE) },
                    onOpen = { viewModel.exportTransactions(ExportAction.OPEN) },
                    onSave = { viewModel.exportTransactions(ExportAction.SAVE) },
                    onDismiss = { showExportDialog = false },
                )
            }
        }
    }
}

@Composable
fun modernCsvExportDialog(
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
            ) {
                // Заголовок
                Text(
                    text = stringResource(UiR.string.export_button_friendly),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp),
                )

                // Кнопка "Поделиться"
                actionCard(
                    icon = Icons.Default.Share,
                    title = stringResource(UiR.string.share),
                    description = stringResource(UiR.string.share_description),
                    color = MaterialTheme.colorScheme.primary,
                    onClick = {
                        onShare()
                        onDismiss()
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка "Открыть"
                actionCard(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    title = stringResource(UiR.string.open_file),
                    description = stringResource(UiR.string.open_file_description),
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        onOpen()
                        onDismiss()
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка "Сохранить"
                actionCard(
                    icon = Icons.Default.Download,
                    title = stringResource(UiR.string.save),
                    description = stringResource(UiR.string.save_without_actions),
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = {
                        onSave()
                        onDismiss()
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопка отмены
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun actionCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f),
            ),
        border =
            BorderStroke(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Иконка в цветном круге
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(
                            color = color.copy(alpha = 0.15f),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            // Стрелка справа
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
