package com.saicharan.moneymate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saicharan.moneymate.ui.analytics.AnalyticsScreen
import com.saicharan.moneymate.ui.dashboard.DashboardScreen
import com.saicharan.moneymate.ui.navigation.Screen
import com.saicharan.moneymate.ui.navigation.topLevelDestinations
import com.saicharan.moneymate.ui.settings.SettingsScreen
import com.saicharan.moneymate.ui.transactions.TRANSACTION_ID_ARG
import com.saicharan.moneymate.ui.transactions.TransactionFormScreen
import com.saicharan.moneymate.ui.transactions.TransactionsScreen
import kotlinx.coroutines.launch

@Composable
fun MoneyMateRoot() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val currentRoute = destination?.route
    val topLevelRoutes = remember { topLevelDestinations.map { it.screen.route } }
    val showBottomBar = currentRoute in topLevelRoutes
    val showFab = currentRoute == Screen.Home.route || currentRoute == Screen.Transactions.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.TransactionForm.createRoute()) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add transaction")
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    topLevelDestinations.forEach { item ->
                        val selected = destination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateTopLevel(item.screen.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Screen.Home.route) {
                DashboardScreen(
                    onAddTransaction = { navController.navigate(Screen.TransactionForm.createRoute()) },
                    onEditTransaction = { id -> navController.navigate(Screen.TransactionForm.createRoute(id)) },
                    onSeeAllTransactions = { navController.navigateTopLevel(Screen.Transactions.route) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    snackbarHostState = snackbarHostState,
                    onAddTransaction = { navController.navigate(Screen.TransactionForm.createRoute()) },
                    onEditTransaction = { id -> navController.navigate(Screen.TransactionForm.createRoute(id)) }
                )
            }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
            composable(Screen.Settings.route) { SettingsScreen(snackbarHostState) }
            composable(
                route = Screen.TransactionForm.route,
                arguments = listOf(
                    navArgument(TRANSACTION_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                TransactionFormScreen(
                    onBack = { navController.popBackStack() },
                    onFinished = { message ->
                        navController.popBackStack()
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
