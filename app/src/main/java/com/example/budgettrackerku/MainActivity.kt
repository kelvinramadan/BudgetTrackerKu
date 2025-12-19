package com.example.budgettrackerku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgettrackerku.data.local.BudgetDatabase
import com.example.budgettrackerku.data.repository.BudgetRepository
import com.example.budgettrackerku.ui.auth.AuthScreen
import com.example.budgettrackerku.ui.home.HomeScreen
import com.example.budgettrackerku.ui.main.ReportScreen
import com.example.budgettrackerku.ui.main.SettingScreen
import com.example.budgettrackerku.ui.main.TransactionScreen
import com.example.budgettrackerku.ui.theme.BudgetTrackerKuTheme
import com.example.budgettrackerku.ui.theme.LightGray
import com.example.budgettrackerku.viewmodel.BudgetViewModel
import com.example.budgettrackerku.viewmodel.BudgetViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = BudgetDatabase.getDatabase(this)
        val repository = BudgetRepository(database.transactionDao())
        val viewModelFactory = BudgetViewModelFactory(repository, this)

        setContent {
            BudgetTrackerKuTheme {
                val viewModel: BudgetViewModel = viewModel(factory = viewModelFactory)
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    val auth = FirebaseAuth.getInstance()
                    isLoggedIn = auth.currentUser != null
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        isLoggedIn = firebaseAuth.currentUser != null
                    }
                    auth.addAuthStateListener(listener)
                }

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
        "settings" -> "Settings"
        else -> "BudgetTrackerKu"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                val items = listOf(
                    "home" to Icons.Default.Home,
                    "transactions" to Icons.AutoMirrored.Filled.List,
                    "reports" to Icons.Default.PieChart,
                    "settings" to Icons.Default.Settings
                )
                items.forEach { (route, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = route) },
                        label = { Text(route.replaceFirstChar { it.uppercase() }) },
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding).background(LightGray)
        ) {
            composable("home") { HomeScreen(viewModel) }
            composable("transactions") { TransactionScreen(viewModel) }
            composable("reports") { ReportScreen(viewModel) }
            composable("settings") { SettingScreen(viewModel, onLogout) }
        }
    }
}
