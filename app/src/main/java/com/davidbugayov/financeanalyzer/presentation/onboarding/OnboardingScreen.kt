package com.davidbugayov.financeanalyzer.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Экран онбординга, показывающий основные возможности приложения.
 * Отображается только при первом запуске приложения.
 *
 * @param onFinish Колбэк, вызываемый при завершении онбординга
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    
    // Список страниц онбординга с их содержимым
    val pages = listOf(
        OnboardingPage(
            title = "Управление финансами",
            description = "Легко отслеживайте доходы и расходы, добавляя транзакции в удобном интерфейсе",
            icon = Icons.Default.AccountBalance
        ),
        OnboardingPage(
            title = "Аналитика и графики",
            description = "Анализируйте свои финансы с помощью наглядных графиков и диаграмм",
            icon = Icons.Default.ShowChart
        ),
        OnboardingPage(
            title = "Импорт транзакций",
            description = "Импортируйте транзакции из различных банков и источников",
            icon = Icons.Default.FileDownload
        ),
        OnboardingPage(
            title = "Категории",
            description = "Создавайте, редактируйте и удаляйте категории для лучшей организации финансов",
            icon = Icons.Default.Category
        ),
        OnboardingPage(
            title = "Уведомления",
            description = "Получайте уведомления для регулярного ввода транзакций и контроля бюджета",
            icon = Icons.Default.Notifications
        )
    )
    
    // Автоматическое переключение страниц каждые 4 секунды, если не на последней странице
    LaunchedEffect(currentPage) {
        if (currentPage < pages.size - 1) {
            delay(4000)
            currentPage++
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Контент страницы
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                ) {
                    pages.forEachIndexed { index, page ->
                        if (currentPage == index) {
                            OnboardingPageContent(page)
                        }
                    }
                }
            }
            
            // Навигация и индикатор страниц
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Индикаторы страниц
                Row(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    pages.forEachIndexed { index, _ ->
                        val width by animateDpAsState(
                            targetValue = if (currentPage == index) 24.dp else 10.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "indicator"
                        )
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(width)
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentPage == index) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Кнопки навигации
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        TextButton(
                            onClick = { currentPage-- }
                        ) {
                            Text("Назад")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp))
                    }
                    
                    if (currentPage < pages.size - 1) {
                        Button(
                            onClick = { currentPage++ }
                        ) {
                            Text("Далее")
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Начать")
                        }
                    }
                }
                
                // Кнопка пропустить
                if (currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            "Пропустить",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Компонент для отображения содержимого одной страницы онбординга.
 */
@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = page.title,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Модель данных для страницы онбординга.
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
) 