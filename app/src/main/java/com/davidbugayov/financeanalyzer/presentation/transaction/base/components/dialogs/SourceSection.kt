package com.davidbugayov.financeanalyzer.presentation.transaction.base.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Source

/**
 * Секция выбора источника (откуда/куда) с обработчиком долгого нажатия
 */
@Composable
fun SourceSection(
    sources: List<Source>,
    selectedSource: String,
    onSourceSelected: (Source) -> Unit,
    onAddSourceClick: () -> Unit,
    onSourceLongClick: (Source) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Горизонтальный список источников
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // Отображаем доступные источники
            items(sources) { source ->
                SourceItem(
                    source = source,
                    isSelected = source.name == selectedSource,
                    onClick = { onSourceSelected(source) },
                    onLongClick = { onSourceLongClick(source) }
                )
            }
            
            // Добавляем кнопку "Добавить источник"
            item {
                AddSourceButton(onClick = onAddSourceClick)
            }
        }
    }
}

/**
 * Секция выбора источника (откуда/куда) без обработчика долгого нажатия
 */
@Composable
fun SourceSection(
    sources: List<Source>,
    selectedSource: String,
    onSourceSelected: (Source) -> Unit,
    onAddSourceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Горизонтальный список источников
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // Отображаем доступные источники
            items(sources) { source ->
                SourceItem(
                    source = source,
                    isSelected = source.name == selectedSource,
                    onClick = { onSourceSelected(source) },
                    onLongClick = { }
                )
            }
            
            // Добавляем кнопку "Добавить источник"
            item {
                AddSourceButton(onClick = onAddSourceClick)
            }
        }
    }
}

/**
 * Элемент списка источников с обработчиком долгого нажатия
 */
@Composable
private fun SourceItem(
    source: Source,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // Получаем цвет источника из его атрибутов
    val sourceColor = Color(source.color)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(65.dp)
            .clickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        // Отображаем цветной круг с первой буквой источника
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(sourceColor)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Первая буква названия источника
            Text(
                text = source.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Название источника
        Text(
            text = source.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black
        )
    }
}

/**
 * Элемент списка источников без обработчика долгого нажатия
 */
@Composable
private fun SourceItem(
    source: Source,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Получаем цвет источника из его атрибутов
    val sourceColor = Color(source.color)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(65.dp)
            .clickable(onClick = onClick)
    ) {
        // Отображаем цветной круг с первой буквой источника
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(sourceColor)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Первая буква названия источника
            Text(
                text = source.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Название источника
        Text(
            text = source.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black
        )
    }
}

/**
 * Кнопка добавления источника
 */
@Composable
private fun AddSourceButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(65.dp)
            .clickable(onClick = onClick)
    ) {
        // Серый круг с иконкой плюса
        Surface(
            shape = CircleShape,
            color = Color(0xFFEEEEEE), // Светло-серый
            contentColor = Color.Gray,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить источник",
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Добавить",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black
        )
    }
} 