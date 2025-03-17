package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.davidbugayov.financeanalyzer.R
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen

/**
 * Нижняя навигационная панель приложения.
 * @param currentRoute Текущий маршрут.
 * @param onNavigate Обработчик навигации.
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) },
            icon = { Icon(painter = painterResource(id = R.drawable.ic_home), contentDescription = null) },
            label = { Text("Главная") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = { onNavigate(Screen.History.route) },
            icon = { Icon(painter = painterResource(id = R.drawable.ic_history), contentDescription = null) },
            label = { Text("История") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Screen.Chart.route,
            onClick = { onNavigate(Screen.Chart.route) },
            icon = { Icon(painter = painterResource(id = R.drawable.ic_chart), contentDescription = null) },
            label = { Text("Графики") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) },
            icon = { Icon(painter = painterResource(id = R.drawable.ic_profile), contentDescription = null) },
            label = { Text("Профиль") }
        )
    }
} 