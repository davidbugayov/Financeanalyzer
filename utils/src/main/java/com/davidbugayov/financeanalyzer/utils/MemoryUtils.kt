package com.davidbugayov.financeanalyzer.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Debug
import com.davidbugayov.financeanalyzer.shared.analytics.AnalyticsConstants
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.PerformanceMetrics
import timber.log.Timber

/**
 * Утилиты для работы с памятью приложения
 */
object MemoryUtils {
    /**
     * Получить информацию о памяти устройства и приложения
     * @param context Контекст приложения
     * @return Информация о памяти (использовано МБ, всего МБ, доступно МБ)
     */
    fun getMemoryInfo(context: Context): Triple<Long, Long, Long> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableMemoryMB = memoryInfo.availMem / (1024 * 1024)
        val totalMemoryMB = memoryInfo.totalMem / (1024 * 1024)
        val usedMemoryMB = totalMemoryMB - availableMemoryMB

        return Triple(usedMemoryMB, totalMemoryMB, availableMemoryMB)
    }

    /**
     * Получить информацию о нативной куче приложения
     * @return Пара (размер кучи в МБ, выделено в куче в МБ)
     */
    fun getNativeHeapInfo(): Pair<Long, Long> {
        val nativeHeapSizeMB = Debug.getNativeHeapSize() / (1024 * 1024)
        val nativeHeapAllocatedMB = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)

        return Pair(nativeHeapSizeMB, nativeHeapAllocatedMB)
    }

    /**
     * Отслеживать использование памяти и отправлять данные в аналитику
     * @param context Контекст приложения
     */
    fun trackMemoryUsage(context: Context) {
        val (usedMemoryMB, totalMemoryMB, availableMemoryMB) = getMemoryInfo(context)
        val (nativeHeapSizeMB, nativeHeapAllocatedMB) = getNativeHeapInfo()

        val percentUsed = (usedMemoryMB.toFloat() / totalMemoryMB.toFloat()) * 100

        Timber.d("Memory usage: $usedMemoryMB MB / $totalMemoryMB MB ($percentUsed%)")
        Timber.d("Native heap: $nativeHeapAllocatedMB MB / $nativeHeapSizeMB MB")

        // Отправляем базовую информацию о памяти через PerformanceMetrics
        PerformanceMetrics.trackMemoryUsage(usedMemoryMB, totalMemoryMB, availableMemoryMB)

        // Отправляем расширенную информацию о памяти напрямую
        val params =
            Bundle().apply {
                putLong(AnalyticsConstants.Params.MEMORY_USAGE_MB, usedMemoryMB)
                putLong(AnalyticsConstants.Params.MEMORY_TOTAL, totalMemoryMB)
                putLong(AnalyticsConstants.Params.MEMORY_AVAILABLE, availableMemoryMB)
                putFloat("memory_percent_used", percentUsed)
                putLong("native_heap_size_mb", nativeHeapSizeMB)
                putLong("native_heap_allocated_mb", nativeHeapAllocatedMB)
            }

        AnalyticsUtils.logEvent(AnalyticsConstants.Events.MEMORY_USAGE, params)
    }

    /**
     * Проверить, достаточно ли памяти для выполнения операции
     * @param context Контекст приложения
     * @param requiredMemoryMB Требуемый объем памяти в МБ
     * @return true, если достаточно памяти, иначе false
     */
    fun hasEnoughMemory(
        context: Context,
        requiredMemoryMB: Long,
    ): Boolean {
        val (_, _, availableMemoryMB) = getMemoryInfo(context)
        return availableMemoryMB >= requiredMemoryMB
    }

    /**
     * Запросить сборку мусора
     * Примечание: это лишь предложение для системы, не гарантирует выполнение
     */
    fun requestGarbageCollection() {
        System.gc()
        Runtime.getRuntime().gc()
    }
}
