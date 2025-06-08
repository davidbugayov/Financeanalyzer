package com.davidbugayov.financeanalyzer.utils

import android.content.Context
import timber.log.Timber
import ru.rustore.sdk.review.RuStoreReviewManagerFactory
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.UpdateAvailability

/**
 * Утилиты для работы с RuStore SDK
 */
object RuStoreUtils {
    /**
     * Запускает диалог отзыва в RuStore, если это возможно
     */
    fun requestReview(context: Context) {
        try {
            if (context.packageManager.getLaunchIntentForPackage("ru.vk.store") != null) {
                val reviewManager = RuStoreReviewManagerFactory.create(context)
                reviewManager.requestReviewFlow()
                    .addOnSuccessListener { reviewInfo ->
                        reviewManager.launchReviewFlow(reviewInfo)
                            .addOnSuccessListener {
                                Timber.d("RuStore review launched successfully")
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Failed to launch RuStore review")
                            }
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Failed to request RuStore review")
                    }
            } else {
                Timber.d("RuStore app not installed")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error when trying to launch RuStore review")
        }
    }

    /**
     * Проверяет наличие обновлений в RuStore
     */
    fun checkForUpdates(context: Context) {
        try {
            val appUpdateManager = RuStoreAppUpdateManagerFactory.create(context)
            appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailable()) {
                        Timber.d("RuStore update available")
                        // Можно добавить логику для показа обновления
                    }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Failed to check for RuStore updates")
                }
        } catch (e: Exception) {
            Timber.e(e, "Error when trying to check for RuStore updates")
        }
    }
    
    /**
     * Метод расширения для проверки наличия обновления
     */
    private fun AppUpdateInfo.updateAvailable(): Boolean {
        return updateAvailability == UpdateAvailability.UPDATE_AVAILABLE
    }
} 