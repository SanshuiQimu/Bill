package com.sanshuiqimu.bill.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sanshuiqimu.bill.ui.theme.AmountTextStyle
import com.sanshuiqimu.bill.ui.theme.BalanceBlue
import com.sanshuiqimu.bill.ui.theme.ExpenseRed
import com.sanshuiqimu.bill.ui.theme.IncomeGreen
import com.sanshuiqimu.bill.util.TransactionType
import java.util.Locale

/**
 * 格式化金额，保留两位小数并添加千位分隔符
 */
private fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%,.2f", amount)
}

/**
 * 金额文本
 * 根据收入/支出显示不同颜色，收入为绿色带 "+" 前缀，支出为红色带 "-" 前缀
 *
 * @param amount 金额数值
 * @param type 交易类型（收入/支出）
 * @param modifier 修饰符
 * @param style 文本样式
 */
@Composable
fun AmountText(
    amount: Double,
    type: TransactionType,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium
) {
    val color = when (type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
    }
    val prefix = when (type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
    }
    Text(
        text = "$prefix${formatAmount(amount)}",
        color = color,
        style = style,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * 交易卡片
 * 显示单条交易信息，包含类型图标、分类、描述、金额、日期
 *
 * @param category 分类名称
 * @param description 描述（可选）
 * @param amount 金额
 * @param type 交易类型
 * @param date 日期文本
 * @param modifier 修饰符
 * @param onClick 点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    category: String,
    description: String,
    amount: Double,
    type: TransactionType,
    date: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 类型图标
            val iconBgColor = if (type == TransactionType.INCOME) {
                IncomeGreen.copy(alpha = 0.12f)
            } else {
                ExpenseRed.copy(alpha = 0.12f)
            }
            val iconTint = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
            val iconVector = if (type == TransactionType.INCOME) {
                Icons.Filled.TrendingUp
            } else {
                Icons.Filled.TrendingDown
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 分类、描述、日期
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 金额
            AmountText(
                amount = amount,
                type = type,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

/**
 * 总览卡片
 * 显示总收入、总支出、本月结余
 *
 * @param totalIncome 总收入
 * @param totalExpense 总支出
 * @param modifier 修饰符
 */
@Composable
fun SummaryCard(
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val balance = totalIncome - totalExpense

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 结余标题
            Text(
                text = "本月结余",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // 结余金额
            Text(
                text = formatAmount(balance),
                style = AmountTextStyle,
                color = BalanceBlue,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 收入与支出
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入
                Column {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatAmount(totalIncome),
                        style = MaterialTheme.typography.titleMedium,
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 支出
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatAmount(totalExpense),
                        style = MaterialTheme.typography.titleMedium,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 分类标签芯片
 *
 * @param label 标签文本
 * @param selected 是否选中
 * @param modifier 修饰符
 * @param onClick 点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        modifier = modifier
    )
}

/**
 * 空状态占位组件
 *
 * @param title 标题
 * @param modifier 修饰符
 * @param message 描述信息（可选）
 * @param icon 图标
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Filled.Inbox
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 月份选择器
 * 左右箭头切换月份，中间显示当前年月
 *
 * @param year 年份
 * @param month 月份 (1-12)
 * @param modifier 修饰符
 * @param onPreviousMonth 点击上个月回调
 * @param onNextMonth 点击下个月回调
 */
@Composable
fun MonthSelector(
    year: Int,
    month: Int,
    modifier: Modifier = Modifier,
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "上个月"
            )
        }

        Text(
            text = "${year}年${month}月",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "下个月"
            )
        }
    }
}
