package com.davidbugayov.financeanalyzer.feature.profile.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.utils.AppLocale

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedCode by remember {
        mutableStateOf(
            when (currentLanguage) {
                "English" -> "en"
                "中文" -> "zh"
                else -> "ru"
            },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(UiR.string.settings_language_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(modifier = Modifier.padding(top = dimensionResource(UiR.dimen.spacing_small))) {
                LanguageItem(
                    label = stringResource(UiR.string.settings_language_ru),
                    badge = "RU",
                    selected = selectedCode == "ru",
                    onClick = { selectedCode = "ru" },
                )
                Spacer(modifier = Modifier.padding(4.dp))
                LanguageItem(
                    label = stringResource(UiR.string.settings_language_en),
                    badge = "EN",
                    selected = selectedCode == "en",
                    onClick = { selectedCode = "en" },
                )
                Spacer(modifier = Modifier.padding(4.dp))
                LanguageItem(
                    label = stringResource(UiR.string.settings_language_zh),
                    badge = "中文",
                    selected = selectedCode == "zh",
                    onClick = { selectedCode = "zh" },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Применяем локаль немедленно и пересоздаём активити для мгновенного эффекта
                    timber.log.Timber
                        .tag("LANG")
                        .d("LanguageSelectionDialog.confirm: selected=%s", selectedCode)
                    AppLocale.apply(selectedCode)
                    onLanguageSelected(selectedCode)
                    val activity = (context as? Activity)
                    timber.log.Timber
                        .tag("LANG")
                        .d("LanguageSelectionDialog.confirm: recreate activity=%s", activity)
                    // Надёжный перезапуск Activity для применения локали по всему дереву
                    val restartIntent =
                        activity?.packageManager?.getLaunchIntentForPackage(activity.packageName)?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    if (restartIntent != null) {
                        timber.log.Timber
                            .tag("LANG")
                            .d("LanguageSelectionDialog.confirm: restart via launch intent")
                        activity.startActivity(restartIntent)
                        activity.finish()
                    } else {
                        timber.log.Timber
                            .tag("LANG")
                            .d("LanguageSelectionDialog.confirm: restart via recreate()")
                        activity?.recreate()
                    }
                    onDismiss()
                },
                colors =
                    androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) { Text(stringResource(UiR.string.select)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(UiR.string.cancel)) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(dimensionResource(UiR.dimen.radius_xlarge)),
        tonalElevation = 0.dp,
        properties =
            androidx.compose.ui.window
                .DialogProperties(usePlatformDefaultWidth = false),
    )
}

@Composable
private fun LanguageItem(
    label: String,
    badge: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
        }

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(UiR.string.select),
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp),
            )
        }
    }
}
