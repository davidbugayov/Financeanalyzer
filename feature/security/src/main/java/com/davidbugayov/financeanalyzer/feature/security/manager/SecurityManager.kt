package com.davidbugayov.financeanalyzer.feature.security.manager

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.davidbugayov.financeanalyzer.utils.PreferencesManager
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

/**
 * Менеджер безопасности для управления биометрической аутентификацией и PIN-кодом.
 * Предоставляет функции для настройки и проверки безопасности приложения.
 */
class SecurityManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
) {
    /**
     * Результат аутентификации
     */
    sealed class AuthResult {
        object Success : AuthResult()

        object Failed : AuthResult()

        object Error : AuthResult()

        object UserCancel : AuthResult()

        object HardwareUnavailable : AuthResult()

        object FeatureUnavailable : AuthResult()

        object NoneEnrolled : AuthResult()
    }

    /**
     * Типы аутентификации
     */
    enum class AuthType {
        BIOMETRIC,
        PIN,
        NONE,
    }

    /**
     * Проверяет, поддерживает ли устройство биометрическую аутентификацию
     */
    fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(context)

        // Пробуем разные уровни биометрии
        val weakStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        val strongStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        Timber.d("Биометрия - проверка поддержки: WEAK=$weakStatus, STRONG=$strongStatus")

        return when {
            weakStatus == BiometricManager.BIOMETRIC_SUCCESS -> true
            strongStatus == BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Проверяет, настроена ли биометрическая аутентификация на устройстве
     */
    fun isBiometricEnrolled(): Boolean {
        val biometricManager = BiometricManager.from(context)

        // Пробуем разные уровни биометрии
        val weakStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        val strongStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        Timber.d("Биометрия - проверка регистрации: WEAK=$weakStatus, STRONG=$strongStatus")

        return when {
            weakStatus == BiometricManager.BIOMETRIC_SUCCESS -> true
            strongStatus == BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Возвращает статус биометрической аутентификации
     */
    fun getBiometricStatus(): AuthResult {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> AuthResult.Success
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthResult.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthResult.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthResult.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> AuthResult.FeatureUnavailable
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> AuthResult.FeatureUnavailable
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> AuthResult.FeatureUnavailable
            else -> AuthResult.Error
        }
    }

    /**
     * Выполняет биометрическую аутентификацию
     */
    suspend fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String = "Биометрическая аутентификация",
        subtitle: String = "Подтвердите свою личность",
        negativeButtonText: String = "Отмена",
    ): AuthResult =
        suspendCancellableCoroutine { continuation ->

            Timber.d("Запуск биометрической аутентификации")

            // Проверяем статус биометрии перед запуском
            val biometricManager = BiometricManager.from(context)
            val weakStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            val strongStatus = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

            Timber.d("Статус биометрии перед аутентификацией: WEAK=$weakStatus, STRONG=$strongStatus")

            // Выбираем подходящий тип аутентификации
            val authenticatorType =
                when {
                    strongStatus == BiometricManager.BIOMETRIC_SUCCESS -> BiometricManager.Authenticators.BIOMETRIC_STRONG
                    weakStatus == BiometricManager.BIOMETRIC_SUCCESS -> BiometricManager.Authenticators.BIOMETRIC_WEAK
                    else -> {
                        val result =
                            when {
                                weakStatus == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthResult.HardwareUnavailable
                                weakStatus == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthResult.HardwareUnavailable
                                weakStatus == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthResult.NoneEnrolled
                                else -> AuthResult.FeatureUnavailable
                            }
                        Timber.d("Биометрия недоступна, возвращаем: $result")
                        continuation.resume(result)
                        return@suspendCancellableCoroutine
                    }
                }

            Timber.d("Используем тип аутентификации: $authenticatorType")

            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt =
                BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence,
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            Timber.d("Биометрическая аутентификация - ошибка: $errorCode, $errString")
                            val result =
                                when (errorCode) {
                                    BiometricPrompt.ERROR_USER_CANCELED -> AuthResult.UserCancel
                                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> AuthResult.UserCancel
                                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> AuthResult.HardwareUnavailable
                                    BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> AuthResult.Error
                                    BiometricPrompt.ERROR_TIMEOUT -> AuthResult.Error
                                    else -> AuthResult.Error
                                }
                            if (continuation.isActive) {
                                continuation.resume(result)
                            }
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            Timber.d("Биометрическая аутентификация успешна")
                            if (continuation.isActive) {
                                continuation.resume(AuthResult.Success)
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Timber.d("Биометрическая аутентификация неудачна")
                            // Не завершаем continuation здесь, позволяем пользователю попробовать еще раз
                        }
                    },
                )

            val promptInfo =
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText(negativeButtonText)
                    .build()

            try {
                Timber.d("Показываем диалог биометрической аутентификации")
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при запуске биометрической аутентификации")
                if (continuation.isActive) {
                    continuation.resume(AuthResult.Error)
                }
            }

            continuation.invokeOnCancellation {
                Timber.d("Отмена биометрической аутентификации")
                biometricPrompt.cancelAuthentication()
            }
        }

    /**
     * Проверяет PIN-код
     */
    fun validatePinCode(enteredPin: String): Boolean {
        val savedPin = preferencesManager.getPinCode()
        return savedPin != null && savedPin == enteredPin
    }

    /**
     * Устанавливает PIN-код
     */
    fun setPinCode(pinCode: String) {
        preferencesManager.setPinCode(pinCode)
    }

    /**
     * Удаляет PIN-код
     */
    fun removePinCode() {
        preferencesManager.removePinCode()
    }

    /**
     * Проверяет, установлен ли PIN-код
     */
    fun hasPinCode(): Boolean {
        return preferencesManager.getPinCode() != null
    }

    /**
     * Возвращает текущий тип аутентификации
     */
    fun getCurrentAuthType(): AuthType {
        return when {
            preferencesManager.isBiometricEnabled() && isBiometricSupported() -> AuthType.BIOMETRIC
            preferencesManager.isAppLockEnabled() -> AuthType.PIN
            else -> AuthType.NONE
        }
    }

    /**
     * Проверяет, нужна ли аутентификация
     */
    fun isAuthenticationRequired(): Boolean {
        return preferencesManager.isAppLockEnabled() || preferencesManager.isBiometricEnabled()
    }

    /**
     * Включает блокировку приложения
     */
    fun enableAppLock(pinCode: String) {
        setPinCode(pinCode)
        preferencesManager.setAppLockEnabled(true)
    }

    /**
     * Отключает блокировку приложения
     */
    fun disableAppLock() {
        removePinCode()
        preferencesManager.setAppLockEnabled(false)
        preferencesManager.setBiometricEnabled(false) // Отключаем и биометрию
    }

    /**
     * Включает биометрическую аутентификацию
     */
    fun enableBiometric() {
        if (isBiometricSupported() && isBiometricEnrolled()) {
            preferencesManager.setBiometricEnabled(true)
        }
    }

    /**
     * Отключает биометрическую аутентификацию
     */
    fun disableBiometric() {
        preferencesManager.setBiometricEnabled(false)
    }
}
