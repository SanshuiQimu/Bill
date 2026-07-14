package com.sanshuiqimu.bill.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanshuiqimu.bill.util.TransactionType

/**
 * 分类实体
 *
 * 对应数据库表 `categories`，用于定义收支分类。
 *
 * @property id   自增主键
 * @property name 分类名称
 * @property type 分类对应的交易类型（收入 / 支出）
 * @property icon 分类图标，存储 emoji 字符串，可为空
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val icon: String? = null
) {
    companion object {
        /**
         * 获取应用默认的 12 个分类（8 个支出 + 4 个收入）。
         *
         * 支出分类：餐饮、交通、购物、住房、娱乐、医疗、教育、其他支出
         * 收入分类：工资、奖金、投资收益、其他收入
         *
         * @return 默认分类列表
         */
        fun getDefaultCategories(): List<CategoryEntity> {
            return listOf(
                // 支出分类
                CategoryEntity(name = "餐饮", type = TransactionType.EXPENSE, icon = "🍜"),
                CategoryEntity(name = "交通", type = TransactionType.EXPENSE, icon = "🚗"),
                CategoryEntity(name = "购物", type = TransactionType.EXPENSE, icon = "🛒"),
                CategoryEntity(name = "住房", type = TransactionType.EXPENSE, icon = "🏠"),
                CategoryEntity(name = "娱乐", type = TransactionType.EXPENSE, icon = "🎮"),
                CategoryEntity(name = "医疗", type = TransactionType.EXPENSE, icon = "💊"),
                CategoryEntity(name = "教育", type = TransactionType.EXPENSE, icon = "📚"),
                CategoryEntity(name = "其他支出", type = TransactionType.EXPENSE, icon = "📦"),
                // 收入分类
                CategoryEntity(name = "工资", type = TransactionType.INCOME, icon = "💰"),
                CategoryEntity(name = "奖金", type = TransactionType.INCOME, icon = "🎁"),
                CategoryEntity(name = "投资收益", type = TransactionType.INCOME, icon = "📈"),
                CategoryEntity(name = "其他收入", type = TransactionType.INCOME, icon = "💵")
            )
        }
    }
}
