package com.sanshuiqimu.bill.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dock 导航项数据
 */
data class DockItem(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * 液态玻璃 Dock - 原生 Compose 实现
 *
 * 精确复刻 Liquid Glass Dock HTML 的视觉效果:
 * - 药丸形底座: 半透明白色 + 模糊 + 阴影
 * - 边缘高光: 双向线性渐变
 * - 滑动指示器: 弹簧动画 + 半透明背景 + 内外阴影
 * - 选中项: 主题色高亮 + 放大
 * - 未选中项: 黑色半透明
 */
@Composable
fun LiquidGlassDock(
    items: List<DockItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 每个 tab 的宽度 (原始 HTML: 81.5px)
    val itemWidthDp = 81.5.dp
    // 指示器宽度 (原始 HTML: 79px)
    val sliderWidthDp = 79.dp
    // 指示器偏移: tab index * itemWidth + 起始偏移(4dp)
    val targetOffset = (selectedIndex * 81 + 4).dp

    // 弹簧动画 - 还原 HTML 中的物理效果
    val sliderOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sliderOffset"
    )

    // 选中项放大效果
    val selectedScale by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "selectedScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // === 1. 阴影层 ===
        Box(
            modifier = Modifier
                .width(324.dp)
                .height(55.dp)
                .offset(y = 2.dp)
                .clip(RoundedCornerShape(25.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(25.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
        )

        // === 2. Dock 底座 - 半透明白色 + 模糊 ===
        Box(
            modifier = Modifier
                .width(324.dp)
                .height(55.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color.White.copy(alpha = 0.55f))
                .blur(20.dp)
        )

        // === 3. 边缘高光渐变 (还原 ::after 伪元素) ===
        Box(
            modifier = Modifier
                .width(324.dp)
                .height(55.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.4f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset.Infinite
                    )
                )
        )

        // === 4. 滑动指示器 (frosted-slider) ===
        Box(
            modifier = Modifier
                .offset(x = sliderOffset, y = 3.5.dp)
                .width(sliderWidthDp)
                .height(48.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.45f))
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
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
                                Color.White.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
            // 指示器底部阴影
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .drawBehind {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.06f),
                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 1.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width, 1.dp.toPx())
                        )
                    }
            )
        }

        // === 5. Dock 导航项 ===
        Row(
            modifier = Modifier
                .width(324.dp)
                .height(55.dp),
            horizontalArrangement = Arrangement.Absolute.Left,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                // 未选中项的透明度
                val itemAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "itemAlpha$index"
                )

                Column(
                    modifier = Modifier
                        .width(itemWidthDp)
                        .height(48.dp)
                        .offset(y = 0.5.dp)
                        .clickable(
                            enabled = true,
                            onClick = { onItemClick(index) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) item.accentColor else Color.Black.copy(alpha = itemAlpha),
                        modifier = Modifier
                            .size(25.dp)
                            .graphicsLayer {
                                scaleX = if (isSelected) selectedScale else 1f
                                scaleY = if (isSelected) selectedScale else 1f
                            }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) item.accentColor else Color.Black.copy(alpha = itemAlpha)
                    )
                }
            }
        }
    }
}

/**
 * 获取记账本应用的 Dock 项
 *
 * 还原原 HTML 中的 4 个标签:
 * Wallet(钱包) → 首页 - 绿色 #04B285
 * Gift(礼物) → 记一笔 - 红色 #FF375F
 * Cart(购物) → 统计 - 蓝色 #0A84FF
 * Me(个人) → 设置 - 橙色 #FF9F0A
 */
fun getDockItems(): List<DockItem> = listOf(
    DockItem("首页", Icons.Filled.AccountBalanceWallet, Color(0xFF04B285)),
    DockItem("记一笔", Icons.Filled.CardGiftcard, Color(0xFFFF375F)),
    DockItem("统计", Icons.Filled.ShoppingBag, Color(0xFF0A84FF)),
    DockItem("设置", Icons.Filled.Person, Color(0xFFFF9F0A))
)
