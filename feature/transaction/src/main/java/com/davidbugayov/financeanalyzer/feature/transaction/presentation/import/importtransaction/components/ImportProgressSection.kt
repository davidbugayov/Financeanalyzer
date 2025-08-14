@file:Suppress("FunctionName")
package com.davidbugayov.financeanalyzer.presentation.importtransaction.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Компонент для отображения прогресса импорта
 */
@Composable
fun ImportProgressSection(
    progress: Int,
    message: String,
    modifier: Modifier = Modifier,
    fileName: String = "",
    bankName: String? = null,
) {
    // Создаем бесконечную анимацию вращения
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "rotation",
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(UiR.dimen.space_small)),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = dimensionResource(UiR.dimen.card_elevation),
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(UiR.dimen.space_small)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Отображаем банк и имя файла в одной строке, если есть банк
                if (bankName != null && bankName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = bankName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )

                        // Если есть имя файла, показываем его рядом с банком
                        if (fileName.isNotBlank()) {
                            Text(
                                text = " - ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                } else if (fileName.isNotBlank()) {
                    // Если нет банка, но есть имя файла
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                }

                Text(
                    text = stringResource(UiR.string.importing_file),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = dimensionResource(UiR.dimen.space_small)),
                )

                // Прогресс-бар в красивой карточке
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    shape = RoundedCornerShape(dimensionResource(UiR.dimen.radius_card)),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(UiR.dimen.space_small)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(UiR.dimen.import_progress_indicator_height)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(UiR.dimen.space_small)))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Вращающаяся иконка синхронизации
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier =
                                    Modifier
                                        .size(dimensionResource(UiR.dimen.import_icon_size))
                                        .graphicsLayer {
                                            rotationZ = rotation.value
                                        },
                            )

                            Spacer(modifier = Modifier.width(dimensionResource(UiR.dimen.space_small)))

                            Text(
                                text = stringResource(UiR.string.progress_percentage, progress),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(UiR.dimen.space_small)),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
