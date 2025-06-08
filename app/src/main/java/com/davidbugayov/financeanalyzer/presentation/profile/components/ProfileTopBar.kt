package com.davidbugayov.financeanalyzer.presentation.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.components.AppTopBar

/**
 * Верхняя панель для экрана профиля.
 *
 * @param onNavigateBack Обработчик нажатия кнопки "Назад"
 */
@Composable
fun ProfileTopBar(onNavigateBack: () -> Unit) {
    AppTopBar(
        title = stringResource(R.string.profile_title),
        showBackButton = true,
        onBackClick = onNavigateBack,
        titleFontSize = dimensionResource(R.dimen.text_size_normal).value.toInt()
    )
} 
