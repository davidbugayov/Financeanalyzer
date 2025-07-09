package com.davidbugayov.financeanalyzer.feature.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.ComponentActivity
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.feature.security.components.PinKeyboard
import com.davidbugayov.financeanalyzer.feature.security.manager.SecurityManager
import com.davidbugayov.financeanalyzer.feature.security.viewmodel.AuthViewModel
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Экран аутентификации для входа в приложение
 * @param onAuthSuccess Обработчик успешной аутентификации
 * @param viewModel ViewModel для управления логикой аутентификации
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferencesManager: PreferencesManager = koinInject()
    
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Получаем FragmentActivity из контекста
    val activity = remember(context) {
        Timber.d("AuthScreen - поиск FragmentActivity в контексте: ${context::class.simpleName}")
        when (context) {
            is FragmentActivity -> {
                Timber.d("AuthScreen - найдена FragmentActivity: ${context::class.simpleName}")
                context
            }
            else -> {
                // Попробуем найти FragmentActivity в цепочке контекстов
                var currentContext = context
                while (currentContext is android.content.ContextWrapper) {
                    Timber.d("AuthScreen - проверяем контекст: ${currentContext::class.simpleName}")
                    if (currentContext is FragmentActivity) {
                        Timber.d("AuthScreen - найдена FragmentActivity в цепочке: ${currentContext::class.simpleName}")
                        return@remember currentContext
                    }
                    currentContext = currentContext.baseContext
                }
                Timber.w("AuthScreen - FragmentActivity не найдена")
                null
            }
        }
    }

    // Обновляем состояние безопасности при запуске
    LaunchedEffect(Unit) {
        viewModel.updateSecurityState()
        
        // Логируем просмотр экрана аутентификации
        val hasPinCode = preferencesManager.getPinCode() != null
        val biometricSupported = state.canUseBiometric
        val biometricEnrolled = state.canUseBiometric && preferencesManager.isBiometricEnabled()
        
        AnalyticsUtils.logSecurityAuthScreenViewed(
            hasPinCode = hasPinCode,
            biometricSupported = biometricSupported,
            biometricEnrolled = biometricEnrolled
        )
    }

    // Автоматически пробуем биометрическую аутентификацию при запуске, если она включена
    LaunchedEffect(state.canUseBiometric) {
        val isBiometricEnabled = preferencesManager.isBiometricEnabled()
        Timber.d("AuthScreen - проверка автозапуска биометрии: canUse=${state.canUseBiometric}, enabled=$isBiometricEnabled, activity=$activity")
        
        if (state.canUseBiometric && isBiometricEnabled && activity != null) {
            Timber.d("AuthScreen - запуск автоматической биометрической аутентификации")
            val result = viewModel.authenticateWithBiometric(activity)
            Timber.d("AuthScreen - результат автоматической аутентификации: $result")
            when (result) {
                SecurityManager.AuthResult.Success -> {
                    AnalyticsUtils.logSecurityAuthSuccess(AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC)
                    onAuthSuccess()
                }
                SecurityManager.AuthResult.Error -> {
                    AnalyticsUtils.logSecurityAuthFailed(
                        authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                        reason = AnalyticsConstants.Values.AUTH_RESULT_ERROR
                    )
                }
                SecurityManager.AuthResult.UserCancel -> {
                    AnalyticsUtils.logSecurityAuthFailed(
                        authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                        reason = AnalyticsConstants.Values.AUTH_RESULT_CANCELLED
                    )
                }
                SecurityManager.AuthResult.Failed -> {
                    AnalyticsUtils.logSecurityAuthFailed(
                        authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                        reason = AnalyticsConstants.Values.AUTH_RESULT_FAILED
                    )
                }
                SecurityManager.AuthResult.HardwareUnavailable,
                SecurityManager.AuthResult.FeatureUnavailable,
                SecurityManager.AuthResult.NoneEnrolled -> {
                    AnalyticsUtils.logSecurityAuthFailed(
                        authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                        reason = AnalyticsConstants.Values.AUTH_RESULT_ERROR
                    )
                }
            }
        }
    }

    FinanceAnalyzerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Логотип и заголовок
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = stringResource(R.string.auth_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.auth_enter_pin),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Отображение точек PIN-кода
                PinDots(
                    pinLength = pin.length,
                    maxLength = 4
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Отображение ошибки
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Кнопка биометрической аутентификации
                if (state.canUseBiometric && preferencesManager.isBiometricEnabled() && activity != null) {
                    Button(
                        onClick = {
                            Timber.d("AuthScreen - нажата кнопка биометрической аутентификации")
                            viewModel.authenticateWithBiometric(activity) { result ->
                                Timber.d("AuthScreen - результат ручной аутентификации: $result")
                                when (result) {
                                    SecurityManager.AuthResult.Success -> {
                                        AnalyticsUtils.logSecurityAuthSuccess(AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC)
                                        onAuthSuccess()
                                    }
                                    SecurityManager.AuthResult.Error -> {
                                        AnalyticsUtils.logSecurityAuthFailed(
                                            authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                                            reason = AnalyticsConstants.Values.AUTH_RESULT_ERROR
                                        )
                                    }
                                    SecurityManager.AuthResult.UserCancel -> {
                                        AnalyticsUtils.logSecurityAuthFailed(
                                            authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                                            reason = AnalyticsConstants.Values.AUTH_RESULT_CANCELLED
                                        )
                                    }
                                    SecurityManager.AuthResult.Failed -> {
                                        AnalyticsUtils.logSecurityAuthFailed(
                                            authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                                            reason = AnalyticsConstants.Values.AUTH_RESULT_FAILED
                                        )
                                    }
                                    SecurityManager.AuthResult.HardwareUnavailable,
                                    SecurityManager.AuthResult.FeatureUnavailable,
                                    SecurityManager.AuthResult.NoneEnrolled -> {
                                        AnalyticsUtils.logSecurityAuthFailed(
                                            authMethod = AnalyticsConstants.Values.AUTH_METHOD_BIOMETRIC,
                                            reason = AnalyticsConstants.Values.AUTH_RESULT_ERROR
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.auth_use_biometric))
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Цифровая клавиатура
                PinKeyboard(
                    onNumberClick = { number ->
                        error = null
                        if (pin.length < 4) {
                            pin += number
                            
                            // Проверяем PIN когда введено 4 символа
                            if (pin.length == 4) {
                                if (viewModel.validatePin(pin)) {
                                    AnalyticsUtils.logSecurityAuthSuccess(AnalyticsConstants.Values.AUTH_METHOD_PIN)
                                    onAuthSuccess()
                                } else {
                                    AnalyticsUtils.logSecurityAuthFailed(
                                        authMethod = AnalyticsConstants.Values.AUTH_METHOD_PIN,
                                        reason = AnalyticsConstants.Values.AUTH_RESULT_FAILED
                                    )
                                    error = context.getString(R.string.auth_wrong_pin)
                                    pin = ""
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        error = null
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Отображение точек для визуализации введенного PIN-кода
 */
@Composable
private fun PinDots(
    pinLength: Int,
    maxLength: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (index < pinLength) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        shape = CircleShape
                    )
            )
        }
    }
} 