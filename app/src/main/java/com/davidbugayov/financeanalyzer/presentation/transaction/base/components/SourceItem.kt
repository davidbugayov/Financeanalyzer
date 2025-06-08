package com.davidbugayov.financeanalyzer.presentation.transaction.base.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.ui.theme.SourceItemBorderWidth
import com.davidbugayov.financeanalyzer.ui.theme.SourceItemErrorBackgroundColor
import com.davidbugayov.financeanalyzer.ui.theme.SourceItemErrorContentColor
import com.davidbugayov.financeanalyzer.ui.theme.SourceItemNoBorderWidth

/**
 * Элемент источника средств
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SourceItem(
    source: Source,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isError: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isError) {
                        SourceItemErrorBackgroundColor
                    } else {
                        Color(source.color)
                    },
                )
                .border(
                    width = when {
                        isSelected -> SourceItemBorderWidth
                        isError -> SourceItemBorderWidth
                        else -> SourceItemNoBorderWidth
                    },
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isError -> MaterialTheme.colorScheme.error
                        else -> Color.Transparent
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Здесь можно добавить иконку для источника
            Text(
                text = source.name.first().toString(),
                color = if (isError) SourceItemErrorContentColor else Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = source.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (isError) Color.Red else MaterialTheme.colorScheme.onSurface,
        )
    }
}
