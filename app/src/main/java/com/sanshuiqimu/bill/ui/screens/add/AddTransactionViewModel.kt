package com.sanshuiqimu.bill.ui.screens.add

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

/**
 * 新增 / 编辑交易 ViewModel
 *
 * 负责维护表单状态（类型、金额、分类、描述、日期），并执行保存与加载逻辑。
 *
 * - 新增模式：表单初始为默认值，调用 [saveTransaction] 执行插入。
 * - 编辑模式：调用 [loadTransaction] 根据 id 加载已有记录并回填表单，
 *   保存时执行更新。
 *
 * 保存校验：金额必须大于 0、分类不能为空；金额以「分」为单位存储，
 * 通过 `(amount.toDouble() * 100).roundToLong()` 转换。
 *
 * @param repository 记账本数据仓库
 */
class AddTransactionViewModel(
    private val repository: BillRepository
) : ViewModel() {

    /** 当前编辑中的交易记录（新增模式下为 null） */
    private var editingTransaction: TransactionEntity? = null

    /** 交易类型，默认支出 */
    private val _transactionType = MutableStateFlow(TransactionType.EXPENSE)
    val transactionType: StateFlow<TransactionType> = _transactionType.asStateFlow()

    /** 金额输入文本（仅包含数字与小数点） */
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    /** 选中的分类名称 */
    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    /** 备注描述 */
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    /** 交易日期时间戳（毫秒），默认为当前时间 */
    private val _date = MutableStateFlow(System.currentTimeMillis())
    val date: StateFlow<Long> = _date.asStateFlow()

    /**
     * 按当前交易类型过滤后的分类列表。
     *
     * 当 [transactionType] 变化时，自动重新订阅对应类型的分类数据流。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<CategoryEntity>> = _transactionType
        .flatMapLatest { type -> repository.getCategoriesByType(type) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = emptyList()
        )

    /** 切换交易类型，同时清空已选分类（避免跨类型无效选择） */
    fun setTransactionType(type: TransactionType) {
        if (_transactionType.value != type) {
            _transactionType.value = type
            _category.value = ""
        }
    }

    /**
     * 设置金额，仅保留数字与小数点，并限制最多两位小数、一个小数点。
     */
    fun setAmount(value: String) {
        // 只保留数字与小数点
        val digitsAndDots = value.filter { it.isDigit() || it == '.' }

        // 最多一个小数点：取第一个小数点之前 + 之后的内容
        val sanitized = if (digitsAndDots.count { it == '.' } > 1) {
            val firstDotIndex = digitsAndDots.indexOf('.')
            digitsAndDots.substring(0, firstDotIndex + 1) +
                digitsAndDots.substring(firstDotIndex + 1).replace(".", "")
        } else {
            digitsAndDots
        }

        // 小数部分最多两位
        val result = if (sanitized.contains('.')) {
            val dotIndex = sanitized.indexOf('.')
            val intPart = sanitized.substring(0, dotIndex)
            val decimalPart = sanitized.substring(dotIndex + 1).take(2)
            "$intPart.$decimalPart"
        } else {
            sanitized
        }

        _amount.value = result
    }

    /** 设置选中的分类名称 */
    fun setCategory(value: String) {
        _category.value = value
    }

    /** 设置备注描述 */
    fun setDescription(value: String) {
        _description.value = value
    }

    /** 设置交易日期时间戳（毫秒） */
    fun setDate(timestamp: Long) {
        _date.value = timestamp
    }

    /**
     * 保存当前交易记录（新增或更新）。
     *
     * 校验规则：
     * - 金额必须能解析为数字且大于 0
     * - 分类不能为空
     *
     * 金额转换为分：`amountCents = (amount.toDouble() * 100).roundToLong()`。
     *
     * @return true 表示保存成功，false 表示校验失败或保存异常
     */
    suspend fun saveTransaction(): Boolean {
        val amountValue = _amount.value.toDoubleOrNull() ?: return false
        if (amountValue <= 0.0) return false
        if (_category.value.isBlank()) return false

        val amountCents = (amountValue * 100).roundToLong()
        val type = _transactionType.value
        val category = _category.value
        val description = _description.value.ifBlank { null }
        val date = _date.value

        return try {
            val existing = editingTransaction
            if (existing != null) {
                // 编辑模式：更新已有记录
                repository.updateTransaction(
                    existing.copy(
                        type = type,
                        amountCents = amountCents,
                        category = category,
                        description = description,
                        date = date
                    )
                )
            } else {
                // 新增模式：插入新记录
                repository.insertTransaction(
                    TransactionEntity(
                        type = type,
                        amountCents = amountCents,
                        category = category,
                        description = description,
                        date = date
                    )
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 加载已有交易记录用于编辑（编辑模式）。
     *
     * @param id 交易记录主键
     */
    fun loadTransaction(id: String) {
        viewModelScope.launch {
            repository.getTransactionById(id)?.let { transaction ->
                editingTransaction = transaction
                _transactionType.value = transaction.type
                _amount.value = centsToEditableString(transaction.amountCents)
                _category.value = transaction.category
                _description.value = transaction.description.orEmpty()
                _date.value = transaction.date
            }
        }
    }

    companion object {

        /** StateFlow 订阅保活超时时间（毫秒） */
        private const val SUBSCRIPTION_TIMEOUT_MS = 5000L

        /**
         * 将金额（分）转换为可编辑的字符串。
         *
         * - 整数金额（如 12300 分 → 123 元）返回 "123"
         * - 带小数金额（如 12345 分 → 123.45 元）返回 "123.45"
         */
        private fun centsToEditableString(cents: Long): String {
            val whole = cents / 100
            val fraction = cents % 100
            return if (fraction == 0L) {
                whole.toString()
            } else {
                "$whole.${fraction.toString().padStart(2, '0')}"
            }
        }

        /**
         * ViewModel 工厂方法。
         *
         * 从 [Application] 中获取数据库单例并构建 [BillRepository]，
         * 然后注入到 [AddTransactionViewModel] 中。
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = BillDatabase.getInstance(application)
                val repository = BillRepository(
                    transactionDao = database.transactionDao(),
                    categoryDao = database.categoryDao()
                )
                AddTransactionViewModel(repository)
            }
        }
    }
}
