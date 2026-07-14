package com.sanshuiqimu.bill.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sanshuiqimu.bill.data.entity.TransactionEntity
import com.sanshuiqimu.bill.util.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 月度收支统计数据模型
 *
 * 由 [TransactionDao.getMonthlyStats] 查询返回，按年月分组统计收支总额。
 *
 * @property year              年份
 * @property month             月份（1-12）
 * @property totalIncomeCents  当月收入总额（分）
 * @property totalExpenseCents 当月支出总额（分）
 */
data class MonthlyStat(
    val year: Int,
    val month: Int,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long
) {
    /** 当月结余（收入 - 支出），单位：分 */
    val balanceCents: Long
        get() = totalIncomeCents - totalExpenseCents
}

/**
 * 交易记录数据访问对象
 *
 * 提供对 `transactions` 表的增删改查及统计操作。
 */
@Dao
interface TransactionDao {

    /**
     * 查询所有交易记录，按日期降序排列。
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * 按交易类型查询记录，按日期降序排列。
     *
     * @param type 交易类型（INCOME / EXPENSE）
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    /**
     * 按年月查询交易记录，按日期降序排列。
     *
     * 使用 SQLite 的 strftime 从毫秒时间戳中提取年份和月份，
     * 并转换为本地时区进行比较。
     *
     * @param year  年份（如 2026）
     * @param month 月份（1-12）
     */
    @Query(
        """
        SELECT * FROM transactions
        WHERE CAST(strftime('%Y', date / 1000, 'unixepoch', 'localtime') AS INTEGER) = :year
          AND CAST(strftime('%m', date / 1000, 'unixepoch', 'localtime') AS INTEGER) = :month
        ORDER BY date DESC
        """
    )
    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<TransactionEntity>>

    /**
     * 插入一条交易记录。
     */
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    /**
     * 更新一条交易记录。
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * 删除一条交易记录。
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    /**
     * 根据主键 ID 查询单条交易记录。
     *
     * @param id 交易记录的唯一标识
     * @return 匹配的交易记录，不存在时返回 null
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    /**
     * 获取所有月份的收支统计数据。
     *
     * 按年月分组，分别汇总收入和支出总额，
     * 结果按年份降序、月份降序排列。
     *
     * @return 月度统计列表的数据流
     */
    @Query(
        """
        SELECT
            CAST(strftime('%Y', date / 1000, 'unixepoch', 'localtime') AS INTEGER) AS year,
            CAST(strftime('%m', date / 1000, 'unixepoch', 'localtime') AS INTEGER) AS month,
            SUM(CASE WHEN type = 'INCOME'  THEN amountCents ELSE 0 END) AS totalIncomeCents,
            SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END) AS totalExpenseCents
        FROM transactions
        GROUP BY year, month
        ORDER BY year DESC, month DESC
        """
    )
    fun getMonthlyStats(): Flow<List<MonthlyStat>>

    /**
     * 清空所有交易记录。
     */
    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}
