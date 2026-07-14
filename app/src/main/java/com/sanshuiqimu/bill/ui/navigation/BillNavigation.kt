package com.sanshuiqimu.bill.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
            /** NavHost 注册时使用的路由模式（带可选参数占位符） */
            const val ROUTE_PATTERN = "add_transaction?transactionId={transactionId}"

            /** 参数名 */
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
 * 底部导航栏项数据
 *
 * @param name 显示名称
 * @param route 路由路径
 * @param icon 图标
 */
data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

/**
 * 底部导航栏项列表
 * 首页、统计、记一笔、设置
 */
val bottomNavItems = listOf(
    BottomNavItem("首页", Screen.Home.route, Icons.Filled.Home),
    BottomNavItem("统计", Screen.Stats.route, Icons.Filled.BarChart),
    BottomNavItem("记一笔", Screen.AddTransaction().route, Icons.Filled.Add),
    BottomNavItem("设置", Screen.Settings.route, Icons.Filled.Settings)
)

/**
 * 底部导航栏
 *
 * @param currentRoute 当前路由
 * @param onNavigate 导航回调
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
 *
 * 管理 Home、AddTransaction、Stats、Settings、CategoryManage 五个页面，
 * 底部包含 4 个导航标签。在「记一笔」页面隐藏底部导航栏。
 *
 * @param navController 导航控制器
 */
@Composable
fun BillNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // 在 AddTransaction 页面隐藏底部导航栏
            if (currentRoute?.startsWith("add_transaction") != true) {
                BillBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == Screen.AddTransaction().route) {
                            // 记一笔：直接导航到新页面
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
