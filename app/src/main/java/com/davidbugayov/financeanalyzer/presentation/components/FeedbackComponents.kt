package com.davidbugayov.financeanalyzer.presentation.components
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.R
import kotlinx.coroutines.delay

/**
 * Типы уведомлений для обратной связи
 */
enum class FeedbackType {

    SUCCESS,
    ERROR,
    WARNING,
    INFO,
}

/**
 * Компонент для отображения уведомлений с анимацией
 */
@Composable
fun FeedbackMessage(
    title: String,
    message: String? = null,
    type: FeedbackType,
    visible: Boolean,
    onDismiss: () -> Unit,
    duration: Long = 3000L,
    modifier: Modifier = Modifier,
    isFilePath: Boolean = false,
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            color = when (type) {
                FeedbackType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                FeedbackType.ERROR -> MaterialTheme.colorScheme.errorContainer
                FeedbackType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                FeedbackType.INFO -> MaterialTheme.colorScheme.secondaryContainer
            },
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 8.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp),
            ) {
                Icon(
                    imageVector = when (type) {
                        FeedbackType.SUCCESS -> Icons.Default.CheckCircle
                        FeedbackType.ERROR -> Icons.Default.Error
                        FeedbackType.WARNING -> Icons.Default.Warning
                        FeedbackType.INFO -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (type) {
                        FeedbackType.SUCCESS -> MaterialTheme.colorScheme.primary
                        FeedbackType.ERROR -> MaterialTheme.colorScheme.error
                        FeedbackType.WARNING -> MaterialTheme.colorScheme.tertiary
                        FeedbackType.INFO -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                if (!message.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    if (isFilePath) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(
                                    if (isCopied) {
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.18f,
                                        )
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = MaterialTheme.shapes.small,
                                )
                                .clickable {
                                    val clipboard = android.content.Context.CLIPBOARD_SERVICE
                                    val clip = android.content.ClipData.newPlainText(
                                        "file path",
                                        message,
                                    )
                                    (
                                        context.getSystemService(
                                            clipboard,
                                        ) as? android.content.ClipboardManager
                                        )?.setPrimaryClip(
                                        clip,
                                    )
                                    Toast.makeText(context, "Путь скопирован", Toast.LENGTH_SHORT).show()
                                    isCopied = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                        if (isCopied) {
                            LaunchedEffect(isCopied) {
                                delay(700)
                                isCopied = false
                            }
                        }
                    } else {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        LaunchedEffect(visible) {
            if (visible) {
                delay(duration)
                onDismiss()
            }
        }
    }
}

@Composable
fun EnhancedEmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = stringResource(id = R.string.empty_state),
            modifier = Modifier
                .size(128.dp)
                .padding(dimensionResource(R.dimen.spacing_normal)),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )

        Text(
            text = stringResource(id = R.string.empty_transactions),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(id = R.string.empty_transactions_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
