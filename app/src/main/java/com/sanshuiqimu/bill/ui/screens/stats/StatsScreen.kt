package com.sanshuiqimu.bill.ui.screens.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanshuiqimu.bill.ui.components.EmptyState
import com.sanshuiqimu.bill.ui.components.SummaryCard
import com.sanshuiqimu.bill.ui.theme.BalanceBlue
import com.sanshuiqimu.bill.ui.theme.ExpenseRed
import com.sanshuiqimu.bill.ui.theme.IncomeGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val isYearly by viewModel.isYearlyMode.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val transactionCount by viewModel.transactionCount.collectAsStateWithLifecycle()
    val expenseByCategory by viewModel.expenseByCategory.collectAsStateWithLifecycle()
    val incomeByCategory by viewModel.incomeByCategory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("统计报表") })
        }
    ) { innerPadding ->
        if (transactionCount == 0) {
            EmptyState(
                title = "暂无数据",
                message = "当前时间段没有交易记录",
                icon = Icons.Filled.BarChart,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 模式切换
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = !isYearly,
                        onClick = { if (isYearly) viewModel.toggleYearlyMode() },
                        label = { Text("月度") }
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    FilterChip(
                        selected = isYearly,
                        onClick = { if (!isYearly) viewModel.toggleYearlyMode() },
                        label = { Text("年度") }
                    )
                }
            }

            // 时间选择器
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        if (isYearly) {
                            viewModel.toggleYearlyMode()
                            viewModel.changeMonth(-12)
                            viewModel.toggleYearlyMode()
                        } else {
                            viewModel.changeMonth(-1)
                        }
                    }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "上一个")
                    }
                    Text(
                        text = if (isYearly) "${selectedYear}年" else "${selectedYear}年${selectedMonth}月",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = {
                        if (isYearly) {
                            viewModel.toggleYearlyMode()
                            viewModel.changeMonth(12)
                            viewModel.toggleYearlyMode()
                        } else {
                            viewModel.changeMonth(1)
                        }
                    }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "下一个")
                    }
                }
            }

            // 总览卡片
            item {
                SummaryCard(totalIncome = totalIncome, totalExpense = totalExpense)
            }

            // 支出分类统计
            if (expenseByCategory.isNotEmpty()) {
                item {
                    Text(
                        text = "支出分类",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(expenseByCategory) { stat ->
                    CategoryStatRow(stat = stat, typeColor = ExpenseRed)
                }
            }

            // 收入分类统计
            if (incomeByCategory.isNotEmpty()) {
                item {
                    Text(
                        text = "收入分类",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(incomeByCategory) { stat ->
                    CategoryStatRow(stat = stat, typeColor = IncomeGreen)
                }
            }
        }
    }
}

@Composable
private fun CategoryStatRow(
    stat: CategoryStat,
    typeColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.2f 元", stat.amountCents / 100.0),
                    style = MaterialTheme.typography.titleSmall,
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { stat.percentage },
                modifier = Modifier.fillMaxWidth(),
                color = typeColor,
                trackColor = typeColor.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.1f%%", stat.percentage * 100),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
