package com.sanshuiqimu.bill.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
}

/**
 * 根据当前路由获取 Dock 选中索引
 *
 * Dock 项顺序: 0=首页, 1=记一笔, 2=统计, 3=设置
 */
private fun getDockIndex(route: String?): Int = when {
    route == null -> 0
    route == Screen.Home.route -> 0
    route.startsWith("add_transaction") -> 1
    route == Screen.Stats.route -> 2
    route == Screen.Settings.route -> 3
    route == Screen.CategoryManage.route -> 3
    else -> 0
}

/**
 * 主导航 Host
 *
 * 使用液态玻璃风格 Dock 替代传统底部导航栏。
 * Dock 包含 4 个标签：首页、记一笔、统计、设置。
 * 在「记一笔」编辑页面隐藏 Dock。
 */
@Composable
fun BillNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val dockItems = getDockItems()

    // 判断是否显示 Dock（AddTransaction 页面隐藏）
    val showDock = currentRoute?.startsWith("add_transaction") != true

    Scaffold(
        bottomBar = {
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
            // 首页
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

            // 记一笔 / 编辑账单
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

            // 统计
            composable(Screen.Stats.route) {
                StatsScreen()
            }

            // 设置
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // 分类管理
            composable(Screen.CategoryManage.route) {
                SettingsScreen()
            }
        }
    }
}
