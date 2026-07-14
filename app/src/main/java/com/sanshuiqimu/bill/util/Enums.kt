package com.sanshuiqimu.bill.util

/**
 * 交易类型枚举
 *
 * @property displayName 用于 UI 展示的中文名称
 */
enum class TransactionType(val displayName: String) {
    INCOME("收入"),
    EXPENSE("支出");

    companion object {
        /**
         * 将字符串安全地转换为 [TransactionType]。
         *
         * @param value 枚举名称字符串（大小写不敏感），为 null 或无法匹配时返回 null
         * @return 对应的 [TransactionType]，若无法匹配则返回 null
         */
        fun fromString(value: String?): TransactionType? {
            if (value.isNullOrBlank()) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
