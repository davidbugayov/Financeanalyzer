package com.davidbugayov.financeanalyzer.presentation.add.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.add.model.CategoryItem

/**
 * Элемент категории
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isError: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = if (isSelected) 3.dp else if (isError) 2.dp else 0.dp,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isError -> MaterialTheme.colorScheme.error
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
} 