package com.sanshuiqimu.bill.ui.screens.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.sanshuiqimu.bill.data.BillDatabase
import com.sanshuiqimu.bill.data.entity.CategoryEntity
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
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 首页 ViewModel
 *
 * 持有 [BillRepository] 引用，负责向首页 UI 提供：
 * - 当前选中月份的交易列表与收支统计
 * - 所有交易、所有分类的数据流
 * - 月份切换、删除交易等用户操作
 *
 * 月份切换基于 [selectedYear] / [selectedMonth] 两个 [StateFlow]，
 * 当它们变化时通过 [flatMapLatest] 重新订阅对应月份的数据流。
 *
 * @param repository 记账本数据仓库
 */
class HomeViewModel(
    private val repository: BillRepository
) : ViewModel() {

    /** 当前选中年份 */
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    /** 当前选中月份（1-12） */
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    /** 所有交易记录的数据流（按日期降序） */
    val allTransactions: Flow<List<TransactionEntity>> = repository.allTransactions

    /** 所有分类的数据流 */
    val categories: Flow<List<CategoryEntity>> = repository.allCategories

    /**
     * 当前选中月份的交易列表数据流。
     *
     * 当 [selectedYear] 或 [selectedMonth] 变化时，自动切换到新月份的数据流。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTransactions: Flow<List<TransactionEntity>> =
        combine(_selectedYear, _selectedMonth) { year, month -> year to month }
            .flatMapLatest { (year, month) ->
                repository.getTransactionsByMonth(year, month)
            }

    /**
     * 当前选中月份的收支统计（私有），由 [monthlyTransactions] 派生而来。
     */
    private val monthlyTotals: StateFlow<MonthlyTotals> =
        monthlyTransactions
            .map { transactions ->
                val incomeCents = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amountCents }
                val expenseCents = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amountCents }
                MonthlyTotals(
                    income = incomeCents / 100.0,
                    expense = expenseCents / 100.0,
                    balance = (incomeCents - expenseCents) / 100.0
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                initialValue = MonthlyTotals(0.0, 0.0, 0.0)
            )

    /** 当月总收入（元） */
    val totalIncome: StateFlow<Double> = monthlyTotals
        .map { it.income }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = 0.0
        )

    /** 当月总支出（元） */
    val totalExpense: StateFlow<Double> = monthlyTotals
        .map { it.expense }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = 0.0
        )

    /** 当月结余（元）= 收入 - 支出 */
    val balance: StateFlow<Double> = monthlyTotals
        .map { it.balance }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = 0.0
        )

    /**
     * 切换月份。
     *
     * @param delta 月份增量，正数为下个月，负数为上个月（自动处理跨年）
     */
    fun changeMonth(delta: Int) {
        var newMonth = _selectedMonth.value + delta
        var newYear = _selectedYear.value
        while (newMonth < 1) {
            newMonth += 12
            newYear -= 1
        }
        while (newMonth > 12) {
            newMonth -= 12
            newYear += 1
        }
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
    }

    /**
     * 删除一条交易记录。
     *
     * @param transaction 待删除的交易记录
     */
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    /**
     * 月度收支统计内部数据载体。
     *
     * @property income   当月总收入（元）
     * @property expense  当月总支出（元）
     * @property balance  当月结余（元）
     */
    private data class MonthlyTotals(
        val income: Double,
        val expense: Double,
        val balance: Double
    )

    companion object {

        /** StateFlow 订阅保活超时时间（毫秒） */
        private const val SUBSCRIPTION_TIMEOUT_MS = 5000L

        /**
         * ViewModel 工厂方法。
         *
         * 从 [Application] 中获取数据库单例并构建 [BillRepository]，
         * 然后注入到 [HomeViewModel] 中，供 `viewModel(factory = ...)` 使用。
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = BillDatabase.getInstance(application)
                val repository = BillRepository(
                    transactionDao = database.transactionDao(),
                    categoryDao = database.categoryDao()
                )
                HomeViewModel(repository)
            }
        }
    }
}
