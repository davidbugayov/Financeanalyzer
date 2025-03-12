package com.davidbugayov.financeanalyzer.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.davidbugayov.financeanalyzer.R

class AppColors(context: Context) {

    // Primary colors
    val md_theme_light_primary = Color(ContextCompat.getColor(context, R.color.primary_light))
    val md_theme_light_onPrimary = Color(ContextCompat.getColor(context, R.color.on_primary_light))
    val md_theme_light_primaryContainer = Color(ContextCompat.getColor(context, R.color.primary_container_light))
    val md_theme_light_onPrimaryContainer = Color(ContextCompat.getColor(context, R.color.on_primary_container_light))
    val md_theme_dark_primary = Color(ContextCompat.getColor(context, R.color.primary_dark))
    val md_theme_dark_onPrimary = Color(ContextCompat.getColor(context, R.color.on_primary_dark))
    val md_theme_dark_primaryContainer = Color(ContextCompat.getColor(context, R.color.primary_container_dark))
    val md_theme_dark_onPrimaryContainer = Color(ContextCompat.getColor(context, R.color.on_primary_container_dark))

    // Secondary colors
    val md_theme_light_secondary = Color(ContextCompat.getColor(context, R.color.secondary_light))
    val md_theme_light_onSecondary = Color(ContextCompat.getColor(context, R.color.on_secondary_light))
    val md_theme_light_secondaryContainer = Color(ContextCompat.getColor(context, R.color.secondary_container_light))
    val md_theme_light_onSecondaryContainer = Color(ContextCompat.getColor(context, R.color.on_secondary_container_light))
    val md_theme_dark_secondary = Color(ContextCompat.getColor(context, R.color.secondary_dark))
    val md_theme_dark_onSecondary = Color(ContextCompat.getColor(context, R.color.on_secondary_dark))
    val md_theme_dark_secondaryContainer = Color(ContextCompat.getColor(context, R.color.secondary_container_dark))
    val md_theme_dark_onSecondaryContainer = Color(ContextCompat.getColor(context, R.color.on_secondary_container_dark))

    // Tertiary colors
    val md_theme_light_tertiary = Color(ContextCompat.getColor(context, R.color.tertiary_light))
    val md_theme_light_onTertiary = Color(ContextCompat.getColor(context, R.color.on_tertiary_light))
    val md_theme_light_tertiaryContainer = Color(ContextCompat.getColor(context, R.color.tertiary_container_light))
    val md_theme_light_onTertiaryContainer = Color(ContextCompat.getColor(context, R.color.on_tertiary_container_light))
    val md_theme_dark_tertiary = Color(ContextCompat.getColor(context, R.color.tertiary_dark))
    val md_theme_dark_onTertiary = Color(ContextCompat.getColor(context, R.color.on_tertiary_dark))
    val md_theme_dark_tertiaryContainer = Color(ContextCompat.getColor(context, R.color.tertiary_container_dark))
    val md_theme_dark_onTertiaryContainer = Color(ContextCompat.getColor(context, R.color.on_tertiary_container_dark))

    // Error colors
    val md_theme_light_error = Color(ContextCompat.getColor(context, R.color.error_light))
    val md_theme_light_onError = Color(ContextCompat.getColor(context, R.color.on_error_light))
    val md_theme_light_errorContainer = Color(ContextCompat.getColor(context, R.color.error_container_light))
    val md_theme_light_onErrorContainer = Color(ContextCompat.getColor(context, R.color.on_error_container_light))
    val md_theme_dark_error = Color(ContextCompat.getColor(context, R.color.error_dark))
    val md_theme_dark_onError = Color(ContextCompat.getColor(context, R.color.on_error_dark))
    val md_theme_dark_errorContainer = Color(ContextCompat.getColor(context, R.color.error_container_dark))
    val md_theme_dark_onErrorContainer = Color(ContextCompat.getColor(context, R.color.on_error_container_dark))

    // Background colors
    val md_theme_light_background = Color(ContextCompat.getColor(context, R.color.background_light))
    val md_theme_light_onBackground = Color(ContextCompat.getColor(context, R.color.on_background_light))
    val md_theme_dark_background = Color(ContextCompat.getColor(context, R.color.background_dark))
    val md_theme_dark_onBackground = Color(ContextCompat.getColor(context, R.color.on_background_dark))

    // Surface colors
    val md_theme_light_surface = Color(ContextCompat.getColor(context, R.color.surface_light))
    val md_theme_light_onSurface = Color(ContextCompat.getColor(context, R.color.on_surface_light))
    val md_theme_light_surfaceVariant = Color(ContextCompat.getColor(context, R.color.surface_variant_light))
    val md_theme_light_onSurfaceVariant = Color(ContextCompat.getColor(context, R.color.on_surface_variant_light))
    val md_theme_dark_surface = Color(ContextCompat.getColor(context, R.color.surface_dark))
    val md_theme_dark_onSurface = Color(ContextCompat.getColor(context, R.color.on_surface_dark))
    val md_theme_dark_surfaceVariant = Color(ContextCompat.getColor(context, R.color.surface_variant_dark))
    val md_theme_dark_onSurfaceVariant = Color(ContextCompat.getColor(context, R.color.on_surface_variant_dark))

    // Outline
    val md_theme_light_outline = Color(ContextCompat.getColor(context, R.color.outline_light))
    val md_theme_dark_outline = Color(ContextCompat.getColor(context, R.color.outline_dark))

    // Income and Expense colors
    val md_theme_light_income = Color(ContextCompat.getColor(context, R.color.income_light))
    val md_theme_dark_income = Color(ContextCompat.getColor(context, R.color.income_dark))
    val md_theme_light_expense = Color(ContextCompat.getColor(context, R.color.expense_light))
    val md_theme_dark_expense = Color(ContextCompat.getColor(context, R.color.expense_dark))

    // Common colors
    val errorColor = Color(ContextCompat.getColor(context, R.color.error))
    val successColor = Color(ContextCompat.getColor(context, R.color.success))
    val warningColor = Color(ContextCompat.getColor(context, R.color.warning))
    val infoColor = Color(ContextCompat.getColor(context, R.color.info))
}