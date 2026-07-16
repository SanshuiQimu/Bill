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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DockItem(
    val label: String,
    val iconPath: @Composable (Color, Float) -> Unit,
    val accentColor: Color
)

// === SVG 图标用 Canvas 精确复刻 (使用 lineTo 替代 hLineTo/vLineTo) ===

@Composable
private fun WalletIcon(color: Color, sw: Float = 2.2f) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(25.dp)) {
        val w = size.width
        val h = size.height
        val u = w / 24f
        val v = h / 24f
        val s = sw * u

        // path1: M 3 9 v -1 a 2 2 0 0 1 2 -2 h 14 a 2 2 0 0 1 2 2 v 1 z
        val p1 = Path().apply {
            moveTo(3*u, 9*v)
            lineTo(3*u, 8*v)
            arcTo(Rect(2*u, 6*v, 6*u, 10*v), 180f, 90f, false)
            lineTo(20*u, 6*v)
            arcTo(Rect(18*u, 6*v, 22*u, 10*v), 270f, 90f, false)
            lineTo(22*u, 9*v)
            close()
        }
        // path2: M 3 11 v 5 a 2 2 0 0 0 2 2 h 14 a 2 2 0 0 0 2 -2 v -5 z
        val p2 = Path().apply {
            moveTo(3*u, 11*v)
            lineTo(3*u, 16*v)
            arcTo(Rect(2*u, 14*v, 6*u, 18*v), 180f, -90f, false)
            lineTo(20*u, 18*v)
            arcTo(Rect(18*u, 14*v, 22*u, 18*v), 0f, -90f, false)
            lineTo(22*u, 11*v)
            close()
        }
        drawPath(p1, color, style = Stroke(s))
        drawPath(p2, color, style = Stroke(s))
    }
}

@Composable
private fun GiftIcon(color: Color, sw: Float = 2.2f) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(25.dp)) {
        val w = size.width
        val h = size.height
        val u = w / 24f
        val v = h / 24f
        val s = sw * u

        val bow1 = Path().apply {
            moveTo(10.5f*u, 10*v)
            cubicTo(10.5f*u, 4*v, 3*u, 5*v, 6.5f*u, 10*v)
            close()
        }
        val bow2 = Path().apply {
            moveTo(13.5f*u, 10*v)
            cubicTo(13.5f*u, 4*v, 21*u, 5*v, 17.5f*u, 10*v)
            close()
        }
        val box1 = Path().apply { addRect(Rect(4*u, 10*v, 10.5f*u, 13*v)) }
        val box2 = Path().apply { addRect(Rect(13.5f*u, 10*v, 20*u, 13*v)) }
        val body1 = Path().apply {
            moveTo(5*u, 13*v)
            lineTo(10.5f*u, 13*v)
            lineTo(10.5f*u, 19.5f*v)
            arcTo(Rect(4.5f*u, 19*v, 6.5f*u, 21*v), 270f, 90f, false)
            lineTo(5*u, 20*v)
            close()
        }
        val body2 = Path().apply {
            moveTo(13.5f*u, 13*v)
            lineTo(19*u, 13*v)
            lineTo(19*u, 19.5f*v)
            arcTo(Rect(17.5f*u, 19*v, 19.5f*u, 21*v), 0f, 90f, false)
            lineTo(13.5f*u, 20*v)
            close()
        }
        listOf(bow1, bow2, box1, box2, body1, body2).forEach {
            drawPath(it, color, style = Stroke(s))
        }
    }
}

@Composable
private fun CartIcon(color: Color, sw: Float = 2.2f) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(25.dp)) {
        val w = size.width
        val h = size.height
        val u = w / 24f
        val v = h / 24f
        val s = sw * u

        val body = Path().apply {
            moveTo(5*u, 9*v)
            lineTo(19*u, 9*v)
            lineTo(19*u, 18*v)
            arcTo(Rect(5*u, 18*v, 9*u, 22*v), 0f, -90f, false)
            lineTo(5*u, 20*v)
            arcTo(Rect(3*u, 18*v, 7*u, 22*v), 180f, -90f, false)
            close()
        }
        val handle = Path().apply {
            moveTo(8*u, 9*v)
            lineTo(8*u, 6*v)
            arcTo(Rect(8*u, 2*v, 16*u, 10*v), 180f, 90f, false)
            lineTo(16*u, 9*v)
        }
        drawPath(body, color, style = Stroke(s))
        drawPath(handle, color, style = Stroke(s))
    }
}

@Composable
private fun MeIcon(color: Color, sw: Float = 2.2f) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(25.dp)) {
        val w = size.width
        val h = size.height
        val u = w / 24f
        val v = h / 24f
        val s = sw * u

        val body = Path().apply {
            moveTo(20*u, 21*v)
            lineTo(20*u, 19*v)
            arcTo(Rect(12*u, 15*v, 20*u, 23*v), 0f, -90f, false)
            lineTo(8*u, 15*v)
            arcTo(Rect(4*u, 15*v, 12*u, 23*v), 180f, -90f, false)
            lineTo(4*u, 21*v)
        }
        drawCircle(color, 4f*u, Offset(12f*u, 7f*v), style = Stroke(s))
        drawPath(body, color, style = Stroke(s))
    }
}

