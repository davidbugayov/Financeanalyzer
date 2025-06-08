package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.davidbugayov.financeanalyzer.R

/**
 * Верхняя панель приложения с поддержкой кнопки "Назад" и настраиваемыми действиями.
 *
 * @param title Заголовок панели
 * @param showBackButton Показывать ли кнопку "Назад"
 * @param onBackClick Обработчик нажатия на кнопку "Назад"
 * @param navigationIcon Пользовательская иконка навигации
 * @param actions Дополнительные действия в правой части панели
 * @param titleFontSize Размер шрифта заголовка
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    titleFontSize: Int = dimensionResource(R.dimen.text_size_normal).value.toInt(),
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = titleFontSize.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { onBackClick?.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button),
                    )
                }
            } else {
                navigationIcon?.invoke()
            }
        },
        actions = {
            actions?.invoke()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier,
    )
}
