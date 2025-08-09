package com.davidbugayov.financeanalyzer.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.davidbugayov.financeanalyzer.ui.R as UiR
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val scrollState = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface,
                                ),
                        ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                     .padding(dimensionResource(UiR.dimen.onboarding_header_padding_horizontal)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Заголовок приложения
            Text(
                text = stringResource(UiR.string.onboarding_welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(UiR.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка начать - размещаем рано для быстрого доступа
            Button(
                onClick = {
                    viewModel.completeOnboarding()
                    onFinish()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = stringResource(UiR.string.onboarding_start_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Основные возможности приложения
            Text(
                text = stringResource(UiR.string.onboarding_features_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Карточки с возможностями
            FeatureCard(
                icon = Icons.Default.AccountBalance,
                title = stringResource(UiR.string.onboarding_feature_tracking_title),
                description = stringResource(UiR.string.onboarding_feature_tracking_description),
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(UiR.string.onboarding_feature_analytics_title),
                description = stringResource(UiR.string.onboarding_feature_analytics_description),
                color = MaterialTheme.colorScheme.secondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.PieChart,
                title = stringResource(UiR.string.onboarding_feature_budget_title),
                description = stringResource(UiR.string.onboarding_feature_budget_description),
                color = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.Security,
                title = stringResource(UiR.string.onboarding_feature_security_title),
                description = stringResource(UiR.string.onboarding_feature_security_description),
                color = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.Widgets,
                title = stringResource(UiR.string.onboarding_feature_widgets_title),
                description = stringResource(UiR.string.onboarding_feature_widgets_description),
                color = Color(0xFF4CAF50),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Преимущества
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(UiR.string.onboarding_benefits_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BenefitItem(stringResource(UiR.string.onboarding_benefit_free))
                    BenefitItem(stringResource(UiR.string.onboarding_benefit_offline))
                    BenefitItem(stringResource(UiR.string.onboarding_benefit_privacy))
                    BenefitItem(stringResource(UiR.string.onboarding_benefit_simple))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
