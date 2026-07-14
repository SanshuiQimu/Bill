package com.sanshuiqimu.bill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sanshuiqimu.bill.ui.navigation.BillNavHost
import com.sanshuiqimu.bill.ui.theme.BillTheme

/**
 * 记账本应用主 Activity
 *
 * 使用 Jetpack Compose 构建界面，启用边缘到边缘（Edge-to-Edge）沉浸式布局。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 启用边缘到边缘布局，实现沉浸式状态栏和导航栏
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BillTheme {
                BillNavHost()
            }
        }
    }
}
