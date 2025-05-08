package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import timber.log.Timber

/**
 * Менеджер состояний разрешения для уведомлений.
 * Реализует конечный автомат для управления жизненным циклом разрешения.
 */
class PermissionManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("finance_analyzer_prefs", 0)

    /**
     * Состояния разрешения для уведомлений
     */
    enum class NotificationPermissionState {

        INITIAL,                // Начальное состояние, после установки
        ONBOARDING_COMPLETED,   // Онбординг завершен
        PERMISSION_REQUESTED,   // Запрос разрешения отправлен
        PERMISSION_GRANTED,     // Разрешение предоставлено
        PERMISSION_DENIED,      // Разрешение отклонено
        PERMANENTLY_DENIED,     // Разрешение отклонено постоянно ("не спрашивать снова")
    }

    /**
     * События, которые могут изменить состояние разрешения
     */
    enum class PermissionEvent {

        FINISH_ONBOARDING,     // Завершение онбординга
        REQUEST_PERMISSION,    // Запрос разрешения
        GRANT_PERMISSION,      // Предоставление разрешения
        DENY_PERMISSION,       // Отклонение разрешения
        DISMISS_DIALOG,        // Закрытие диалога
        OPEN_SETTINGS,         // Открытие настроек приложения
    }

    /**
     * Получение текущего состояния разрешения
     */
    fun getCurrentState(): NotificationPermissionState {
        val stateOrdinal = sharedPreferences.getInt(
            "notification_permission_state",
            NotificationPermissionState.INITIAL.ordinal
        )
        return NotificationPermissionState.values()[stateOrdinal]
    }

    /**
     * Обработка события и изменение состояния разрешения
     */
    fun processEvent(event: PermissionEvent) {
        val currentState = getCurrentState()
        val newState = when (event) {
            PermissionEvent.FINISH_ONBOARDING -> {
                when (currentState) {
                    NotificationPermissionState.INITIAL -> NotificationPermissionState.ONBOARDING_COMPLETED
                    else -> currentState
                }
            }

            PermissionEvent.REQUEST_PERMISSION -> {
                when (currentState) {
                    NotificationPermissionState.ONBOARDING_COMPLETED,
                    NotificationPermissionState.PERMISSION_DENIED -> NotificationPermissionState.PERMISSION_REQUESTED

                    else -> currentState
                }
            }

            PermissionEvent.GRANT_PERMISSION -> {
                NotificationPermissionState.PERMISSION_GRANTED
            }

            PermissionEvent.DENY_PERMISSION -> {
                when (currentState) {
                    NotificationPermissionState.PERMISSION_REQUESTED -> NotificationPermissionState.PERMISSION_DENIED
                    NotificationPermissionState.PERMISSION_DENIED -> NotificationPermissionState.PERMANENTLY_DENIED
                    else -> currentState
                }
            }

            PermissionEvent.DISMISS_DIALOG -> {
                when (currentState) {
                    NotificationPermissionState.PERMISSION_DENIED -> NotificationPermissionState.PERMANENTLY_DENIED
                    else -> currentState
                }
            }

            PermissionEvent.OPEN_SETTINGS -> {
                currentState // Открытие настроек не меняет состояние
            }
        }

        if (newState != currentState) {
            Timber.d("Состояние разрешения изменено: $currentState -> $newState")
            saveState(newState)
        }
    }

    /**
     * Сохранение состояния в SharedPreferences
     */
    private fun saveState(state: NotificationPermissionState) {
        sharedPreferences.edit()
            .putInt("notification_permission_state", state.ordinal)
            .apply()
    }

    /**
     * Проверка, нужно ли показывать диалог запроса разрешения на главном экране
     */
    fun shouldShowPermissionDialogOnMain(): Boolean {
        // Проверяем, есть ли уже разрешение
        if (PermissionUtils.hasNotificationPermission(context)) {
            return false
        }

        // Проверяем поддержку Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false
        }

        // Проверяем текущее состояние
        val currentState = getCurrentState()
        Timber.d("Проверка показа диалога на главном экране. Текущее состояние: $currentState")

        return when (currentState) {
            NotificationPermissionState.ONBOARDING_COMPLETED -> true
            else -> false // Во всех остальных случаях не показываем
        }
    }

    /**
     * Проверка, нужно ли показывать диалог перехода в настройки после отказа
     */
    fun shouldShowSettingsDialog(): Boolean {
        // Только для Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false
        }

        // Проверяем, показывали ли уже диалог о переходе в настройки
        val hasShownPermissionDeniedDialog = sharedPreferences.getBoolean("has_shown_permission_denied_dialog", false)
        if (hasShownPermissionDeniedDialog) {
            return false
        }

        // Показываем диалог только если разрешение отклонено и не постоянно
        val currentState = getCurrentState()
        return currentState == NotificationPermissionState.PERMISSION_DENIED
    }

    /**
     * Отмечаем, что диалог запроса разрешения был закрыт на главном экране
     */
    fun markMainDialogDismissed() {
        processEvent(PermissionEvent.DISMISS_DIALOG)
    }

    // --- Новый функционал для однократного показа диалога разрешения ---
    fun markPermissionDialogShown() {
        sharedPreferences.edit().putBoolean("was_permission_dialog_shown", true).apply()
    }

    fun wasPermissionDialogShown(): Boolean {
        return sharedPreferences.getBoolean("was_permission_dialog_shown", false)
    }
} 