package com.sanshuiqimu.bill.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanshuiqimu.bill.data.entity.TransactionEntity
import com.sanshuiqimu.bill.ui.components.EmptyState
import com.sanshuiqimu.bill.ui.components.MonthSelector
import com.sanshuiqimu.bill.ui.components.SummaryCard
import com.sanshuiqimu.bill.ui.components.TransactionCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 首页
 *
 * 展示当前月份的收支总览、月份切换器以及当月交易列表。
 *
 * 顶部使用 [SummaryCard] 显示收入、支出、结余；[MonthSelector] 用于左右切换月份；
 * 中间使用 [LazyColumn] 渲染当月交易，每项使用 [TransactionCard]，
 * 并支持左滑（从右向左滑）删除交易；列表为空时显示 [EmptyState]。
 *
 * 点击交易卡片会通过 [onNavigateToEditTransaction] 导航到编辑页面，
 * 点击右上角加号通过 [onNavigateToAddTransaction] 导航到新增页面。
 *
 * @param onNavigateToAddTransaction 导航到「记一笔」页面
 * @param onNavigateToEditTransaction 携带交易 id 导航到编辑页面
 * @param viewModel 首页 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val monthlyTransactions by viewModel.monthlyTransactions
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "账单") },
                actions = {
                    IconButton(onClick = onNavigateToAddTransaction) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "记一笔"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 100.dp,
                start = 0.dp,
                end = 0.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 收支总览卡片
            item {
                SummaryCard(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 月份选择器
            item {
                MonthSelector(
                    year = selectedYear,
                    month = selectedMonth,
                    onPreviousMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (monthlyTransactions.isEmpty()) {
                // 空列表占位
                item {
                    EmptyState(
                        title = "本月暂无记录",
                        message = "点击右上角「+」开始记一笔吧",
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            } else {
                // 当月交易列表（支持左滑删除）
                items(
                    items = monthlyTransactions,
                    key = { it.id }
                ) { transaction ->
                    SwipeToDeleteTransactionItem(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(transaction) },
                        onClick = { onNavigateToEditTransaction(transaction.id) }
                    )
                }
            }
        }
    }
}

/**
 * 单条交易项。
 *
 * 使用 [Row] 包裹 [TransactionCard] 与一个删除 [IconButton]；
 * 点击卡片可进入编辑，点击删除按钮触发删除回调。
 *
 * @param transaction 交易记录
 * @param onDelete    删除回调
 * @param onClick     点击回调（进入编辑）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTransactionItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransactionCard(
            category = transaction.category,
            description = transaction.description.orEmpty(),
            amount = transaction.amountCents / 100.0,
            type = transaction.type,
            date = formatDate(transaction.date),
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "删除"
            )
        }
    }
}

/**
 * 将时间戳（毫秒）格式化为展示用日期字符串。
 */
private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}
