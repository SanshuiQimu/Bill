package com.sanshuiqimu.bill.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sanshuiqimu.bill.ui.components.LiquidGlassDockWebView
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
 * 内容全屏延伸（edge-to-edge），液态玻璃 Dock 悬浮覆盖在底部。
 * 在「记一笔」编辑页面隐藏 Dock。
 */
@Composable
fun BillNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 判断是否显示 Dock（AddTransaction 页面隐藏）
    val showDock = currentRoute?.startsWith("add_transaction") != true

    // 防止 Dock 通知和导航控制器互相触发循环
    var dockSyncIndex by remember { mutableStateOf(getDockIndex(currentRoute)) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // === 内容区域：全屏延伸 ===
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
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

        // === 悬浮 Dock：覆盖在内容底部 ===
        if (showDock) {
            LiquidGlassDockWebView(
                selectedIndex = getDockIndex(currentRoute),
                onItemSelected = { index ->
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
