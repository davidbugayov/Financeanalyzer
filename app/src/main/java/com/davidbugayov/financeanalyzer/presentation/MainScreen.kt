package com.davidbugayov.financeanalyzer.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.FinanceChartScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.libraries.LibrariesScreen
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileScreen
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(startDestination: String = "home") {
    val navController = rememberNavController()
    val layoutDirection = LocalLayoutDirection.current
    val homeViewModel: HomeViewModel = koinViewModel()
    val chartViewModel: ChartViewModel = koinViewModel()
    val addTransactionViewModel: AddTransactionViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    
    val themeState = profileViewModel.themeMode.collectAsState()
    val themeMode = themeState.value
    
    val view = LocalView.current

    // Определяем isDarkTheme здесь, за пределами LaunchedEffect
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Отслеживаем изменения темы
    LaunchedEffect(themeMode) {
        // Обновляем UI при изменении темы
        val window = (view.context as? Activity)?.window
        if (window != null) {
            // Устанавливаем прозрачность через WindowInsetsController вместо устаревших свойств
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)

            // Настраиваем иконки статус-бара и навигационной панели в зависимости от темы
            // Светлые иконки для темной темы, темные иконки для светлой темы
            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    // Применяем тему к всему приложению - теперь изменения themeMode будут автоматически
    // вызывать перерисовку всей композиции с новой темой
    FinanceAnalyzerTheme(themeMode = themeMode) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize() // Растягиваем фон на весь экран
        ) { paddingValues ->
            NavHost(
                navController = navController, 
                startDestination = startDestination,
                modifier = Modifier.padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection)
                )
            ) {
                composable(
                    route = Screen.Home.route,
                    enterTransition = {
                        fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    popEnterTransition = {
                        fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    }
                ) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToHistory = { navController.navigate(Screen.History.route) },
                        onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                        onNavigateToChart = { navController.navigate(Screen.Chart.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                    )
                }
                
                composable(
                    route = Screen.History.route,
                    enterTransition = {
                        slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        ) + fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        ) + fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        ) + fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        ) + fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    }
                ) {
                    TransactionHistoryScreen(
                        viewModel = koinViewModel<TransactionHistoryViewModel>(),
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                
                composable(
                    route = Screen.AddTransaction.route,
                    enterTransition = {
                        slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Up
                        ) + fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Down
                        ) + fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    }
                ) {
                    AddTransactionScreen(
                        viewModel = addTransactionViewModel,
                        onNavigateBack = {
                            homeViewModel.onEvent(com.davidbugayov.financeanalyzer.presentation.home.event.HomeEvent.LoadTransactions)
                            chartViewModel.loadTransactions()
                            profileViewModel.updateFinancialStatistics()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
                
                composable(
                    route = Screen.Chart.route,
                    enterTransition = {
                        slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        ) + fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        ) + fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        ) + fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        ) + fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    }
                ) {
                    FinanceChartScreen(
                        viewModel = chartViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(
                    route = Screen.Profile.route,
                    enterTransition = {
                        fadeIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    },
                    exitTransition = {
                        fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    }
                ) {
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLibraries = { navController.navigate(Screen.Libraries.route) },
                        onNavigateToChart = { navController.navigate(Screen.Chart.route) }
                    )
                }
                
                composable(
                    route = Screen.Libraries.route,
                    enterTransition = {
                        slideIntoContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.Start
                        ) + fadeIn(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        ) + fadeOut(
                            animationSpec = tween(300, easing = EaseInOut)
                        )
                    }
                ) {
                    LibrariesScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}