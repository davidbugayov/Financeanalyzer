package com.davidbugayov.financeanalyzer.util

import android.content.Context
import androidx.annotation.StringRes
import com.davidbugayov.financeanalyzer.core.util.ResourceProvider

/**
 * Android-реализация ResourceProvider через Context
 */
class AndroidResourceProvider(
    private val context: Context,
) : ResourceProvider {
    override fun getString(
        @StringRes id: Int,
        vararg args: Any?,
    ): String {
        val ctx = context.applicationContext
        return if (args.isNotEmpty()) ctx.getString(id, *args) else ctx.getString(id)
    }
    
    override fun getStringByName(
        name: String,
        vararg args: Any?,
    ): String {
        val ctx = context.applicationContext
        val resourceId = ctx.resources.getIdentifier(name, "string", ctx.packageName)
        return if (resourceId != 0) {
            if (args.isNotEmpty()) ctx.getString(resourceId, *args) else ctx.getString(resourceId)
        } else {
            name // fallback to name if resource not found
        }
    }
}