@Composable
private fun DockIcon(item: DockItem, color: Color) {
    item.iconPath(color, 2.2f)
}

@Composable
private fun DockItemContent(item: DockItem, color: Color, scale: Float = 1f) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        DockIcon(item, color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, color = color)
    }
}

/**
 * 液态玻璃 Dock - 精确复刻原 HTML
 *
 * 图层: 阴影 → 底座(0.23白) → 边缘高光 → 黑色图标(可点击) → 主题色图标(仅滑块内) → 滑块(0.15黑)
 */
@Composable
fun LiquidGlassDock(
    items: List<DockItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockW = 324.dp; val dockH = 55.dp
    val sliderW = 79.dp; val sliderH = 48.dp
    val sliderT = 3.5.dp
    val itemW = 81.5.dp; val itemH = 48.dp; val itemT = 3.5.dp
    val itemLefts = listOf(3.dp, 83.dp, 162.dp, 242.dp)
    val sliderTargets = listOf(4.dp, 84.dp, 163.dp, 243.dp)

    val sliderX by animateDpAsState(
        sliderTargets[selectedIndex],
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "sliderX"
    )
    val itemScale by animateFloatAsState(
        1.1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    val themeColor = items[selectedIndex].accentColor

    Box(
        modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
        Alignment.BottomCenter
    ) {
        Box(Modifier.width(dockW).height(dockH)) {
            // 1. 阴影
            Box(Modifier.matchParentSize().offset(8.dp, 2.dp).width(dockW-16.dp).height(dockH-4.dp)
                .clip(RoundedCornerShape(25.dp))
                .drawBehind { drawRect(Color.Black.copy(alpha = 0.12f)) }
                .graphicsLayer { alpha = 0.6f })

            // 2. 底座 rgba(255,255,255,0.23)
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(27.5.dp))
                .background(Color.White.copy(alpha = 0.23f)))

            // 3. 边缘高光
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(27.5.dp)).drawBehind {
                drawRect(Brush.linearGradient(listOf(Color.White.copy(0.4f), Color.Transparent),
                    Offset.Zero, Offset(size.width, size.height)))
                drawRect(Brush.linearGradient(listOf(Color.White.copy(0.4f), Color.Transparent),
                    Offset(size.width*0.7f, size.height), Offset(size.width*0.3f, 0f)))
            })

            // 4. 黑色图标层 (可点击)
            items.forEachIndexed { index, item ->
                Box(Modifier.offset(itemLefts[index], itemT).width(itemW).height(itemH)
                    .clickable { onItemClick(index) }, Alignment.Center) {
                    DockItemContent(item, Color.Black.copy(0.7f), 1f)
                }
            }

            // 5. 主题色图标层 (仅滑块内可见)
            Box(Modifier.offset(sliderX, sliderT).width(sliderW).height(sliderH)
                .clip(RoundedCornerShape(24.dp)).clipToBounds()) {
                items.forEachIndexed { index, item ->
                    val sel = index == selectedIndex
                    Box(Modifier.offset(itemLefts[index] - sliderX, 0.dp).width(itemW).height(itemH),
                        Alignment.Center) {
                        DockItemContent(item, themeColor, if (sel) itemScale else 1f)
                    }
                }
            }

            // 6. 滑块 rgba(0,0,0,0.15) + 高光
            Box(Modifier.offset(sliderX, sliderT).width(sliderW).height(sliderH)
                .clip(RoundedCornerShape(24.dp)).background(Color.Black.copy(0.15f))) {
                Box(Modifier.matchParentSize().clip(RoundedCornerShape(24.dp)).drawBehind {
                    drawRect(Brush.linearGradient(listOf(Color.White.copy(0.9f), Color.Transparent),
                        Offset.Zero, Offset(size.width, size.height)))
                    drawRect(Brush.linearGradient(listOf(Color.White.copy(0.9f), Color.Transparent),
                        Offset(size.width*0.7f, size.height), Offset(size.width*0.3f, 0f)))
                })
            }
        }
    }
}

fun getDockItems(): List<DockItem> = listOf(
    DockItem("WALLET", { c, _ -> WalletIcon(c) }, Color(0xFF04B285)),
    DockItem("GIFT", { c, _ -> GiftIcon(c) }, Color(0xFFFF375F)),
    DockItem("CART", { c, _ -> CartIcon(c) }, Color(0xFF0A84FF)),
    DockItem("ME", { c, _ -> MeIcon(c) }, Color(0xFFFF9F0A))
)
