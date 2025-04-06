package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import timber.log.Timber
import com.davidbugayov.financeanalyzer.ui.theme.WarningColor

/**
 * Компонент для отображения логов в диалоговом окне.
 * 
 * @param logs Список логов для отображения
 * @param onDismiss Колбэк для закрытия диалога
 * @param onRefresh Колбэк для обновления логов (опционально)
 * @param onShare Колбэк для отправки логов (опционально)
 */
@Composable
fun LogDialog(
    logs: List<String>,
    onDismiss: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Автоматически прокручиваем к последней записи при открытии
    LaunchedEffect(logs) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.scrollToItem(logs.size - 1)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Логи приложения",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                
                // Кнопка обновления, если предоставлен колбэк
                if (onRefresh != null) {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить логи"
                        )
                    }
                }
                
                // Кнопка отправки, если предоставлен колбэк
                if (onShare != null) {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Отправить логи"
                        )
                    }
                }
                
                // Кнопка закрытия
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть"
                    )
                }
            }
        },
        text = {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Логи отсутствуют",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    state = listState
                ) {
                    items(logs) { log ->
                        LogItem(log = log)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

/**
 * Компонент для отображения одной записи лога.
 */
@Composable
private fun LogItem(log: String) {
    // Определяем цвет в зависимости от типа лога
    val logColor = when {
        log.contains(" E/") || log.contains("[E]") -> Color.Red.copy(alpha = 0.8f)
        log.contains(" W/") || log.contains("[W]") -> WarningColor // Amber
        log.contains(" D/") || log.contains("[D]") -> Color.Blue.copy(alpha = 0.7f)
        log.contains(" I/") || log.contains("[I]") -> Color.Green.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 4.dp)
    ) {
        Text(
            text = log,
            color = logColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            overflow = TextOverflow.Ellipsis,
            maxLines = 10
        )
    }
} 