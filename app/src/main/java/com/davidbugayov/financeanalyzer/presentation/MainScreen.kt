package com.davidbugayov.financeanalyzer.presentation

import android.Manifest
import android.app.Activity
import android.os.Build
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.add.AddTransactionViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetScreen
import com.davidbugayov.financeanalyzer.presentation.budget.BudgetViewModel
import com.davidbugayov.financeanalyzer.presentation.budget.wallet.WalletTransactionsScreen
import com.davidbugayov.financeanalyzer.presentation.chart.ChartViewModel
import com.davidbugayov.financeanalyzer.presentation.chart.FinanceChartScreen
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.model.EditTransactionScreen
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
import com.davidbugayov.financeanalyzer.presentation.transaction.edit.EditTransactionViewModel
import com.davidbugayov.financeanalyzer.ui.theme.FinanceAnalyzerTheme
import com.davidbugayov.financeanalyzer.utils.NotificationScheduler
import com.davidbugayov.financeanalyzer.utils.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

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
                            addTransactionViewModel = addTransactionViewModel,
                            onNavigateToHistory = { navController.navigate(Screen.History.route) },
                            onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                            onNavigateToChart = { navController.navigate(Screen.Chart.route) },
                            onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                            onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                            onNavigateToWallets = { navController.navigate(Screen.Wallets.route) },
                            onNavigateToEdit = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) }
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
                            addTransactionViewModel = addTransactionViewModel,
                            onNavigateBack = { navController.navigateUp() },
                            navController = navController
                        )
                    }
                    
                    composable(
                        route = Screen.AddTransaction.route,
                        enterTransition = {
                            fadeIn(
                                animationSpec = tween(300, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            fadeOut(
                                animationSpec = tween(300, easing = EaseInOut)
                            )
                        }
                    ) {
                        // Получаем предыдущий маршрут из стека навигации, чтобы определить источник перехода
                        val previousRoute = navController.previousBackStackEntry?.destination?.route
                        
                        // Добавляем подробное логирование
                        Timber.d("Переход на AddTransaction с экрана: $previousRoute")
                        
                        // Проверяем, приходим ли мы с экрана BudgetScreen, WalletTransactions или Wallets
                        if (previousRoute == Screen.Budget.route || previousRoute == Screen.Wallets.route) {
                            // Если переход был из бюджета или кошельков
                            LaunchedEffect(Unit) {
                                // Проверяем текущее состояние типа транзакции
                                val currentIsExpense = addTransactionViewModel.state.value.isExpense
                                val currentForceExpense = addTransactionViewModel.state.value.forceExpense
                                
                                // Используем storedTargetWalletId из AddTransactionViewModel
                                val storedWalletId = addTransactionViewModel.storedTargetWalletId
                                val shouldDistribute = addTransactionViewModel.autoDistributeIncome
                                
                                Timber.d("Параметры перехода: storedWalletId=$storedWalletId, shouldDistribute=$shouldDistribute, currentIsExpense=$currentIsExpense, currentForceExpense=$currentForceExpense")
                                
                                // Если уже установлен расход через forceExpense, сохраняем этот режим
                                if (currentIsExpense && currentForceExpense) {
                                    Timber.d("=== Переход из Budget/Wallets - сохраняем режим РАСХОДА ===")
                                    // Ничего не меняем, оставляем расход как есть
                                } else {
                                    // Иначе устанавливаем режим дохода (по умолчанию)
                                    Timber.d("=== Переход из Budget/Wallets - устанавливаем режим ДОХОДА ===")
                                    
                                    if (storedWalletId != null) {
                                        // Если есть сохраненный ID, используем его
                                        Timber.d("Используем сохраненный walletId: $storedWalletId")
                                        
                                        // Сначала устанавливаем ID целевого кошелька
                                        addTransactionViewModel.setTargetWalletId(storedWalletId)
                                    }
                                    
                                    // Затем настраиваем для доходной операции
                                    addTransactionViewModel.setupForIncomeAddition(
                                        amount = "",
                                        shouldDistribute = false
                                    )
                                    
                                    // Если включено автоматическое распределение, выбираем все кошельки
                                    if (shouldDistribute) {
                                        Timber.d("Включено автоматическое распределение дохода, выбираем все кошельки")
                                        addTransactionViewModel.clearSelectedWallets()
                                        addTransactionViewModel.selectAllWallets()
                                    }
                                }
                                
                                // Проверяем финальное состояние
                                Timber.d("После настройки из Budget/Wallets: isExpense=${addTransactionViewModel.state.value.isExpense}, targetWalletId=${addTransactionViewModel.state.value.targetWalletId}, addToWallet=${addTransactionViewModel.state.value.addToWallet}, selectedWallets=${addTransactionViewModel.state.value.selectedWallets}")
                            }
                        } else if (previousRoute?.startsWith("wallet/") == true) {
                            // Если переход был из WalletTransactions, извлекаем walletId из маршрута
                            val walletId = previousRoute.removePrefix("wallet/")
                            Timber.d("=== Переход из WalletTransactions с walletId=$walletId ===")
                            
                            LaunchedEffect(Unit) {
                                // Сначала устанавливаем ID целевого кошелька
                                addTransactionViewModel.setTargetWalletId(walletId)
                                
                                // Затем настраиваем ViewModel для добавления дохода
                                addTransactionViewModel.setupForIncomeAddition(
                                    amount = "",
                                    shouldDistribute = false
                                )
                                
                                // Дополнительно логируем состояние после установки
                                Timber.d("После setupForIncomeAddition в MainScreen с walletId: isExpense=${addTransactionViewModel.state.value.isExpense}, targetWalletId=${addTransactionViewModel.state.value.targetWalletId}, addToWallet=${addTransactionViewModel.state.value.addToWallet}, selectedWallets=${addTransactionViewModel.state.value.selectedWallets}")
                            }
                        } else {
                            // Если переход с любого другого экрана (например, HomeScreen),
                            // настраиваем для расхода с выбором всех кошельков
                            Timber.d("=== Переход из $previousRoute - устанавливаем режим РАСХОДА со всеми кошельками ===")
                            LaunchedEffect(Unit) {
                                try {
                                    // Сбрасываем состояние и настраиваем для расхода с автоматическим выбором всех кошельков
                                    addTransactionViewModel.resetToDefaultState()
                                    
                                    // Включаем добавление в кошельки и выбираем все кошельки без показа диалога
                                    addTransactionViewModel.selectAllWalletsWithoutDialog()
                                    
                                    // Настраиваем вьюмодель на режим расхода
                                    addTransactionViewModel.setupForExpenseAddition("", "Списание", context = context)
                                    
                                    // Дополнительно логируем состояние после настройки
                                    Timber.d("После настройки из HomeScreen: isExpense=${addTransactionViewModel.state.value.isExpense}, forceExpense=${addTransactionViewModel.state.value.forceExpense}, addToWallet=${addTransactionViewModel.state.value.addToWallet}, selectedWallets=${addTransactionViewModel.state.value.selectedWallets}, showWalletSelector=${addTransactionViewModel.state.value.showWalletSelector}")
                                } catch (e: Exception) {
                                    Timber.e(e, "Ошибка при настройке параметров кошельков")
                                }
                            }
                        }
                        
                        AddTransactionScreen(
                            viewModel = addTransactionViewModel,
                            onNavigateBack = {
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
                    
                    // Экран редактирования транзакции
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
                    ) { backStackEntry ->
                        val transactionId = 
                            backStackEntry.arguments?.getString("transactionId") ?: ""
                        
                        // Логируем переход на экран редактирования
                        Timber.d("Переход на экран редактирования транзакции: $transactionId")
                        
                        // Используем EditTransactionScreen вместо AddTransactionScreen
                        EditTransactionScreen(
                            viewModel = koinViewModel<EditTransactionViewModel>(),
                            onNavigateBack = { 
                                // При возврате обновляем данные и возвращаемся назад
                                homeViewModel.initiateBackgroundDataRefresh()
                                navController.navigateUp() 
                            },
                            transactionId = transactionId
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