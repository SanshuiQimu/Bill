package com.sanshuiqimu.bill.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sanshuiqimu.bill.ui.screens.add.AddTransactionScreen
import com.sanshuiqimu.bill.ui.screens.group.GroupScreen
import com.sanshuiqimu.bill.ui.screens.home.HomeScreen
import com.sanshuiqimu.bill.ui.screens.settings.SettingsScreen
import com.sanshuiqimu.bill.ui.screens.stats.StatsScreen

/**
 * 页面路由定义
 */
sealed class Screen(val route: String) {

    /** 首页 */
    data object Home : Screen("home")

    /** 记一笔 / 编辑账单 */
    data class AddTransaction(val transactionId: String? = null) : Screen(
        if (transactionId != null) "add_transaction?transactionId=$transactionId" else "add_transaction"
    ) {
        companion object {
            const val ROUTE_PATTERN = "add_transaction?transactionId={transactionId}"
            const val ARG_TRANSACTION_ID = "transactionId"
        }
    }

    /** 统计 */
    data object Stats : Screen("stats")

    /** 设置 */
    data object Settings : Screen("settings")

    /** 分类管理 */
    data object CategoryManage : Screen("category_manage")

    /** 小组共享记账 */
    data object Group : Screen("group")
}

/**
 * 底部导航栏项数据
 */
data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

/**
 * 底部导航栏项列表
 */
val bottomNavItems = listOf(
    BottomNavItem("首页", Screen.Home.route, Icons.Filled.Home),
    BottomNavItem("统计", Screen.Stats.route, Icons.Filled.BarChart),
    BottomNavItem("记一笔", Screen.AddTransaction().route, Icons.Filled.Add),
    BottomNavItem("设置", Screen.Settings.route, Icons.Filled.Settings)
)

/**
 * 底部导航栏
 */
@Composable
fun BillBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name
                    )
                },
                label = { Text(text = item.name) },
                selected = currentRoute == item.route ||
                        currentRoute?.startsWith("${item.route}?") == true,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

/**
 * 主导航 Host
 */
@Composable
fun BillNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute?.startsWith("add_transaction") != true) {
                BillBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == Screen.AddTransaction().route) {
                            navController.navigate(route)
                        } else {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
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
                SettingsScreen(
                    onNavigateToGroup = { navController.navigate(Screen.Group.route) }
                )
            }

            composable(Screen.CategoryManage.route) {
                SettingsScreen(
                    onNavigateToGroup = { navController.navigate(Screen.Group.route) }
                )
            }

            composable(Screen.Group.route) {
                GroupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
