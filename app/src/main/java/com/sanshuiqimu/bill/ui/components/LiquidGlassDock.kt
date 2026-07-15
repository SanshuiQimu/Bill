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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dock 导航项
 */
data class DockItem(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * 液态玻璃风格底部 Dock 导航栏 (原生 Compose 实现)
 *
 * 视觉效果还原自 Liquid Glass Dock HTML:
 * - 半透明毛玻璃背景 + 模糊
 * - 圆角药丸形状 (28dp)
 * - 滑动指示器带弹性弹簧动画
 * - 每个标签独立主题色
 * - 选中项放大 + 高亮
 */
@Composable
fun LiquidGlassDock(
    items: List<DockItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 每个标签的宽度
    val itemWidth = 81.dp
    // 指示器相对左边的偏移
    val indicatorOffset by animateDpAsState(
        targetValue = (selectedIndex * itemWidth.value).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // === Dock 底座 - 毛玻璃效果 ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.20f))
                .blur(30.dp)
        )

        // 阴影层
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
        )

        // 高光渐变层
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.15f)
                        )
                    )
                )
        )

        // === 滑动指示器 ===
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset + 4.dp, y = 4.dp)
                .width(73.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(items[selectedIndex].accentColor.copy(alpha = 0.12f))
        ) {
            // 指示器内部高光
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
        }

        // === Dock 项 ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                val itemAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.45f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "itemAlpha"
                )

                val itemScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "itemScale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemClick(index) }
                        .padding(vertical = 6.dp)
                        .graphicsLayer {
                            scaleX = itemScale
                            scaleY = itemScale
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) item.accentColor else Color.Gray.copy(alpha = itemAlpha),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) item.accentColor else Color.Gray.copy(alpha = itemAlpha)
                    )
                }
            }
        }
    }
}

/**
 * 获取记账本应用的 Dock 项列表
 */
fun getDockItems(): List<DockItem> = listOf(
    DockItem("首页", Icons.Filled.AccountBalanceWallet, Color(0xFF04B285)),
    DockItem("记一笔", Icons.Filled.CardGiftcard, Color(0xFFFF3760)),
    DockItem("统计", Icons.Filled.ShoppingBag, Color(0xFF0A84FF)),
    DockItem("设置", Icons.Filled.Person, Color(0xFFFF9F0A))
)
