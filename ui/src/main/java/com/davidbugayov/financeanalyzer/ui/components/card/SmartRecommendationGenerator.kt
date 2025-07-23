package com.davidbugayov.financeanalyzer.ui.components.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R

/**
 * 🧠 Умный генератор персональных финансовых рекомендаций
 * Создан профессиональным маркетологом с учетом психологии потребителей
 */
object SmartRecommendationGenerator {

    /**
     * 🎯 Генерация критически важных рекомендаций для финансового здоровья
     */
    @Composable
    fun generateCriticalFinancialRecommendations(
        savingsRate: Float,
        monthsOfEmergencyFund: Float,
        debtToIncomeRatio: Float = 0f,
        topExpenseCategory: String = "",
        topCategoryPercentage: Float = 0f,
        totalTransactions: Int = 0,
        unusualSpendingDetected: Boolean = false
    ): List<SmartRecommendation> {
        val recommendations = mutableListOf<SmartRecommendation>()

        // 🚨 КРИТИЧЕСКИЕ рекомендации (требуют немедленного внимания)

        // Отсутствие финансовой подушки
        if (monthsOfEmergencyFund < 1f) {
            recommendations.add(
                SmartRecommendation(
                    title = "Создайте финансовую подушку СРОЧНО",
                    description = "У вас менее месяца расходов в резерве. Это критически опасно для финансовой стабильности",
                    icon = Icons.Default.Warning,
                    priority = SmartRecommendationPriority.CRITICAL,
                    impact = "Защита от финансового краха при потере дохода",
                    category = RecommendationCategory.EMERGENCY_FUND
                )
            )
        }

        // Критически низкие сбережения
        if (savingsRate < 5f) {
            recommendations.add(
                SmartRecommendation(
                    title = "Норма сбережений ниже критической",
                    description = "Вы откладываете менее 5% дохода. Это ставит под угрозу ваше финансовое будущее",
                    icon = Icons.Default.PriorityHigh,
                    priority = SmartRecommendationPriority.CRITICAL,
                    impact = "Начните с 10% - это минимум для финансовой безопасности",
                    category = RecommendationCategory.SAVINGS
                )
            )
        }

        // Высокая долговая нагрузка
        if (debtToIncomeRatio > 0.4f) {
            recommendations.add(
                SmartRecommendation(
                    title = "Опасный уровень задолженности",
                    description = "Долги составляют более 40% вашего дохода. Это может привести к финансовой яме",
                    icon = Icons.Default.Error,
                    priority = SmartRecommendationPriority.CRITICAL,
                    impact = "Срочное погашение долгов освободит ${(debtToIncomeRatio * 100).toInt()}% дохода",
                    category = RecommendationCategory.EXPENSES
                )
            )
        }

        // ⚠️ ВАЖНЫЕ рекомендации (требуют внимания в ближайшее время)

        // Малая финансовая подушка
        if (monthsOfEmergencyFund in 1f..3f) {
            recommendations.add(
                SmartRecommendation(
                    title = "Увеличьте финансовую подушку",
                    description = "Ваших накоплений хватит только на ${monthsOfEmergencyFund.toInt()} мес. Эксперты рекомендуют 3-6 месяцев",
                    icon = Icons.Default.Savings,
                    priority = SmartRecommendationPriority.HIGH,
                    impact = "Финансовая подушка на 6 месяцев даст полную защиту",
                    category = RecommendationCategory.EMERGENCY_FUND
                )
            )
        }

        // Низкие сбережения
        if (savingsRate in 5f..15f) {
            recommendations.add(
                SmartRecommendation(
                    title = "Увеличьте норму сбережений",
                    description = "Вы откладываете ${savingsRate.toInt()}%. Это хорошо, но можно лучше!",
                    icon = Icons.Default.AccountBalance,
                    priority = SmartRecommendationPriority.HIGH,
                    impact = "Увеличение до 20% ускорит достижение финансовых целей в 2 раза",
                    category = RecommendationCategory.SAVINGS
                )
            )
        }

        // Концентрация расходов в одной категории
        if (topExpenseCategory.isNotEmpty() && topCategoryPercentage > 40f) {
            recommendations.add(
                SmartRecommendation(
                    title = "Оптимизируйте \"$topExpenseCategory\"",
                    description = "Эта категория \"съедает\" ${topCategoryPercentage.toInt()}% ваших расходов. Слишком много!",
                    icon = Icons.Default.PieChart,
                    priority = SmartRecommendationPriority.HIGH,
                    impact = "Сокращение на 10% освободит значительные средства",
                    category = RecommendationCategory.EXPENSES
                )
            )
        }

        // 💡 СРЕДНИЕ рекомендации (стоит рассмотреть)

        // Улучшение привычек трат
        if (totalTransactions > 150) {
            recommendations.add(
                SmartRecommendation(
                    title = "Слишком много мелких трат",
                    description = "У вас $totalTransactions операций. Много мелких покупок \"съедают\" бюджет незаметно",
                    icon = Icons.Default.ShoppingCart,
                    priority = SmartRecommendationPriority.MEDIUM,
                    impact = "Планирование покупок сэкономит до 15% расходов",
                    category = RecommendationCategory.HABITS
                )
            )
        }

        // Необычные траты
        if (unusualSpendingDetected) {
            recommendations.add(
                SmartRecommendation(
                    title = "Обнаружены нетипичные траты",
                    description = "В этом периоде ваши расходы отличаются от обычного паттерна",
                    icon = Icons.Default.Analytics,
                    priority = SmartRecommendationPriority.MEDIUM,
                    impact = "Анализ поможет выявить \"утечки\" в бюджете",
                    category = RecommendationCategory.BUDGETING
                )
            )
        }

        // ✅ НОРМАЛЬНЫЕ рекомендации (общие советы)

        // Хорошие показатели - инвестиции
        if (savingsRate > 20f && monthsOfEmergencyFund > 3f) {
            recommendations.add(
                SmartRecommendation(
                    title = "Пора подумать об инвестициях",
                    description = "У вас отличная финансовая дисциплина! Время приумножать капитал",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    priority = SmartRecommendationPriority.NORMAL,
                    impact = "Инвестиции помогут обогнать инфляцию и накопить на крупные цели",
                    category = RecommendationCategory.INVESTMENTS
                )
            )
        }

        // Регулярный анализ бюджета
        recommendations.add(
            SmartRecommendation(
                title = "Еженедельный анализ трат",
                description = "Потратьте 10 минут в неделю на анализ расходов. Это изменит ваши финансы!",
                icon = Icons.Default.Schedule,
                priority = SmartRecommendationPriority.NORMAL,
                impact = "Регулярный контроль помогает экономить до 20% бюджета",
                category = RecommendationCategory.HABITS
            )
        )

        return recommendations.sortedBy { it.priority.order }
    }

