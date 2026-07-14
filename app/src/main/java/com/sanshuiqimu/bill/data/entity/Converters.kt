package com.sanshuiqimu.bill.data.entity

import androidx.room.TypeConverter
import com.sanshuiqimu.bill.util.TransactionType

/**
 * Room 类型转换器
 *
 * 负责在 [TransactionType] 枚举与数据库中的 String 之间进行转换，
 * 使得枚举类型可以被持久化到 SQLite 中。
 */
class Converters {

    /**
     * 将 [TransactionType] 枚举转换为数据库可存储的字符串（枚举名称）。
     */
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    /**
     * 将数据库中的字符串还原为 [TransactionType] 枚举。
     *
     * 优先使用 [TransactionType.valueOf] 进行精确匹配，
     * 若匹配失败则回退到 [TransactionType.fromString] 以提高容错性。
     */
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return runCatching { TransactionType.valueOf(value) }
            .getOrElse {
                TransactionType.fromString(value)
                    ?: throw IllegalArgumentException("未知的交易类型: $value")
            }
    }
}
