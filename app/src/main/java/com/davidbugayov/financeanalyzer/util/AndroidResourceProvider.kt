package com.davidbugayov.financeanalyzer.util

import android.content.Context
import androidx.annotation.StringRes
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider

/**
 * Android-реализация ResourceProvider через Context
 */
class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(@StringRes id: Int, vararg args: Any?): String {
        return context.getString(id, *args)
    }
}