    /**
     * 🏠 Рекомендации для главного экрана (онбординг)
     */
    @Composable
    fun generateOnboardingRecommendations(): List<SmartRecommendation> {
        return listOf(
            SmartRecommendation(
                title = "Изучите свои достижения",
                description = "Отслеживайте прогресс и получайте мотивацию для финансовых целей",
                icon = Icons.Default.EmojiEvents,
                priority = SmartRecommendationPriority.NORMAL,
                category = RecommendationCategory.GENERAL
            ),
            SmartRecommendation(
                title = "Импортируйте операции",
                description = "Загрузите выписки банка для автоматического анализа",
                icon = Icons.Default.Upload,
                priority = SmartRecommendationPriority.HIGH,
                category = RecommendationCategory.GENERAL
            ),
            SmartRecommendation(
                title = "Анализируйте статистику",
                description = "Изучайте графики доходов и расходов для принятия решений",
                icon = Icons.Default.Analytics,
                priority = SmartRecommendationPriority.MEDIUM,
                category = RecommendationCategory.GENERAL
            ),
            SmartRecommendation(
                title = "Получайте умные советы",
                description = "Персональные рекомендации на основе анализа ваших финансов",
                icon = Icons.Default.Lightbulb,
                priority = SmartRecommendationPriority.NORMAL,
                category = RecommendationCategory.GENERAL
            )
        )
    }

    /**
     * 📊 Динамические советы для экрана статистики
     */
    @Composable
    fun generateStatisticsTips(): List<SmartRecommendation> {
        val tips = listOf(
            "Сравнивайте периоды для выявления трендов",
            "Анализируйте категории расходов",
            "Планируйте бюджет на основе данных",
            "Следите за нормой сбережений",
            "Контролируйте импульсивные покупки"
        )

        return tips.mapIndexed { index, tip ->
            SmartRecommendation(
                title = tip,
                description = "Профессиональный совет №${index + 1}",
                icon = when (index % 5) {
                    0 -> Icons.Default.Compare
                    1 -> Icons.Default.PieChart
                    2 -> Icons.Default.AccountBalanceWallet
                    3 -> Icons.Default.Savings
                    else -> Icons.Default.Psychology
                },
                priority = SmartRecommendationPriority.NORMAL,
                category = RecommendationCategory.GENERAL
            )
        }
    }

    /**
     * 🎯 Самые важные советы для бюджетирования
     */
    @Composable
    fun generateTopBudgetingTips(): List<SmartRecommendation> {
        return listOf(
            SmartRecommendation(
                title = "Правило 50/30/20",
                description = "50% на нужды, 30% на желания, 20% на сбережения и долги",
                icon = Icons.Default.Percent,
                priority = SmartRecommendationPriority.HIGH,
                impact = "Золотое правило личных финансов",
                category = RecommendationCategory.BUDGETING
            ),
            SmartRecommendation(
                title = "Автоматизируйте сбережения",
                description = "Настройте автоперевод в первый день после зарплаты",
                icon = Icons.Default.AutoMode,
                priority = SmartRecommendationPriority.HIGH,
                impact = "Увеличивает сбережения на 30-50%",
                category = RecommendationCategory.SAVINGS
            ),
            SmartRecommendation(
                title = "Отслеживайте каждую трату",
                description = "Записывайте все расходы в течение месяца",
                icon = Icons.Default.Visibility,
                priority = SmartRecommendationPriority.MEDIUM,
                impact = "Экономия до 20% бюджета от осознанности",
                category = RecommendationCategory.HABITS
            ),
            SmartRecommendation(
                title = "Создайте целевые категории",
                description = "Распределите бюджет по конкретным целям и потребностям",
                icon = Icons.Default.Category,
                priority = SmartRecommendationPriority.MEDIUM,
                impact = "Структурированный подход к тратам",
                category = RecommendationCategory.BUDGETING
            )
        )
    }

    /**
     * 🔄 Конвертация старых рекомендаций в новый формат
     */
    fun convertLegacyRecommendations(
        oldRecommendations: List<Any>
    ): List<SmartRecommendation> {
        // Здесь можно добавить логику конвертации старых форматов
        return emptyList()
    }
}
