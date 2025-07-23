package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

/**
 * 🎨 Превью для SmartRecommendationCard
 * Показывает различные стили и сценарии использования
 */

class SmartRecommendationPreviewProvider : PreviewParameterProvider<List<SmartRecommendation>> {
    override val values = sequenceOf(
        // Критические рекомендации
        listOf(
            SmartRecommendation(
                title = "Создайте финансовую подушку СРОЧНО",
                description = "У вас менее месяца расходов в резерве. Это критически опасно для финансовой стабильности",
                icon = Icons.Default.Warning,
                priority = SmartRecommendationPriority.CRITICAL,
                impact = "Защита от финансового краха при потере дохода",
                category = RecommendationCategory.EMERGENCY_FUND
            ),
            SmartRecommendation(
                title = "Норма сбережений ниже критической",
                description = "Вы откладываете менее 5% дохода. Это ставит под угрозу ваше финансовое будущее",
                icon = Icons.Default.PriorityHigh,
                priority = SmartRecommendationPriority.CRITICAL,
                impact = "Начните с 10% - это минимум для финансовой безопасности",
                category = RecommendationCategory.SAVINGS
            )
        ),
        // Обычные рекомендации
        listOf(
            SmartRecommendation(
                title = "Увеличьте норму сбережений",
                description = "Стремитесь к сбережениям 15-20% от дохода для финансовой стабильности",
                icon = Icons.Default.Savings,
                priority = SmartRecommendationPriority.HIGH,
                impact = "Увеличение на 5% улучшит финансовое здоровье",
                category = RecommendationCategory.SAVINGS
            ),
            SmartRecommendation(
                title = "Пора подумать об инвестициях",
                description = "У вас отличная финансовая дисциплина! Время приумножать капитал",
                icon = Icons.Default.TrendingUp,
                priority = SmartRecommendationPriority.NORMAL,
                impact = "Инвестиции помогут обогнать инфляцию",
                category = RecommendationCategory.INVESTMENTS
            )
        ),
        // Минимальные рекомендации
        listOf(
            SmartRecommendation(
                title = "Изучите свои достижения",
                description = "Отслеживайте прогресс и получайте мотивацию",
                icon = Icons.Default.EmojiEvents,
                priority = SmartRecommendationPriority.NORMAL,
                category = RecommendationCategory.GENERAL
            )
        )
    )
}

@Preview(name = "Enhanced Style - Light", showBackground = true)
@Composable
private fun SmartRecommendationCardEnhancedPreview(
    @PreviewParameter(SmartRecommendationPreviewProvider::class) recommendations: List<SmartRecommendation>
) {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SmartRecommendationCard(
                    recommendations = recommendations,
                    title = stringResource(R.string.preview_enhanced_title),
                    subtitle = stringResource(R.string.preview_enhanced_subtitle),
                    style = SmartCardStyle.ENHANCED,
                    showPriorityIndicator = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Compact Style - Light", showBackground = true)
@Composable
private fun SmartRecommendationCardCompactPreview() {
    val recommendations = listOf(
        SmartRecommendation(
            title = stringResource(R.string.increase_savings),
            icon = Icons.Default.Savings,
            priority = SmartRecommendationPriority.HIGH,
            category = RecommendationCategory.SAVINGS
        ),
        SmartRecommendation(
            title = stringResource(R.string.create_emergency_fund),
            icon = Icons.Default.PriorityHigh,
            priority = SmartRecommendationPriority.HIGH,
            category = RecommendationCategory.EMERGENCY_FUND
        )
    )
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SmartRecommendationCard(
                    recommendations = recommendations,
                    title = stringResource(R.string.preview_compact_title),
                    subtitle = stringResource(R.string.preview_compact_subtitle),
                    style = SmartCardStyle.COMPACT,
                    showPriorityIndicator = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Minimal Style - Light", showBackground = true)
@Composable
private fun SmartRecommendationCardMinimalPreview() {
    val recommendations = listOf(
        SmartRecommendation(
            title = stringResource(R.string.rec_onboarding_achievements_title),
            description = stringResource(R.string.rec_onboarding_achievements_desc),
            icon = Icons.Default.EmojiEvents,
            priority = SmartRecommendationPriority.NORMAL,
            category = RecommendationCategory.GENERAL
        ),
        SmartRecommendation(
            title = stringResource(R.string.rec_onboarding_import_title),
            description = stringResource(R.string.rec_onboarding_import_desc),
            icon = Icons.Default.Upload,
            priority = SmartRecommendationPriority.HIGH,
            category = RecommendationCategory.GENERAL
        )
    )
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SmartRecommendationCard(
                    recommendations = recommendations,
                    title = stringResource(R.string.preview_minimal_title),
                    subtitle = stringResource(R.string.preview_minimal_subtitle),
                    style = SmartCardStyle.MINIMAL,
                    showPriorityIndicator = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Dark Theme - Enhanced", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SmartRecommendationCardDarkPreview() {
    val recommendations = listOf(
        SmartRecommendation(
            title = stringResource(R.string.rec_critical_emergency_title),
            description = stringResource(R.string.rec_critical_emergency_desc),
            icon = Icons.Default.Warning,
            priority = SmartRecommendationPriority.CRITICAL,
            impact = stringResource(R.string.rec_critical_emergency_impact),
            category = RecommendationCategory.EMERGENCY_FUND
        ),
        SmartRecommendation(
            title = stringResource(R.string.rec_normal_invest_title),
            description = stringResource(R.string.rec_normal_invest_desc),
            icon = Icons.Default.TrendingUp,
            priority = SmartRecommendationPriority.NORMAL,
            impact = stringResource(R.string.rec_normal_invest_impact),
            category = RecommendationCategory.INVESTMENTS
        )
    )
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SmartRecommendationCard(
                    recommendations = recommendations,
                    title = stringResource(R.string.preview_enhanced_title),
                    subtitle = stringResource(R.string.preview_enhanced_subtitle),
                    style = SmartCardStyle.ENHANCED,
                    showPriorityIndicator = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun SmartRecommendationCardEmptyPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SmartRecommendationCard(
                    recommendations = emptyList(),
                    title = stringResource(R.string.preview_empty_title),
                    subtitle = stringResource(R.string.preview_empty_subtitle),
                    style = SmartCardStyle.ENHANCED,
                    showPriorityIndicator = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}