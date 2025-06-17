package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R

/**
 * Компонент для отображения пронумерованного шага в инструкции
 */
@Composable
fun InstructionStep(
    number: Int,
    text: String,
    icon: ImageVector? = null,
    isLastStep: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Номер шага в круге
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Текст шага
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )

                // Иконка (если есть)
                icon?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Разделитель между шагами (кроме последнего)
            if (!isLastStep) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .height(16.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

/**
 * Компонент для отображения заметки или важной информации
 */
@Composable
fun InstructionNote(text: String, isImportant: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isImportant)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (isImportant)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = if (isImportant)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

/**
 * Компонент для отображения инструкций по импорту для Сбербанка
 */
@Composable
fun SberbankInstructions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.sberbank_instructions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        InstructionStep(
            number = 1,
            text = "Войдите в приложение СберБанк Онлайн",
            icon = Icons.Outlined.Login,
        )

        InstructionStep(
            number = 2,
            text = "Перейдите в раздел «История операций»",
            icon = Icons.Outlined.Description,
        )

        InstructionStep(
            number = 3,
            text = "Нажмите на кнопку «Выписка» в правом верхнем углу",
            icon = Icons.Outlined.Download,
        )

        InstructionStep(
            number = 4,
            text = "Выберите период и счет для выписки",
            icon = Icons.Outlined.AccountBalance,
        )

        InstructionStep(
            number = 5,
            text = "Нажмите «Сформировать» и выберите формат CSV",
            icon = Icons.Default.CheckCircle,
            isLastStep = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        InstructionNote(
            text = stringResource(R.string.sberbank_note),
            isImportant = false,
        )
    }
}

/**
 * Компонент для отображения инструкций по импорту для Тинькофф
 */
@Composable
fun TinkoffInstructions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.tinkoff_instructions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        InstructionStep(
            number = 1,
            text = "Войдите в личный кабинет Тинькофф",
            icon = Icons.Outlined.Web,
        )

        InstructionStep(
            number = 2,
            text = "Перейдите в раздел «Выписки и справки»",
            icon = Icons.Outlined.Description,
        )

        InstructionStep(
            number = 3,
            text = "Выберите карту и период для выписки",
            icon = Icons.Outlined.AccountBalance,
        )

        InstructionStep(
            number = 4,
            text = "Нажмите «Сформировать выписку» и выберите формат CSV",
            icon = Icons.Outlined.Download,
            isLastStep = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        InstructionNote(
            text = "Тинькофф предоставляет выписки в формате CSV, который хорошо подходит для импорта. Выписку можно получить только через веб-версию.",
            isImportant = false,
        )
    }
}

/**
 * Компонент для отображения инструкций по импорту для Альфа-Банка
 */
@Composable
fun AlfaBankInstructions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Мобильное приложение
        Text(
            text = stringResource(R.string.alfabank_instructions_title_mobile),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        InstructionStep(
            number = 1,
            text = "Откройте приложение Альфа-Мобайл",
            icon = Icons.Outlined.Smartphone,
        )

        InstructionStep(
            number = 2,
            text = "Выберите карту или счёт",
            icon = Icons.Outlined.AccountBalance,
        )

        InstructionStep(
            number = 3,
            text = "Нажмите «Выписка» в нижней части экрана",
            icon = Icons.Outlined.Description,
        )

        InstructionStep(
            number = 4,
            text = "Выберите период и нажмите «Отправить на почту»",
            icon = Icons.Outlined.Download,
            isLastStep = true,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Веб-версия
        Text(
            text = stringResource(R.string.alfabank_instructions_title_web),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        InstructionStep(
            number = 1,
            text = "Войдите в Альфа-Клик",
            icon = Icons.Outlined.Web,
        )

        InstructionStep(
            number = 2,
            text = "Выберите счёт в разделе «Счета»",
            icon = Icons.Outlined.AccountBalance,
        )

        InstructionStep(
            number = 3,
            text = "Нажмите «Выписка» и выберите период",
            icon = Icons.Outlined.Description,
        )

        InstructionStep(
            number = 4,
            text = "Нажмите «Сохранить» и выберите формат CSV",
            icon = Icons.Outlined.Download,
            isLastStep = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        InstructionNote(
            text = stringResource(R.string.alfabank_note),
            isImportant = true,
        )
    }
}

/**
 * Компонент для отображения инструкций по импорту для Озон
 */
@Composable
fun OzonInstructions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.ozon_instructions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        InstructionStep(
            number = 1,
            text = "Войдите в личный кабинет на сайте Ozon",
            icon = Icons.Outlined.Web,
        )

        InstructionStep(
            number = 2,
            text = "Перейдите в раздел «Заказы»",
            icon = Icons.Outlined.Description,
        )

        InstructionStep(
            number = 3,
            text = "Выберите период и нажмите «Скачать историю заказов»",
            icon = Icons.Outlined.Download,
        )

        InstructionStep(
            number = 4,
            text = "Выберите формат CSV в появившемся меню",
            icon = Icons.Default.CheckCircle,
            isLastStep = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        InstructionNote(
            text = "История заказов Ozon содержит информацию о ваших покупках, которую можно импортировать как расходы.",
            isImportant = false,
        )
    }
}

/**
 * Компонент для отображения инструкций по формату CSV
 */
@Composable
fun CSVInstructions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.csv_instructions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = stringResource(R.string.csv_format_instructions),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Пример CSV формата
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.csv_example_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Text(
                    text = stringResource(R.string.csv_example),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        InstructionNote(
            text = stringResource(R.string.csv_note),
            isImportant = true,
        )
    }
}


