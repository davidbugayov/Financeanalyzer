package com.davidbugayov.financeanalyzer.presentation.export_import

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.usecase.ExportTransactionsToCSVUseCase.ExportAction
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackMessage
import com.davidbugayov.financeanalyzer.presentation.components.FeedbackType
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.components.ActionButton
import com.davidbugayov.financeanalyzer.presentation.profile.event.ProfileEvent
import com.davidbugayov.financeanalyzer.ui.theme.LocalFriendlyCardBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    onNavigateBack: () -> Unit,
    onImportClick: () -> Unit,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val showFeedbackState = remember { mutableStateOf(false) }
    val feedbackMessageState = remember { mutableStateOf("") }
    val feedbackTypeState = remember { mutableStateOf(FeedbackType.INFO) }
    val showFeedback by showFeedbackState
    val feedbackMessage by feedbackMessageState
    val feedbackType by feedbackTypeState
    val feedbackTitleState = remember { mutableStateOf("") }
    val feedbackTitle by feedbackTitleState
    val showExportDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.intentCommands.collect { intent ->
            try {
                context.startActivity(intent)
            } catch (_: android.content.ActivityNotFoundException) {
                feedbackMessageState.value = context.getString(R.string.error_no_csv_app)
                feedbackTypeState.value = FeedbackType.ERROR
                showFeedbackState.value = true
            } catch (e: Exception) {
                feedbackMessageState.value = context.getString(R.string.error_action_failed, e.localizedMessage ?: "")
                feedbackTypeState.value = FeedbackType.ERROR
                showFeedbackState.value = true
            }
        }
    }
    LaunchedEffect(state.exportSuccess) {
        state.exportSuccess?.let {
            feedbackTitleState.value = context.getString(R.string.export_success_title)
            feedbackMessageState.value = it
            feedbackTypeState.value = FeedbackType.SUCCESS
            showFeedbackState.value = true
        }
    }
    LaunchedEffect(state.exportError) {
        state.exportError?.let {
            feedbackTitleState.value = context.getString(R.string.export_error_title)
            feedbackMessageState.value = it
            feedbackTypeState.value = FeedbackType.ERROR
            showFeedbackState.value = true
        }
    }
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.export_import_screen_title),
                showBackButton = true,
                onBackClick = onNavigateBack,
                titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(10.dp, shape = RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LocalFriendlyCardBackgroundColor.current
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.export_section_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = stringResource(R.string.export_section_description_friendly),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ActionButton(
                            text = stringResource(R.string.export_button_friendly),
                            icon = Icons.Default.FileDownload,
                            isLoading = state.isExporting,
                            onClick = { showExportDialog.value = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                        ExportActionDialog(
                            visible = showExportDialog.value,
                            onDismiss = { showExportDialog.value = false },
                            onShare = {
                                viewModel.onEvent(ProfileEvent.ExportTransactionsToCSV(ExportAction.SHARE))
                                showExportDialog.value = false
                            },
                            onOpen = {
                                viewModel.onEvent(ProfileEvent.ExportTransactionsToCSV(ExportAction.OPEN))
                                showExportDialog.value = false
                            },
                            onSave = {
                                viewModel.onEvent(ProfileEvent.ExportTransactionsToCSV(ExportAction.SAVE_ONLY))
                                showExportDialog.value = false
                            }
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(10.dp, shape = RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LocalFriendlyCardBackgroundColor.current
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.import_section_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = stringResource(R.string.import_section_description_friendly),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ActionButton(
                            text = stringResource(R.string.import_button_friendly),
                            icon = Icons.Default.FileUpload,
                            isLoading = state.isExporting,
                            onClick = onImportClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }
                }
            }
            FeedbackMessage(
                title = feedbackTitle,
                message = feedbackMessage,
                type = feedbackType,
                visible = showFeedback,
                onDismiss = {
                    showFeedbackState.value = false
                    viewModel.onEvent(ProfileEvent.ResetExportState)
                },
                modifier = Modifier.align(Alignment.Center),
                isFilePath = feedbackType == FeedbackType.SUCCESS
            )
        }
    }
}

@Composable
fun ExportActionDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onSave: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .height(40.dp)
                    .width(40.dp)
            )
        },
        title = {
            Text(
                stringResource(R.string.export_choose_action),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.export_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    Button(
                        onClick = onShare,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            stringResource(R.string.export_share),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    FilledTonalButton(
                        onClick = onOpen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            stringResource(R.string.export_open),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                AnimatedVisibility(visible = true, enter = fadeIn()) {
                    OutlinedButton(
                        onClick = onSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.export_only_save),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
} 