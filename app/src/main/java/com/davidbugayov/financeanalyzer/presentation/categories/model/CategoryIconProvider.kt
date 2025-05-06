package com.davidbugayov.financeanalyzer.presentation.categories.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconProvider {
    fun getIconByName(name: String): ImageVector = when (name) {
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Restaurant" -> Icons.Default.Restaurant
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Movie" -> Icons.Default.Movie
        "LocalHospital" -> Icons.Default.LocalHospital
        "Checkroom" -> Icons.Default.Checkroom
        "Home" -> Icons.Default.Home
        "Phone" -> Icons.Default.Phone
        "Pets" -> Icons.Default.Pets
        "Payments" -> Icons.Default.Payments
        "CreditCard" -> Icons.Default.CreditCard
        "Work" -> Icons.Default.Work
        "SwapHoriz" -> Icons.Default.SwapHoriz
        "MoreHoriz" -> Icons.Default.MoreHoriz
        "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
        "Add" -> Icons.Default.Add
        else -> Icons.Default.Category
    }

    fun getIconName(icon: ImageVector?): String = when (icon) {
        Icons.Filled.ShoppingCart -> "ShoppingCart"
        Icons.Filled.Restaurant -> "Restaurant"
        Icons.Filled.DirectionsCar -> "DirectionsCar"
        Icons.Filled.Movie -> "Movie"
        Icons.Filled.LocalHospital -> "LocalHospital"
        Icons.Filled.Checkroom -> "Checkroom"
        Icons.Filled.Home -> "Home"
        Icons.Filled.Phone -> "Phone"
        Icons.Filled.Pets -> "Pets"
        Icons.Filled.Payments -> "Payments"
        Icons.Filled.CreditCard -> "CreditCard"
        Icons.Filled.Work -> "Work"
        Icons.Filled.SwapHoriz -> "SwapHoriz"
        Icons.Filled.MoreHoriz -> "MoreHoriz"
        Icons.AutoMirrored.Filled.TrendingUp -> "TrendingUp"
        Icons.Filled.Add -> "Add"
        else -> "ShoppingCart"
    }
} 