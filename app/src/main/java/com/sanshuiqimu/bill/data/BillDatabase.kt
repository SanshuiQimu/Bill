package com.sanshuiqimu.bill.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanshuiqimu.bill.data.dao.CategoryDao
import com.sanshuiqimu.bill.data.dao.TransactionDao
import com.sanshuiqimu.bill.data.entity.CategoryEntity
import com.sanshuiqimu.bill.data.entity.Converters
import com.sanshuiqimu.bill.data.entity.TransactionEntity

/**
 * 应用主数据库
 *
 * 包含两张表：
 * - [TransactionEntity] → `transactions`（交易记录）
 * - [CategoryEntity]    → `categories`（分类）
 *
 * 使用 [Converters] 处理 [com.sanshuiqimu.bill.util.TransactionType] 枚举的持久化。
 * 在首次创建数据库时，通过 [RoomDatabase.Callback] 自动预填充 12 个默认分类。
 */
@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BillDatabase : RoomDatabase() {

    /** 交易记录 DAO */
    abstract fun transactionDao(): TransactionDao

    /** 分类 DAO */
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: BillDatabase? = null

        /**
         * 获取数据库单例实例（双重检查锁定）。
         *
         * 首次调用时会创建数据库并注册回调，数据库首次创建时自动预填充默认分类。
         *
         * @param context 任意 Context，内部会使用 applicationContext 避免内存泄漏
         * @return [BillDatabase] 单例
         */
        fun getInstance(context: Context): BillDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * 构建数据库实例并注册创建回调。
         */
        private fun buildDatabase(context: Context): BillDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BillDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        populateDefaultCategories(db)
                    }
                })
                .build()
        }

        /**
         * 在数据库首次创建时预填充默认分类。
         *
         * 通过 [SupportSQLiteDatabase] 直接执行原生插入操作，
         * 避免在数据库尚未完全初始化时产生 DAO 循环依赖。
         */
        private fun populateDefaultCategories(db: SupportSQLiteDatabase) {
            val categories = CategoryEntity.getDefaultCategories()
            categories.forEach { category ->
                val values = ContentValues().apply {
                    put(COLUMN_NAME, category.name)
                    put(COLUMN_TYPE, category.type.name)
                    put(COLUMN_ICON, category.icon)
                }
                db.insert(
                    TABLE_CATEGORIES,
                    SQLiteDatabase.CONFLICT_IGNORE,
                    values
                )
            }
        }

        private const val DATABASE_NAME = "bill_database"
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_ICON = "icon"
    }
}
