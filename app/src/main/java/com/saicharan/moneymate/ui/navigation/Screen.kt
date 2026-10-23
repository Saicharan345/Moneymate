package com.saicharan.moneymate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.saicharan.moneymate.ui.transactions.TRANSACTION_ID_ARG

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Transactions : Screen("transactions")
    data object Analytics : Screen("analytics")
    data object Settings : Screen("settings")

    data object TransactionForm : Screen("transaction_form?$TRANSACTION_ID_ARG={$TRANSACTION_ID_ARG}") {
        /** Pass null to add a new transaction, or an id to edit an existing one. */
        fun createRoute(id: Long? = null): String =
            if (id == null) "transaction_form" else "transaction_form?$TRANSACTION_ID_ARG=$id"
    }
}

data class TopLevelDestination(val screen: Screen, val label: String, val icon: ImageVector)

val topLevelDestinations = listOf(
    TopLevelDestination(Screen.Home, "Home", Icons.Filled.Home),
    TopLevelDestination(Screen.Transactions, "Transactions", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(Screen.Analytics, "Analytics", Icons.Filled.PieChart),
    TopLevelDestination(Screen.Settings, "Settings", Icons.Filled.Settings)
)
