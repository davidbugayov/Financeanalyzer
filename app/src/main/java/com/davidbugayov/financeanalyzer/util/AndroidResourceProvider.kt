package com.davidbugayov.financeanalyzer.util

import android.annotation.SuppressLint
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
        // Use the context directly to preserve locale configuration set by AppCompatDelegate
        return if (args.isNotEmpty()) context.getString(id, *args) else context.getString(id)
    }

    @SuppressLint("DiscouragedApi")
    override fun getStringByName(
        name: String,
        vararg args: Any?,
    ): String {
        // Use the context directly to preserve locale configuration set by AppCompatDelegate
        val resourceId =
            try {
                context.resources.getIdentifier(name, "string", context.packageName)
            } catch (_: Exception) {
                0
            }
        return if (resourceId != 0) {
            if (args.isNotEmpty()) context.getString(resourceId, *args) else context.getString(resourceId)
        } else {
            name // fallback to name if resource not found
        }
    }
}
