package com.davidbugayov.financeanalyzer.util

import android.content.Context
import androidx.annotation.StringRes
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider

/**
 * Android-реализация ResourceProvider через Context
 */
class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(@StringRes id: Int, vararg args: Any?): String {
        return if (args.isNotEmpty()) context.getString(id, *args) else context.getString(id)
    }
}
