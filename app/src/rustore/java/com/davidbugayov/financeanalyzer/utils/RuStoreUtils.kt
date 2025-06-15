package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.review.RuStoreReviewManagerFactory
import timber.log.Timber

/**
 * Утилитный класс для работы с RuStore API
 */
object RuStoreUtils {

    private const val PREFS_NAME = "rustore_utils_prefs"
    private const val KEY_LAST_REVIEW_TIME = "last_review_request_time"
    private const val KEY_LAST_UPDATE_CHECK_TIME = "last_update_check_time"
    
    private const val REVIEW_REQUEST_INTERVAL = 7 * 24 * 60 * 60 * 1000L // 7 дней в миллисекундах
    private const val UPDATE_CHECK_INTERVAL = 7 * 24 * 60 * 60 * 1000L // 7 дней в миллисекундах

    /**
     * Запрашивает отзыв пользователя через RuStore API
     * Показывает диалог отзыва только раз в 7 дней
     *
     * @param context Контекст приложения
     */
    fun requestReview(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastReviewRequestTime = prefs.getLong(KEY_LAST_REVIEW_TIME, 0)
        val currentTime = System.currentTimeMillis()

        // Проверяем, прошло ли достаточно времени с последнего запроса отзыва
        if (currentTime - lastReviewRequestTime < REVIEW_REQUEST_INTERVAL) {
            Timber.d("Слишком рано для запроса отзыва. Пропускаем.")
            return
        }

        try {
            val reviewManager = RuStoreReviewManagerFactory.create(context)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    reviewManager.requestReviewFlow()
                        .addOnSuccessListener { reviewInfo ->
                            // Показываем диалог отзыва
                            reviewManager.launchReviewFlow(reviewInfo)
                                .addOnSuccessListener {
                                    Timber.d("Диалог отзыва успешно показан")
                                    prefs.edit().putLong(KEY_LAST_REVIEW_TIME, currentTime).apply()
                                }
                                .addOnFailureListener { e ->
                                    Timber.e(e, "Ошибка при показе диалога отзыва")
                                }
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "Ошибка при запросе диалога отзыва")
                        }
                } catch (e: Exception) {
                    Timber.e(e, "Исключение при запросе отзыва")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации RuStore Review API")
        }
    }

    /**
     * Проверяет наличие обновлений приложения через RuStore API
     * Проверка выполняется не чаще раза в 7 дней
     *
     * @param context Контекст приложения
     */
    fun checkForUpdates(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastUpdateCheckTime = prefs.getLong(KEY_LAST_UPDATE_CHECK_TIME, 0)
        val currentTime = System.currentTimeMillis()

        // Проверяем, прошло ли достаточно времени с последней проверки обновлений
        if (currentTime - lastUpdateCheckTime < UPDATE_CHECK_INTERVAL) {
            Timber.d("Слишком рано для проверки обновлений. Пропускаем.")
            return
        }

        try {
            // Проверяем, установлен ли RuStore на устройстве
            val appUpdateManager = RuStoreAppUpdateManagerFactory.create(context)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Запрашиваем информацию об обновлении
                    appUpdateManager.getAppUpdateInfo()
                        .addOnSuccessListener { appUpdateInfo ->
                            Timber.d("Проверка обновлений в RuStore завершена")
                            prefs.edit().putLong(KEY_LAST_UPDATE_CHECK_TIME, currentTime).apply()
                            
                            // Если доступно обновление, показываем диалог
                            if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                                Timber.d("Доступно обновление в RuStore")
                                // Создаем пустые опции обновления
                                val appUpdateOptions = ru.rustore.sdk.appupdate.model.AppUpdateOptions.Builder().build()
                                appUpdateManager.startUpdateFlow(appUpdateInfo, appUpdateOptions)
                                    .addOnSuccessListener {
                                        Timber.d("Процесс обновления запущен успешно")
                                    }
                                    .addOnFailureListener { e ->
                                        Timber.e(e, "Ошибка при запуске процесса обновления")
                                    }
                            } else {
                                Timber.d("Обновлений не найдено или они не доступны для установки")
                            }
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "Ошибка при проверке обновлений")
                        }
                } catch (e: Exception) {
                    Timber.e(e, "Исключение при проверке обновлений")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации RuStore AppUpdate API")
        }
    }
}
