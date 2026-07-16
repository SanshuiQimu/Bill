package com.sanshuiqimu.bill.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sanshuiqimu.bill.ui.components.LiquidGlassDock
import com.sanshuiqimu.bill.ui.components.getDockItems
import com.sanshuiqimu.bill.ui.screens.add.AddTransactionScreen
import com.sanshuiqimu.bill.ui.screens.home.HomeScreen
import com.sanshuiqimu.bill.ui.screens.settings.SettingsScreen
import com.sanshuiqimu.bill.ui.screens.stats.StatsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data class AddTransaction(val transactionId: String? = null) : Screen(
        if (transactionId != null) "add_transaction?transactionId=$transactionId" else "add_transaction"
    ) {
        companion object {
            const val ROUTE_PATTERN = "add_transaction?transactionId={transactionId}"
            const val ARG_TRANSACTION_ID = "transactionId"
        }
    }
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object CategoryManage : Screen("category_manage")
}

private fun getDockIndex(route: String?): Int = when {
    route == null -> 0
    route == Screen.Home.route -> 0
    route.startsWith("add_transaction") -> 1
    route == Screen.Stats.route -> 2
    route == Screen.Settings.route -> 3
    route == Screen.CategoryManage.route -> 3
    else -> 0
}

@Composable
fun BillNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val dockItems = getDockItems()
    val showDock = currentRoute?.startsWith("add_transaction") != true

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddTransaction = {
                        navController.navigate(Screen.AddTransaction().route)
                    },
                    onNavigateToEditTransaction = { transactionId ->
                        navController.navigate(Screen.AddTransaction(transactionId).route)
                    }
                )
            }
            composable(
                route = Screen.AddTransaction.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(Screen.AddTransaction.ARG_TRANSACTION_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments
                    ?.getString(Screen.AddTransaction.ARG_TRANSACTION_ID)
                AddTransactionScreen(
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.CategoryManage.route) {
                SettingsScreen()
            }
        }

        if (showDock) {
            LiquidGlassDock(
                items = dockItems,
                selectedIndex = getDockIndex(currentRoute),
                onItemClick = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        1 -> navController.navigate(Screen.AddTransaction().route)
                        2 -> navController.navigate(Screen.Stats.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        3 -> navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}
