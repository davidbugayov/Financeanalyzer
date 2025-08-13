package com.davidbugayov.financeanalyzer.feature.transaction.base.util

import android.content.res.Resources
import androidx.compose.ui.graphics.toArgb
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Source
import com.davidbugayov.financeanalyzer.ui.R as UiR
import com.davidbugayov.financeanalyzer.ui.theme.CashColor
import com.davidbugayov.financeanalyzer.utils.ColorUtils

// import com.davidbugayov.financeanalyzer.utils.ColorUtils // ColorUtils может быть еще нужен для getSourceColorByName, если используется где-то еще

fun getInitialSources(
    sourcePreferences: SourcePreferences,
    resources: Resources,
): List<Source> {
    val deletedDefaultSources = sourcePreferences.getDeletedDefaultSources()
    val defaultSourceNames = resources.getStringArray(UiR.array.default_source_names).toList()
    val defaultSources =
        defaultSourceNames
            .filter { it !in deletedDefaultSources }
            .map { name ->
                val colorObject = ColorUtils.getSourceColorByName(name.lowercase()) ?: CashColor
                Source(name = name, color = colorObject.toArgb())
            }
    val savedSources =
        sourcePreferences.getCustomSources().map { customSource ->
            Source(name = customSource.name, color = ColorUtils.parseHexColor(customSource.colorHex), isCustom = true)
        }
    // Объединяем дефолтные и кастомные, убираем дубликаты по имени
    val allSources = (defaultSources + savedSources).distinctBy { it.name }
    return allSources
}

fun addCustomSource(
    sourcePreferences: SourcePreferences,
    currentSources: List<Source>,
    newSource: Source,
): List<Source> {
    val updatedSources = (currentSources + newSource).distinctBy { it.name }

    // Преобразуем Source в CustomSourceData перед сохранением
    val customSourceData =
        updatedSources.map { source ->
            SourcePreferences.CustomSourceData(
                name = source.name,
                colorHex = ColorUtils.colorToHex(source.color),
            )
        }
    sourcePreferences.saveCustomSources(customSourceData)

    return updatedSources
}
