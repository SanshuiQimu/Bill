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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DockItem(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * 液态玻璃 Dock - 精确复刻原 HTML
 *
 * 图层结构 (从底到顶):
 * 1. 阴影层: box-shadow 0 16px 32px rgba(0,0,0,0.15)
 * 2. Dock底座: rgba(255,255,255,0.23) + blur(模拟backdrop-filter)
 * 3. 边缘高光: 135deg + (-35deg) 白色渐变 (模拟plus-lighter)
 * 4. 黑色图标层: 全部图标黑色 (可点击)
 * 5. 主题色图标层: 全部图标主题色, 仅滑块区域内可见 (clipToBounds)
 * 6. 滑块: rgba(0,0,0,0.15) 深色半透明 + 边缘高光
 */
@Composable
fun LiquidGlassDock(
    items: List<DockItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 原始 HTML 尺寸
    val dockWidth = 324.dp
    val dockHeight = 55.dp
    val sliderWidth = 79.dp
    val sliderHeight = 48.dp
    val sliderTopOffset = 3.5.dp
    val itemWidth = 81.dp
    val itemHeight = 48.dp
    val itemTopOffset = 3.5.dp

    // 各图标位置 (left: 3, 83, 162, 242)
    val itemPositions = listOf(3.dp, 83.dp, 162.dp, 242.dp)
    val targetSliderX = itemPositions[selectedIndex]

    // 弹簧动画
    val sliderOffset by animateDpAsState(
        targetValue = targetSliderX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sliderOffset"
    )

    // 选中项放大
    val selectedScale by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "selectedScale"
    )

    val themeColor = items[selectedIndex].accentColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(modifier = Modifier.width(dockWidth).height(dockHeight)) {

            // === 1. 阴影层 ===
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 2.dp, x = 8.dp)
                    .width(dockWidth - 16.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .drawBehind {
                        drawRect(Color.Black.copy(alpha = 0.12f))
                    }
                    .blur(24.dp)
            )

            // === 2. Dock 底座 ===
            // background: rgba(255,255,255,0.23)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(27.5.dp))
                    .background(Color.White.copy(alpha = 0.23f))
            )

            // 底座模糊层
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(27.5.dp))
                    .blur(12.dp)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            // === 3. 边缘高光 (dock-base::after) ===
            // linear-gradient(135deg, rgba(255,255,255,0.4) 0%, transparent 50%)
            // linear-gradient(-35deg, rgba(255,255,255,0.4) 0%, transparent 50%)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(27.5.dp))
                    .drawBehind {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                        )
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                                start = Offset(size.width * 0.7f, size.height),
                                end = Offset(size.width * 0.3f, 0f)
                            )
                        )
                    }
            )

            // === 4. 黑色图标层 (可点击) ===
            items.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .offset(x = itemPositions[index], y = itemTopOffset)
                        .width(itemWidth)
                        .height(itemHeight)
                        .clickable { onItemClick(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
            }

            // === 5. 主题色图标层 (仅滑块区域内可见) ===
            // 用一个跟随滑块位置和大小的 Box + clipToBounds 实现 mask 效果
            Box(
                modifier = Modifier
                    .offset(x = sliderOffset, y = sliderTopOffset)
                    .width(sliderWidth)
                    .height(sliderHeight)
                    .clip(RoundedCornerShape(24.dp))
                    .clipToBounds()
            ) {
                // 在裁切区域内放置所有图标, 用负偏移还原它们的原始位置
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Column(
                        modifier = Modifier
                            .offset(
                                x = itemPositions[index] - sliderOffset,
                                y = 0.dp
                            )
                            .width(itemWidth)
                            .height(itemHeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = themeColor,
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
                            letterSpacing = 0.5.sp,
                            color = themeColor
                        )
                    }
                }
            }

            // === 6. 滑块 (frosted-slider) ===
            // background-color: rgba(0,0,0,0.15)
            // box-shadow: 0 2px 8px rgba(0,0,0,0.06)
            Box(
                modifier = Modifier
                    .offset(x = sliderOffset, y = sliderTopOffset)
                    .width(sliderWidth)
                    .height(sliderHeight)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.15f))
            ) {
                // 滑块边缘高光 (::after)
                // linear-gradient(135deg, rgba(255,255,255,0.9) 0%, transparent 50%)
                // linear-gradient(-35deg, rgba(255,255,255,0.9) 0%, transparent 50%)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                        .drawBehind {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.9f), Color.Transparent),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                )
                            )
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.9f), Color.Transparent),
                                    start = Offset(size.width * 0.7f, size.height),
                                    end = Offset(size.width * 0.3f, 0f)
                                )
                            )
                        }
                )
            }
        }
    }
}

fun getDockItems(): List<DockItem> = listOf(
    DockItem("首页", Icons.Filled.AccountBalanceWallet, Color(0xFF04B285)),
    DockItem("记一笔", Icons.Filled.CardGiftcard, Color(0xFFFF375F)),
    DockItem("统计", Icons.Filled.ShoppingBag, Color(0xFF0A84FF)),
    DockItem("设置", Icons.Filled.Person, Color(0xFFFF9F0A))
)
