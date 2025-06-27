package com.davidbugayov.financeanalyzer.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.review.RuStoreReviewManagerFactory
import timber.log.Timber

/**
 * Утилиты для работы с RuStore SDK.
 * Реализация для rustore flavor.
 */
object RuStoreUtils {
    private const val PREFS_NAME = "rustore_utils_prefs"
    private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
    private const val KEY_LAST_REVIEW_REQUEST = "last_review_request"
    private const val UPDATE_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24 часа
    private const val REVIEW_REQUEST_INTERVAL = 7 * 24 * 60 * 60 * 1000L // 7 дней

    /**
     * Проверяет наличие обновлений в RuStore
     * @param context контекст приложения
     */
    fun checkForUpdates(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0)
        val now = System.currentTimeMillis()

        // Проверяем обновления не чаще чем раз в сутки
        if (now - lastCheck < UPDATE_CHECK_INTERVAL) {
            Timber.d("Пропуск проверки обновлений: последняя проверка была менее 24 часов назад")
            return
        }

        try {
            val appUpdateManager = RuStoreAppUpdateManagerFactory.create(context)
            appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener { appUpdateInfo ->
                    try {
                        if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                            Timber.d("Доступно обновление в RuStore")
                            // Здесь можно добавить логику для отображения диалога обновления
                            // или автоматического обновления
                        } else {
                            Timber.d("Обновлений в RuStore не найдено")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при обработке результата проверки обновлений")
                    }
                    
                    // Сохраняем время последней проверки
                    prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, now).apply()
                }
                .addOnFailureListener { throwable ->
                    Timber.e(throwable, "Ошибка при проверке обновлений в RuStore")
                    // Сохраняем время последней проверки даже при ошибке,
                    // чтобы не спамить запросами при возникновении проблем
                    prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, now).apply()
                }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации проверки обновлений RuStore")
        }
    }

    /**
     * Запрашивает у пользователя оценку приложения в RuStore
     * @param activity активность, из которой запрашивается оценка
     */
    fun requestReview(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastRequest = prefs.getLong(KEY_LAST_REVIEW_REQUEST, 0)
        val now = System.currentTimeMillis()

        // Запрашиваем оценку не чаще чем раз в неделю
        if (now - lastRequest < REVIEW_REQUEST_INTERVAL) {
            Timber.d("Пропуск запроса оценки: последний запрос был менее 7 дней назад")
            return
        }

        try {
            val reviewManager = RuStoreReviewManagerFactory.create(activity)
            reviewManager.requestReviewFlow()
                .addOnSuccessListener { reviewInfo ->
                    try {
                        reviewManager.launchReviewFlow(reviewInfo)
                            .addOnSuccessListener {
                                Timber.d("Запрос на оценку в RuStore выполнен")
                                // Сохраняем время последнего запроса оценки
                                prefs.edit().putLong(KEY_LAST_REVIEW_REQUEST, now).apply()
                            }
                            .addOnFailureListener { throwable ->
                                Timber.e(throwable, "Ошибка при запуске формы оценки в RuStore")
                            }
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка при обработке запуска формы оценки")
                    }
                }
                .addOnFailureListener { throwable ->
                    Timber.e(throwable, "Ошибка при запросе оценки в RuStore")
                }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при инициализации запроса оценки RuStore")
        }
    }
} 