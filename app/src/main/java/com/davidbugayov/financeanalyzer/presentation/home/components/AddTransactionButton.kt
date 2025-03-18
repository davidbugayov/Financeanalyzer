// Этот файл будет содержать компонент кнопки добавления транзакции
// с использованием новых цветов
package com.davidbugayov.financeanalyzer.presentation.home.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.ui.theme.LocalFabColor

/**
 * Компонент кнопки добавления новой транзакции.
 * 
 * @param onClick Обработчик нажатия на кнопку
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun AddTransactionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fabColor = LocalFabColor.current
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = fabColor,
        contentColor = androidx.compose.ui.graphics.Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_transaction),
            modifier = Modifier.size(24.dp)
        )
    }
} 