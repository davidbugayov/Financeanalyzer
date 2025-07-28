package com.davidbugayov.financeanalyzer.feature.profile.components

import androidx.compose.runtime.Composable
import com.davidbugayov.financeanalyzer.feature.profile.util.StringProvider
import com.davidbugayov.financeanalyzer.ui.components.AppTopBar

/**
 * Верхняя панель для экрана профиля.
 *
 * @param onNavigateBack Обработчик нажатия кнопки "Назад"
 */
@Composable
fun ProfileTopBar(onNavigateBack: () -> Unit) {
    AppTopBar(
        title = StringProvider.profileTitle,
        showBackButton = true,
        onBackClick = onNavigateBack,
    )
}
