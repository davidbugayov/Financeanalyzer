package com.davidbugayov.financeanalyzer.presentation.transaction.base.util

import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.utils.ColorUtils

fun getInitialSources(sourcePreferences: SourcePreferences): List<Source> {
    val savedSources = sourcePreferences.getCustomSources()
    return if (savedSources.isNotEmpty()) savedSources else ColorUtils.defaultSources
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

fun deleteCustomSource(
    sourcePreferences: SourcePreferences,
    currentSources: List<Source>,
    sourceName: String
): List<Source> {
    val updatedSources = currentSources.filter { it.name != sourceName }
    sourcePreferences.saveCustomSources(updatedSources)
    return updatedSources
} 