package com.davidbugayov.financeanalyzer.feature.transaction.base.components
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
import com.davidbugayov.financeanalyzer.ui.theme.SourceItemErrorBackgroundColor
import com.davidbugayov.financeanalyzer.ui.theme.SourceItemErrorContentColor

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
        modifier =
            Modifier
                .width(72.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(vertical = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isError -> SourceItemErrorBackgroundColor
                            isSelected -> Color(source.color)
                            else -> Color(source.color).copy(alpha = 0.8f)
                        },
                    )
                    .border(
                        width =
                            when {
                                isSelected -> 3.dp
                                isError -> 2.dp
                                else -> 0.dp
                            },
                        color =
                            when {
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
                text = source.name.first().toString().uppercase(),
                color = if (isError) SourceItemErrorContentColor else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = source.name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}
