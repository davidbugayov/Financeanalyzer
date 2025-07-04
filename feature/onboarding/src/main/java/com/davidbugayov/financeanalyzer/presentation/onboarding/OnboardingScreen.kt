@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.davidbugayov.financeanalyzer.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.feature.onboarding.R
import kotlinx.coroutines.launch

/**
 * Современный экран онбординга с интерактивными элементами и прогрессивной демонстрацией функций.
 * Следует современным трендам UX дизайна и отражает функциональность приложения.
 *
 * @param onFinish Колбэк, вызываемый при завершении онбординга
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = createOnboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    configuration.screenHeightDp.dp

    // Состояние для анимаций
    var isVisible by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = (pagerState.currentPage + 1) / pages.size.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = EaseInOut),
        label = "progress",
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Современный хедер с прогрессом
            ModernHeader(
                progress = progress,
                currentPage = pagerState.currentPage + 1,
                totalPages = pages.size,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.onboarding_header_padding_horizontal),
                    vertical = dimensionResource(R.dimen.onboarding_header_padding_vertical),
                ),
            )

            // Контент с пейджером
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { position ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(
                            animationSpec = tween(600),
                            initialOffsetY = { it / 2 },
                        ),
                    exit = fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(animationSpec = tween(300)),
                ) {
                    when (position) {
                        0 -> WelcomePage()
                        1 -> FeaturePage(pages[position])
                        2 -> InteractiveAnalyticsPage()
                        3 -> SecurityPrivacyPage()
                        4 -> GetStartedPage()
                        else -> FeaturePage(pages[position])
                    }
                }
            }

            // Современная навигация
            ModernNavigation(
                pagerState = pagerState,
                onNext = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pages.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onFinish()
                        }
                    }
                },
                onPrevious = {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onSkip = onFinish,
                modifier = Modifier.padding(dimensionResource(R.dimen.onboarding_page_padding)),
            )
        }
    }
}

/**
 * Создает список страниц онбординга с улучшенным содержанием
 */
@Composable
private fun createOnboardingPages(): List<OnboardingPage> = listOf(
    OnboardingPage(
        title = stringResource(R.string.onboarding_welcome_title),
        description = stringResource(R.string.onboarding_welcome_description),
        icon = Icons.Default.Smartphone,
        features = listOf(
            stringResource(R.string.onboarding_welcome_feature_1),
            stringResource(R.string.onboarding_welcome_feature_2),
            stringResource(R.string.onboarding_welcome_feature_3),
        ),
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_transactions_title),
        description = stringResource(R.string.onboarding_transactions_description),
        icon = Icons.Default.MonetizationOn,
        features = listOf(
            stringResource(R.string.onboarding_transactions_feature_1),
            stringResource(R.string.onboarding_transactions_feature_2),
            stringResource(R.string.onboarding_transactions_feature_3),
        ),
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_analytics_title),
        description = stringResource(R.string.onboarding_analytics_description),
        icon = Icons.Default.Analytics,
        features = listOf(
            stringResource(R.string.onboarding_analytics_feature_1),
            stringResource(R.string.onboarding_analytics_feature_2),
            stringResource(R.string.onboarding_analytics_feature_3),
        ),
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_security_title),
        description = stringResource(R.string.onboarding_security_description),
        icon = Icons.Default.Security,
        features = listOf(
            stringResource(R.string.onboarding_security_feature_1),
            stringResource(R.string.onboarding_security_feature_2),
            stringResource(R.string.onboarding_security_feature_3),
        ),
    ),
    OnboardingPage(
        title = stringResource(R.string.onboarding_ready_title),
        description = stringResource(R.string.onboarding_ready_description),
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        features = listOf(
            stringResource(R.string.onboarding_ready_feature_1),
            stringResource(R.string.onboarding_ready_feature_2),
            stringResource(R.string.onboarding_ready_feature_3),
        ),
    ),
)

/**
 * Современный хедер с линейным прогрессом
 */
@Composable
private fun ModernHeader(
    progress: Float,
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$currentPage/$totalPages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.onboarding_spacing_medium)))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.onboarding_progress_height))
                .clip(RoundedCornerShape(dimensionResource(R.dimen.onboarding_progress_corner_radius))),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round,
        )
    }
}

/**
 * Страница приветствия с анимированными элементами
 */
@Composable
private fun WelcomePage() {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(800, easing = EaseInOut))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.onboarding_welcome_icon_container))
                .scale(scale.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.onboarding_icon_size)),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
        )
    }
}

/**
 * Интерактивная страница аналитики с современным минималистичным дизайном
 */
