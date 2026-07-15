package com.sanshuiqimu.bill.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanshuiqimu.bill.ui.theme.BalanceBlue
import com.sanshuiqimu.bill.ui.theme.ExpenseRed
import com.sanshuiqimu.bill.ui.theme.IncomeGreen

/**
 * Dock 导航项数据
 */
data class DockItem(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * 液态玻璃风格底部 Dock 导航栏
 *
 * 灵感来源于 Liquid Glass Dock HTML 设计
 * - 半透明毛玻璃背景
 * - 滑动指示器跟随选中项
 * - 每个标签有独立主题色
 */
@Composable
fun LiquidGlassDock(
    items: List<DockItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorOffset by animateDpAsState(
        targetValue = (selectedIndex * 82).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorOffset"
    )

    val indicatorScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Dock 底座 - 液态玻璃效果
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = Color.White.copy(alpha = 0.25f),
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(20.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )
        }

        // 阴影层
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
        )

        // 滑动指示器
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset - 0.dp)
                .width(82.dp)
                .height(48.dp)
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    items[selectedIndex].accentColor.copy(alpha = 0.15f)
                )
        )

        // Dock 项
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val iconColor by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.4f,
                    label = "iconAlpha"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemClick(index) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) item.accentColor else Color.Gray.copy(alpha = iconColor),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) item.accentColor else Color.Gray.copy(alpha = iconColor)
                    )
                }
            }
        }
    }
}

/**
 * 获取记账本应用的 Dock 项列表
 *
 * 映射关系:
 * 1. 钱包 (Wallet) → 首页 - 绿色
 * 2. 礼物 (Gift) → 记一笔 - 红色
 * 3. 购物袋 (Cart) → 统计 - 蓝色
 * 4. 个人 (Me) → 设置 - 橙色
 */
fun getDockItems(): List<DockItem> = listOf(
    DockItem(
        label = "首页",
        icon = Icons.Filled.AccountBalanceWallet,
        accentColor = IncomeGreen
    ),
    DockItem(
        label = "记一笔",
        icon = Icons.Filled.CardGiftcard,
        accentColor = ExpenseRed
    ),
    DockItem(
        label = "统计",
        icon = Icons.Filled.ShoppingBag,
        accentColor = BalanceBlue
    ),
    DockItem(
        label = "设置",
        icon = Icons.Filled.Person,
        accentColor = Color(0xFFFF9F0A)
    )
)
