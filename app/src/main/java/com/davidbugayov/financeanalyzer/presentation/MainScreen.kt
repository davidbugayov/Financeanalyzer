package com.davidbugayov.financeanalyzer.presentation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.FinanceChartScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryScreen
import com.davidbugayov.financeanalyzer.presentation.history.TransactionHistoryViewModel
import com.davidbugayov.financeanalyzer.presentation.home.HomeScreen
import com.davidbugayov.financeanalyzer.presentation.home.HomeViewModel
import com.davidbugayov.financeanalyzer.presentation.import_transaction.ImportTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.libraries.LibrariesScreen
import com.davidbugayov.financeanalyzer.presentation.navigation.Screen
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingScreen
import com.davidbugayov.financeanalyzer.presentation.onboarding.OnboardingViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileScreen
import com.davidbugayov.financeanalyzer.presentation.profile.ProfileViewModel
import com.davidbugayov.financeanalyzer.presentation.profile.model.ThemeMode
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import com.davidbugayov.financeanalyzer.utils.ColorUtils
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionScreen

/**
 * Главный экран приложения, содержащий навигацию между различными разделами.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(startDestination: String = "home") {
    val navController = rememberNavController()
    val layoutDirection = LocalLayoutDirection.current
    val homeViewModel: HomeViewModel = koinViewModel()
    val chartViewModel: ChartViewModel = koinViewModel()
    val addTransactionViewModel: AddTransactionViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val onboardingViewModel: OnboardingViewModel = koinViewModel()
    val context = LocalContext.current

    // Состояние для предварительной настройки ViewModel для добавления транзакции
    var fromWallet by remember { mutableStateOf(false) }
    var walletToAddTransaction by remember { mutableStateOf<Wallet?>(null) }

    // Функция для настройки уведомлений, определена локально
    fun setupNotifications() {
        try {
            val notificationScheduler = NotificationScheduler()
            notificationScheduler.scheduleTransactionReminder(
                context,
                20,
                0
            ) // По умолчанию в 20:00
            Timber.d("Transaction reminders scheduled after permission granted")
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule transaction reminders")
        }
    }
    
    // Проверяем, нужно ли показывать онбординг
    var shouldShowOnboarding by remember { mutableStateOf(!onboardingViewModel.isOnboardingCompleted()) }

    // Состояние для диалогов разрешений
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Лаунчер для запроса разрешений на уведомления
    val permissionLauncher = PermissionUtils.rememberNotificationPermissionLauncher { isGranted ->
        if (isGranted) {
            // Разрешение предоставлено, настраиваем уведомления
            setupNotifications()
            Timber.d("Notification permission granted after onboarding")
        } else {
            // Пользователь отказал, показываем диалог с предложением перейти в настройки
            showSettingsDialog = true
            Timber.d("Notification permission denied after onboarding")
        }
    }

    // Диалог с предложением перейти в настройки (когда пользователь отказал в разрешении)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Разрешение отклонено") },
            text = { Text("Для получения уведомлений о транзакциях необходимо разрешение. Вы можете включить его в настройках приложения.") },
            confirmButton = {
                Button(onClick = {
                    PermissionUtils.openNotificationSettings(context)
                    showSettingsDialog = false
                }) {
                    Text("Открыть настройки")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог запроса разрешений
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Нужно разрешение") },
            text = { Text("Для получения уведомлений о транзакциях требуется ваше разрешение.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    // Запрашиваем разрешение напрямую
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Для более старых версий разрешение не требуется
                        setupNotifications()
                    }
                }) {
                    Text("Запросить разрешение")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    val themeState = profileViewModel.themeMode.collectAsState()
    val themeMode = themeState.value
    
    val view = LocalView.current

    // Определяем isDarkTheme здесь, за пределами LaunchedEffect
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Для загрузки данных в фоновом режиме
    LaunchedEffect(Unit) {
        Timber.d("MainScreen: Планируем фоновую инициализацию данных")
        
        // Запускаем единую фоновую задачу для инициализации данных после небольшой задержки
        MainScope().launch(Dispatchers.Default) {
            delay(500) // Даем время UI на отрисовку
            Timber.d("MainScreen: Запускаем initiateBackgroundDataRefresh")
            homeViewModel.initiateBackgroundDataRefresh()
            
            // Загружаем данные для графиков с большей задержкой, чтобы не мешать основному экрану
            delay(2000) 
            Timber.d("MainScreen: Запускаем загрузку данных для графиков")
            chartViewModel.loadTransactions()
        }
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

    // Применяем тему к всему приложению
    FinanceAnalyzerTheme(themeMode = themeMode) {
        if (shouldShowOnboarding) {
            // Показываем экран онбординга
            OnboardingScreen(
                onFinish = {
                    // Отмечаем онбординг как завершенный
                    onboardingViewModel.completeOnboarding()
                    shouldShowOnboarding = false

                    // После завершения онбординга запрашиваем разрешение на уведомления
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !PermissionUtils.hasNotificationPermission(context)
                    ) {
                        showPermissionDialog = true
                    } else {
                        // Для старых версий просто настраиваем уведомления
                        setupNotifications()
                    }
                }
            )
        } else {
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
                            onNavigateToAdd = {
                                // Сбрасываем состояние
                                addTransactionViewModel.resetToDefaultState()
                                // Проверяем параметры из HomeScreen и запоминаем, что переход с другого экрана
                                fromWallet = false
                                walletToAddTransaction = null
                                // Переходим на экран добавления
                                navController.navigate(Screen.AddTransaction.route)
                            },
                            onNavigateToEdit = { transaction ->
                                // Вместо вызова loadTransactionForEditing используем навигацию с ID транзакции
                                navController.navigate(Screen.EditTransaction.createRoute(transaction))
                            },
                            onNavigateToChart = { navController.navigate(Screen.Chart.route) },
                            onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                            onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                            onNavigateToWallets = { navController.navigate(Screen.Wallets.route) }
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
                            onNavigateBack = { navController.navigateUp() },
                            onNavigateToEdit = { transaction ->
                                // Вместо вызова loadTransactionForEditing используем навигацию с ID транзакции
                                navController.navigate(Screen.EditTransaction.createRoute(transaction.id))
                            }
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
                        // Предустановка параметров кошелька, если открыто из экрана кошелька
                        LaunchedEffect(Unit) {
                            if (fromWallet) {
                                try {
                                    val wallet = walletToAddTransaction
                                    if (wallet != null) {
                                        Timber.d("Настройка параметров из экрана кошелька: wallet=${wallet.name}, id=${wallet.id}")
                                        
                                        // Инициализируем ToggleAddToWallet с параметром
                                        addTransactionViewModel.onEvent(BaseTransactionEvent.ToggleAddToWallet(true))
                                        
                                        // И для выбора кошелька используем корректное событие
                                        addTransactionViewModel.onEvent(BaseTransactionEvent.ToggleWalletSelection(wallet.id))
                                        
                                        Timber.d("После настройки из HomeScreen: isExpense=${addTransactionViewModel.state.value.transactionData.isExpense}, forceExpense=${addTransactionViewModel.state.value.forceExpense}, walletState=${addTransactionViewModel.state.value.walletState}")
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Ошибка при настройке параметров кошельков")
                                }
                            }
                        }
                        
                        AddTransactionScreen(
                            onBackClick = {
                                // Используем фоновую загрузку вместо полной перезагрузки
                                homeViewModel.initiateBackgroundDataRefresh()
                                
                                // Обновляем графики и статистику напрямую
                                chartViewModel.loadTransactions()
                                profileViewModel.updateFinancialStatistics()

                                // Просто возвращаемся назад, чтобы вернуться на предыдущий экран
                                navController.navigateUp()
                            }
                        )
                    }
                    
                    // Маршрут для экрана редактирования транзакции
                    composable(
                        route = Screen.EditTransaction.route,
                        arguments = listOf(
                            navArgument("transactionId") { type = NavType.StringType }
                        ),
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
                        val transactionId = it.arguments?.getString("transactionId") ?: ""
                        
                        EditTransactionScreen(
                            transactionId = transactionId,
                            onBackClick = {
                                // Используем фоновую загрузку вместо полной перезагрузки
                                homeViewModel.initiateBackgroundDataRefresh()
                                
                                // Обновляем графики и статистику напрямую
                                chartViewModel.loadTransactions()
                                profileViewModel.updateFinancialStatistics()

                                // Возвращаемся назад
                                navController.navigateUp()
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
                            onNavigateToChart = { navController.navigate(Screen.Chart.route) },
                            onNavigateToImport = { navController.navigate(Screen.ImportTransactions.route) }
                        )
                    }

                    composable(
                        route = Screen.ImportTransactions.route,
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
                        ImportTransactionsScreen(
                            onNavigateBack = { navController.popBackStack() }
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

                    // Экран бюджета
                    composable(
                        route = Screen.Budget.route,
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
                        BudgetScreen(
                            navController = navController,
                            onNavigateBack = { navController.navigateUp() },
                            onNavigateToTransactions = { categoryId ->
                                navController.navigate(
                                    Screen.WalletTransactions.createRoute(
                                        categoryId
                                    )
                                )
                            }
                        )
                    }

                    // Экран транзакций бюджетной категории
                    composable(
                        route = Screen.WalletTransactions.route,
                        arguments = listOf(
                            navArgument("walletId") { type = NavType.StringType }
                        ),
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
                    ) { backStackEntry ->
                        val walletId =
                            backStackEntry.arguments?.getString("walletId") ?: return@composable
                        WalletTransactionsScreen(
                            walletId = walletId,
                            onNavigateBack = { navController.navigateUp() },
                            addTransactionViewModel = addTransactionViewModel,
                            navController = navController
                        )
                    }
                    
                    // Экран кошельков (новый)
                    composable(
                        route = Screen.Wallets.route,
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
                        BudgetScreen( // Временно используем старый экран с новым ViewModel
                            navController = navController,
                            onNavigateBack = { navController.navigateUp() },
                            onNavigateToTransactions = { walletId ->
                                navController.navigate(
                                    Screen.WalletTransactions.createRoute(
                                        walletId
                                    )
                                )
                            },
                            viewModel = koinViewModel<BudgetViewModel>(),
                            addTransactionViewModel = addTransactionViewModel
                        )
                    }

                    // Экран транзакций кошелька (новый)
                    composable(
                        route = Screen.WalletTransactions.route,
                        arguments = listOf(
                            navArgument("walletId") { type = NavType.StringType }
                        ),
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
                    ) { backStackEntry ->
                        val walletId =
                            backStackEntry.arguments?.getString("walletId") ?: return@composable
                        WalletTransactionsScreen(
                            walletId = walletId,
                            onNavigateBack = { navController.navigateUp() },
                            addTransactionViewModel = addTransactionViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}