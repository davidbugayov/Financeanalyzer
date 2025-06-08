package ru.rustore.sdk.review

import android.content.Context

/**
 * Заглушка для RuStoreReviewManagerFactory
 */
object RuStoreReviewManagerFactory {
    fun create(context: Context): RuStoreReviewManager {
        return RuStoreReviewManager()
    }
} 