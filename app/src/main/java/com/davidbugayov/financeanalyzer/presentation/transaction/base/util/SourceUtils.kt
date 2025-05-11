package com.davidbugayov.financeanalyzer.presentation.transaction.base.util

import android.content.res.Resources
import androidx.compose.ui.graphics.toArgb
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.ui.theme.CashColor
import com.davidbugayov.financeanalyzer.utils.ColorUtils

// import com.davidbugayov.financeanalyzer.utils.ColorUtils // ColorUtils может быть еще нужен для getSourceColorByName, если используется где-то еще

fun getInitialSources(sourcePreferences: SourcePreferences, resources: Resources): List<Source> {
    val savedSources = sourcePreferences.getCustomSources()
    return if (savedSources.isNotEmpty()) {
        savedSources
    } else {
        val defaultSourceNames = resources.getStringArray(R.array.default_source_names).toList()

        val defaultSources = defaultSourceNames.map { name ->
            val colorObject = ColorUtils.getSourceColorByName(name.lowercase()) ?: CashColor
            Source(name = name, color = colorObject.toArgb())
        }
        sourcePreferences.saveCustomSources(defaultSources)
        defaultSources
    }
}

fun addCustomSource(
    sourcePreferences: SourcePreferences,
    currentSources: List<Source>,
    newSource: Source
): List<Source> {
    val updatedSources = (currentSources + newSource).distinctBy { it.name }
    sourcePreferences.saveCustomSources(updatedSources)
    return updatedSources
} 