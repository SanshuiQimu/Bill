package com.sanshuiqimu.bill.ui.screens.stats

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.sanshuiqimu.bill.data.BillDatabase
import com.sanshuiqimu.bill.data.entity.TransactionEntity
import com.sanshuiqimu.bill.data.repository.BillRepository
import com.sanshuiqimu.bill.util.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/**
 * 分类统计项
 */
data class CategoryStat(
    val category: String,
    val amountCents: Long,
    val percentage: Float
)

/**
 * 统计报表 ViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val repository: BillRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow<Int?>(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int?> = _selectedMonth.asStateFlow()

    /** 是否年度模式（month为null时） */
    val isYearlyMode: StateFlow<Boolean> = _selectedMonth
        .map { it == null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    /** 当前过滤的交易列表 */
    private val filteredTransactions: Flow<List<TransactionEntity>> =
        combine(_selectedYear, _selectedMonth) { year, month -> year to month }
            .flatMapLatest { (year, month) ->
                if (month != null) {
                    repository.getTransactionsByMonth(year, month)
                } else {
                    repository.allTransactions.map { list ->
                        list.filter { txn ->
                            val cal = Calendar.getInstance().apply { timeInMillis = txn.date }
                            cal.get(Calendar.YEAR) == year
                        }
                    }
                }
            }

    /** 总收入（元） */
    val totalIncome: StateFlow<Double> = filteredTransactions
        .map { list ->
            list.filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountCents } / 100.0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    /** 总支出（元） */
    val totalExpense: StateFlow<Double> = filteredTransactions
        .map { list ->
            list.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountCents } / 100.0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    /** 结余（元） */
    val balance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    /** 交易总数 */
    val transactionCount: StateFlow<Int> = filteredTransactions
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    /** 支出分类统计（按金额降序） */
    val expenseByCategory: StateFlow<List<CategoryStat>> = filteredTransactions
        .map { list ->
            val expenses = list.filter { it.type == TransactionType.EXPENSE }
            val total = expenses.sumOf { it.amountCents }
            expenses.groupBy { it.category }
                .map { (cat, items) ->
                    val amount = items.sumOf { it.amountCents }
                    CategoryStat(
                        category = cat,
                        amountCents = amount,
                        percentage = if (total > 0) amount.toFloat() / total.toFloat() else 0f
                    )
                }
                .sortedByDescending { it.amountCents }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /** 收入分类统计（按金额降序） */
    val incomeByCategory: StateFlow<List<CategoryStat>> = filteredTransactions
        .map { list ->
            val incomes = list.filter { it.type == TransactionType.INCOME }
            val total = incomes.sumOf { it.amountCents }
            incomes.groupBy { it.category }
                .map { (cat, items) ->
                    val amount = items.sumOf { it.amountCents }
                    CategoryStat(
                        category = cat,
                        amountCents = amount,
                        percentage = if (total > 0) amount.toFloat() / total.toFloat() else 0f
                    )
                }
                .sortedByDescending { it.amountCents }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /** 年度模式时的月度统计 */
    val monthlyStats: Flow<List<com.sanshuiqimu.bill.data.dao.MonthlyStat>> = repository.getMonthlyStats()

    /** 切换月份 */
    fun changeMonth(delta: Int) {
        val currentMonth = _selectedMonth.value ?: Calendar.getInstance().get(Calendar.MONTH) + 1
        var newMonth = currentMonth + delta
        var newYear = _selectedYear.value
        while (newMonth < 1) { newMonth += 12; newYear -= 1 }
        while (newMonth > 12) { newMonth -= 12; newYear += 1 }
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
    }

    /** 切换月度/年度模式 */
    fun toggleYearlyMode() {
        _selectedMonth.value = if (_selectedMonth.value == null) {
            Calendar.getInstance().get(Calendar.MONTH) + 1
        } else {
            null
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = BillDatabase.getInstance(application)
                val repository = BillRepository(
                    transactionDao = database.transactionDao(),
                    categoryDao = database.categoryDao()
                )
                StatsViewModel(repository)
            }
        }
    }
}
