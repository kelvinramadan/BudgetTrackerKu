package com.example.budgettrackerku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgettrackerku.ui.auth.AuthScreen
import com.example.budgettrackerku.ui.home.HomeScreen
import com.example.budgettrackerku.ui.main.ReportScreen
import com.example.budgettrackerku.ui.main.BudgetScreen
import com.example.budgettrackerku.ui.main.AddTransactionScreen
import com.example.budgettrackerku.ui.main.ProfileScreen
import com.example.budgettrackerku.ui.main.TransactionScreen
import com.example.budgettrackerku.ui.theme.BudgetTrackerKuTheme
import com.example.budgettrackerku.ui.theme.LightGray
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import com.example.budgettrackerku.viewmodel.BudgetViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Room-related code is removed
        // Room-related code is removed
        val viewModelFactory = BudgetViewModelFactory(this)
        setContent {
            BudgetTrackerKuTheme {
                // Request Notification Permission on Android 13+
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                     val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                         contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                         onResult = { isGranted ->
                             if (isGranted) {
                                 // Permission granted
                             } else {
                                 // Permission denied
                             }
                         }
                     )
                     LaunchedEffect(Unit) {
                         permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                     }
                }

                val viewModel: BudgetViewModel = viewModel(factory = viewModelFactory)
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    isLoggedIn = auth.currentUser != null
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                         val loggedIn = firebaseAuth.currentUser != null
                         isLoggedIn = loggedIn
                         if (loggedIn) {
                             viewModel.refreshUserData()
                         }
                    }
                    auth.addAuthStateListener(listener)
                }

                if (showSplash) {
                    com.example.budgettrackerku.ui.main.SplashScreen(
                        onTimeout = { showSplash = false }
                    )
                } else {
                    if (isLoggedIn == true) {
                        MainApp(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.logout()
                            }
                        )
                    } else if (isLoggedIn == false) {
                        AuthScreen(
                            onLoginSuccess = {
                                viewModel.refreshUserData()
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: BudgetViewModel, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screenTitle = when (currentRoute) {
        "home" -> "Home"
        "transactions" -> "Transactions"
        "reports" -> "Reports"
        "budget" -> "Budget"
        else -> "BudgetTrackerKu"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                composable(
                    "home",
                    enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) }
                ) { 
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToReports = {
                            navController.navigate("reports") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToHistory = {
                            navController.navigate("transactions") { 
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToProfile = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToNotifications = {
                            navController.navigate("notifications") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate("settings") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    "transactions",
                    enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) }
                ) { 
                    TransactionScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        }
                    ) 
                }
                composable(
                    "reports",
                    enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) }
                ) { 
                    ReportScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToNotifications = {
                            navController.navigate("notifications") {
                                launchSingleTop = true
                            }
                        }
                    ) 
                }
                composable(
                    "budget",
                    enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) }
                ) {
                    BudgetScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToNotifications = {
                            navController.navigate("notifications") {
                                launchSingleTop = true
                            }
                        }
                    ) 
                }
                composable(
                    "add_transaction",
                     enterTransition = { androidx.compose.animation.slideInVertically(initialOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                     exitTransition = { androidx.compose.animation.slideOutVertically(targetOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                     popEnterTransition = { androidx.compose.animation.slideInVertically(initialOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                     popExitTransition = { androidx.compose.animation.slideOutVertically(targetOffsetY = { it }, animationSpec = androidx.compose.animation.core.tween(300)) }
                ) { AddTransactionScreen(navController, viewModel) }
                composable(
                    "profile",
                    enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) }
                ) { 
                        ProfileScreen(
                        navController = navController, 
                        viewModel = viewModel, 
                        onLogout = onLogout
                    ) 
                }
                composable(
                    "notifications",
                    enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) }
                ) {
                    com.example.budgettrackerku.ui.main.NotificationScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    "settings",
                    enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }, animationSpec = androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }, animationSpec = androidx.compose.animation.core.tween(300)) }
                ) {
                    com.example.budgettrackerku.ui.main.SettingsScreen(navController = navController)
                }
            }
        }

        // Floating Bottom Bar Overlay - Hide on AddTransactionScreen, ProfileScreen, NotificationScreen, or SettingsScreen
        if (currentRoute != "add_transaction" && currentRoute != "profile" && currentRoute != "notifications" && currentRoute != "settings") {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(90.dp), // Height to accommodate button sticking out
                contentAlignment = Alignment.BottomCenter
            ) {
                // Bottom Bar Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp), // Height to accommodate button sticking out
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // 1. Background Bar
                    val barColor = if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.background else com.example.budgettrackerku.ui.theme.BottomBarBackground
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp) // Standard bar height
                            .background(barColor),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Items
                        val leftItems = listOf(
                            "home" to Icons.Default.Home,
                            "transactions" to Icons.AutoMirrored.Filled.List
                        )
                        leftItems.forEach { (route, icon) ->
                            IconButton(onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    icon, 
                                    contentDescription = route,
                                    tint = if (currentRoute == route) com.example.budgettrackerku.ui.theme.BluePrimary else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Spacer for Center Button
                        Spacer(modifier = Modifier.width(70.dp))

                        // Right Items
                        val rightItems = listOf(
                            "reports" to Icons.Default.PieChart,
                            "budget" to Icons.Default.AccountBalanceWallet
                        )
                        rightItems.forEach { (route, icon) ->
                            IconButton(onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    icon, 
                                    contentDescription = route,
                                    tint = if (currentRoute == route) com.example.budgettrackerku.ui.theme.BluePrimary else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // 2. Center Button (Floating Circle)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter) // Align to top of the 100dp box
                            .size(70.dp) // Large button
                            .clip(androidx.compose.foundation.shape.CircleShape) // Circle shape
                            .background(com.example.budgettrackerku.ui.theme.BluePrimary)
                            .clickable { navController.navigate("add_transaction") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Add,
                            contentDescription = "Add",
                            tint = com.example.budgettrackerku.ui.theme.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
