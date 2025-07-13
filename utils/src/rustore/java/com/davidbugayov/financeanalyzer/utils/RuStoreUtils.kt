package com.davidbugayov.financeanalyzer.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.review.RuStoreReviewManagerFactory
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils

/**
 * Utilities for working with RuStore SDK.
 * Real implementation for the rustore flavor.
 */
object RuStoreUtils {
    private const val PREFS_NAME = "rustore_utils_prefs"
    private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
    private const val KEY_LAST_REVIEW_REQUEST = "last_review_request"
    private const val UPDATE_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
    private const val REVIEW_REQUEST_INTERVAL = 7 * 24 * 60 * 60 * 1000L // 7 days

    /**
     * Checks for updates in RuStore
     */
    fun checkForUpdates(context: Context) {
        AnalyticsUtils.logEvent("rustore_update_check_started")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0)
        val now = System.currentTimeMillis()

        if (now - lastCheck < UPDATE_CHECK_INTERVAL) {
            Timber.d("Skipping update check: last check was less than 24h ago")
            AnalyticsUtils.logEvent("rustore_update_check_skipped")
            return
        }

        try {
            val appUpdateManager = RuStoreAppUpdateManagerFactory.create(context)
            appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener { appUpdateInfo ->
                    try {
                        if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                            Timber.d("Update available in RuStore")
                            AnalyticsUtils.logEvent("rustore_update_available")
                            // TODO: show update UI or trigger auto-update
                        } else {
                            Timber.d("No updates found in RuStore")
                            AnalyticsUtils.logEvent("rustore_no_update")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error handling update result")
                        AnalyticsUtils.logEvent("rustore_update_check_error")
                    }
                    prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, now).apply()
                }
                .addOnFailureListener { throwable ->
                    Timber.e(throwable, "Failed to check updates in RuStore")
                    AnalyticsUtils.logEvent("rustore_update_check_failed")
                    prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, now).apply()
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize RuStore update check")
            AnalyticsUtils.logEvent("rustore_update_check_failed")
        }
    }

    /**
     * Requests an app review via RuStore
     */
    fun requestReview(activity: Activity) {
        AnalyticsUtils.logEvent("rustore_review_requested")
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastRequest = prefs.getLong(KEY_LAST_REVIEW_REQUEST, 0)
        val now = System.currentTimeMillis()

        if (now - lastRequest < REVIEW_REQUEST_INTERVAL) {
            Timber.d("Skipping review request: last request was less than 7d ago")
            AnalyticsUtils.logEvent("rustore_review_skipped")
            return
        }

        try {
            val reviewManager = RuStoreReviewManagerFactory.create(activity)
            reviewManager.requestReviewFlow()
                .addOnSuccessListener { reviewInfo ->
                    try {
                        reviewManager.launchReviewFlow(reviewInfo)
                            .addOnSuccessListener {
                                Timber.d("RuStore review flow launched")
                                AnalyticsUtils.logEvent("rustore_review_flow_launched")
                                prefs.edit().putLong(KEY_LAST_REVIEW_REQUEST, now).apply()
                            }
                            .addOnFailureListener { throwable ->
                                Timber.e(throwable, "Failed to launch RuStore review flow")
                                AnalyticsUtils.logEvent("rustore_review_flow_failed")
                            }
                    } catch (e: Exception) {
                        Timber.e(e, "Error during review flow launch")
                        AnalyticsUtils.logEvent("rustore_review_flow_error")
                    }
                }
                .addOnFailureListener { throwable ->
                    Timber.e(throwable, "Failed to request RuStore review")
                    AnalyticsUtils.logEvent("rustore_review_request_failed")
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize RuStore review request")
            AnalyticsUtils.logEvent("rustore_review_request_failed")
        }
    }
} 