@Composable
private fun InteractiveAnalyticsPage() {
    var selectedChart by remember { mutableStateOf(0) }
    val chartOptions = listOf(
        Triple(
            Icons.Default.PieChart,
            stringResource(R.string.onboarding_chart_categories),
            stringResource(R.string.onboarding_chart_categories_desc),
        ),
        Triple(
            Icons.Default.Timeline,
            stringResource(R.string.onboarding_chart_trends),
            stringResource(R.string.onboarding_chart_trends_desc),
        ),
        Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            stringResource(R.string.onboarding_chart_growth),
            stringResource(R.string.onboarding_chart_growth_desc),
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Новый короткий заголовок
        Text(
            text = "Анализируйте свои финансы",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))
        // Краткое описание
        Text(
            text = "Следите за расходами, доходами и ростом баланса с помощью наглядных графиков.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(28.dp))
        // Горизонтальный свайпер для выбора графика
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(chartOptions.size) { index ->
                ChartOptionCard(
                    icon = chartOptions[index].first,
                    title = chartOptions[index].second,
                    isSelected = selectedChart == index,
                    onClick = { selectedChart = index },
                    modifier = Modifier
                        .width(110.dp)
                        .height(80.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // Крупная анимированная иллюстрация выбранного графика
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = tween(400)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
        ) {
            DemoChart(selectedChart)
        }
        Spacer(modifier = Modifier.height(18.dp))
        // Краткая подпись под графиком
        Text(
            text = chartOptions[selectedChart].third,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Карточка опции графика
 */
@Composable
private fun ChartOptionCard(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Демонстрационный график
 */
@Composable
private fun DemoChart(chartType: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when (chartType) {
                0 -> PieChartDemo()
                1 -> LineChartDemo()
                2 -> BarChartDemo()
            }
        }
    }
}

/**
 * Демонстрация круговой диаграммы
 */
@Composable
private fun PieChartDemo() {
    Canvas(modifier = Modifier.size(120.dp)) {
        val colors = listOf(
            Color(0xFF6750A4),
            Color(0xFF7C4DFF),
            Color(0xFF3F51B5),
            Color(0xFF2196F3),
        )
        val angles = listOf(90f, 120f, 80f, 70f)
        var startAngle = 0f

        angles.forEachIndexed { index, angle ->
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = angle,
                useCenter = true,
                style = Stroke(width = 20.dp.toPx()),
            )
            startAngle += angle
        }
    }
}

/**
 * Демонстрация линейного графика
 */
@Composable
private fun LineChartDemo() {
    Canvas(modifier = Modifier.size(120.dp, 80.dp)) {
        val points = listOf(
            Offset(20f, 60f),
            Offset(40f, 30f),
            Offset(60f, 45f),
            Offset(80f, 20f),
            Offset(100f, 35f),
        )

        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF6750A4),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        points.forEach { point ->
            drawCircle(
                color = Color(0xFF6750A4),
                radius = 6.dp.toPx(),
                center = point,
            )
        }
    }
}

/**
 * Демонстрация столбчатой диаграммы
 */
@Composable
private fun BarChartDemo() {
    Row(
        modifier = Modifier.height(80.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val heights = listOf(40.dp, 60.dp, 30.dp, 50.dp, 70.dp)
        val color = Color(0xFF6750A4)

        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(height)
                    .background(color, RoundedCornerShape(2.dp)),
            )
        }
    }
}

/**
 * Страница безопасности и приватности
 */
@Composable
private fun SecurityPrivacyPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_security_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_security_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        SecurityFeatureList()
    }
}

/**
 * Список функций безопасности
 */
@Composable
private fun SecurityFeatureList() {
    val features = listOf(
        Triple(
            Icons.Default.Smartphone,
            stringResource(R.string.onboarding_security_offline),
            stringResource(R.string.onboarding_security_offline_desc),
        ),
        Triple(
            Icons.Default.Security,
            stringResource(R.string.onboarding_security_data),
            stringResource(R.string.onboarding_security_data_desc),
        ),
        Triple(
            Icons.Default.Download,
            stringResource(R.string.onboarding_security_export),
            stringResource(R.string.onboarding_security_export_desc),
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        features.forEach { (icon, title, description) ->
            SecurityFeatureItem(icon = icon, title = title, description = description)
        }
    }
}

/**
 * Элемент функции безопасности
 */
@Composable
private fun SecurityFeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Страница готовности к началу работы
 */
@Composable
private fun GetStartedPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_ready_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_ready_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Список ключевых действий
        QuickActionsList()
    }
}

/**
 * Список быстрых действий
 */
@Composable
private fun QuickActionsList() {
    val actions = listOf(
        Pair(stringResource(R.string.onboarding_action_transaction), Icons.Default.MonetizationOn),
        Pair(stringResource(R.string.onboarding_action_categories), Icons.Default.Category),
        Pair(stringResource(R.string.onboarding_action_analytics), Icons.Default.Analytics),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        actions.forEach { (action, icon) ->
            QuickActionItem(action = action, icon = icon)
        }
    }
}

/**
 * Элемент быстрого действия
 */
@Composable
private fun QuickActionItem(action: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = action,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * Стандартная страница функций
 */
@Composable
private fun FeaturePage(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = page.title,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Список функций
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            page.features.forEach { feature ->
                FeatureItem(feature = feature)
            }
        }
    }
}

/**
 * Элемент функции
 */
@Composable
private fun FeatureItem(feature: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Современная навигация с кнопками
 */
@Composable
private fun ModernNavigation(
    pagerState: androidx.compose.foundation.pager.PagerState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Индикаторы страниц
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pagerState.pageCount) { index ->
                val width by animateDpAsState(
                    targetValue = if (pagerState.currentPage == index) 32.dp else 8.dp,
                    animationSpec = tween(300, easing = EaseInOut),
                    label = "indicator",
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(width)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            },
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопки навигации
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(0.3f),
                ) {
                    Text(stringResource(R.string.onboarding_previous))
                }
            } else {
                Spacer(modifier = Modifier.weight(0.3f))
            }

            Spacer(modifier = Modifier.weight(0.1f))

            if (pagerState.currentPage < pagerState.pageCount - 1) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(0.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(stringResource(R.string.onboarding_next))
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(0.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(stringResource(R.string.onboarding_start))
                }
            }
        }

        // Кнопка пропустить
        if (pagerState.currentPage < pagerState.pageCount - 1) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Модель данных для страницы онбординга
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val features: List<String> = emptyList(),
)
