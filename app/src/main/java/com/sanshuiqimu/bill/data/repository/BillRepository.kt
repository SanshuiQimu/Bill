package com.sanshuiqimu.bill.data.repository

import com.sanshuiqimu.bill.data.dao.CategoryDao
import com.sanshuiqimu.bill.data.dao.MonthlyStat
import com.sanshuiqimu.bill.data.dao.TransactionDao
import com.sanshuiqimu.bill.data.entity.CategoryEntity
import com.sanshuiqimu.bill.data.entity.TransactionEntity
import com.sanshuiqimu.bill.util.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 记账本数据仓库
 *
 * 作为 ViewModel / UI 层与数据层之间的单一数据来源，
 * 封装了 [TransactionDao] 和 [CategoryDao] 的全部操作。
 *
 * 查询类方法返回响应式 [Flow] 数据流，修改类方法使用 suspend 协程函数。
 *
 * @param transactionDao 交易记录 DAO
 * @param categoryDao    分类 DAO
 */
class BillRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    // ===================== 交易记录操作 =====================

    /** 所有交易记录的数据流（按日期降序） */
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    /** 所有分类的数据流 */
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    /**
     * 按交易类型获取记录流。
     */
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByType(type)

    /**
     * 按年月获取交易记录流。
     *
     * @param year  年份（如 2026）
     * @param month 月份（1-12）
     */
    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByMonth(year, month)

    /**
     * 插入一条新的交易记录。
     */
    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    /**
     * 更新一条已有的交易记录。
     */
    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    /**
     * 删除一条交易记录。
     */
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    /**
     * 根据主键 ID 获取单条交易记录。
     *
     * @param id 交易记录唯一标识
     * @return 匹配的记录，不存在时返回 null
     */
    suspend fun getTransactionById(id: String): TransactionEntity? =
        transactionDao.getTransactionById(id)

    /**
     * 获取所有月份的收支统计数据流。
     *
     * @return 按 年/月 分组的收支统计列表
     */
    fun getMonthlyStats(): Flow<List<MonthlyStat>> =
        transactionDao.getMonthlyStats()

    // ===================== 分类操作 =====================

    /**
     * 按交易类型获取分类流。
     */
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    /**
     * 插入一个新分类。
     *
     * @return 新插入行的主键 id
     */
    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    /**
     * 删除指定分类。
     */
    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    /**
     * 查询当前分类总数。
     */
    suspend fun getCategoryCount(): Int =
        categoryDao.getCategoryCount()

    /**
     * 清空所有交易记录（保留分类）。
     */
    suspend fun clearAllTransactions() {
        transactionDao.clearAllTransactions()
    }
}
