package com.sanshuiqimu.bill.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanshuiqimu.bill.util.TransactionType
import java.util.UUID

/**
 * 交易记录实体
 *
 * 对应数据库表 `transactions`，记录每一笔收入或支出。
 *
 * @property id          主键，使用自动生成的 UUID 字符串
 * @property type        交易类型（收入 / 支出）
 * @property amountCents 金额（以"分"为单位存储，避免浮点精度问题）
 * @property category    分类名称
 * @property description 备注描述，可为空
 * @property date        交易发生日期的时间戳（毫秒）
 * @property createdAt   记录创建时间的时间戳（毫秒）
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val amountCents: Long,
    val category: String,
    val description: String? = null,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
