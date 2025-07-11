package com.davidbugayov.financeanalyzer.feature.security.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.feature.security.manager.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel для управления логикой аутентификации
 */
class AuthViewModel(
    private val securityManager: SecurityManager,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        updateSecurityState()
    }

    /**
     * Проверяет PIN-код
     */
    fun validatePin(pin: String): Boolean {
        return securityManager.validatePinCode(pin)
    }

    /**
     * Выполняет биометрическую аутентификацию
     */
    suspend fun authenticateWithBiometric(activity: FragmentActivity): SecurityManager.AuthResult {
        Timber.d("AuthViewModel - запуск биометрической аутентификации")
        val result =
            securityManager.authenticateWithBiometric(
                activity = activity,
                title = "Аутентификация",
                subtitle = "Подтвердите свою личность для входа в приложение",
                negativeButtonText = "Использовать PIN",
            )
        Timber.d("AuthViewModel - результат биометрической аутентификации: $result")
        return result
    }

    /**
     * Выполняет биометрическую аутентификацию с колбэком
     */
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onResult: (SecurityManager.AuthResult) -> Unit,
    ) {
        viewModelScope.launch {
            val result = authenticateWithBiometric(activity)
            onResult(result)
        }
    }

    /**
     * Обновляет состояние безопасности
     */
    fun updateSecurityState() {
        val isSupported = securityManager.isBiometricSupported()
        val isEnrolled = securityManager.isBiometricEnrolled()
        val canUseBiometric = isSupported && isEnrolled

        Timber.d(
            "AuthViewModel - обновление состояния: supported=$isSupported, enrolled=$isEnrolled, canUse=$canUseBiometric",
        )

        _state.value =
            _state.value.copy(
                canUseBiometric = canUseBiometric,
            )
    }
}

/**
 * Состояние экрана аутентификации
 */
data class AuthState(
    val canUseBiometric: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) 
