package com.davidbugayov.financeanalyzer.feature.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.feature.profile.R
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar

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
    )
}
