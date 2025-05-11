package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.domain.model.Source
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SourcePickerDialog(
    sources: List<Source>,
    onSourceSelected: (Source) -> Unit,
    onDismiss: () -> Unit,
    onAddCustomSource: () -> Unit,
    onDeleteSource: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_source)) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column {
                LazyColumn {
                    items(sources) { source ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onSourceSelected(source) },
                                    onLongClick = {
                                        Timber.d("Long press on source: ${source.name}")
                                        onDeleteSource(source.name)
                                    }
                                )
                                .padding(vertical = dimensionResource(R.dimen.spacing_medium)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = dimensionResource(R.dimen.spacing_medium))
                                    .size(dimensionResource(R.dimen.icon_size_small))
                                    .clip(CircleShape)
                                    .background(Color(source.color))
                            )
                            Text(
                                text = source.name,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_source),
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(18.dp)
                                    .clickable { onDeleteSource(source.name) }
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddCustomSource() }
                                .padding(vertical = dimensionResource(R.dimen.spacing_medium)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_custom_source),
                                modifier = Modifier
                                    .padding(end = dimensionResource(R.dimen.spacing_medium))
                                    .size(dimensionResource(R.dimen.icon_size_small))
                            )
                            Text(stringResource(R.string.add_custom_source))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 