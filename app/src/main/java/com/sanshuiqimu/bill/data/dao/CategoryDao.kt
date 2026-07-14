package com.sanshuiqimu.bill.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sanshuiqimu.bill.data.entity.CategoryEntity
import com.sanshuiqimu.bill.util.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 分类数据访问对象
 *
 * 提供对 `categories` 表的增删改查操作。
 */
@Dao
interface CategoryDao {

    /**
     * 查询所有分类，按 id 升序排列。
     */
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * 按交易类型查询分类，按 id 升序排列。
     *
     * @param type 交易类型（INCOME / EXPENSE）
     */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    /**
     * 插入单个分类。
     *
     * @return 新插入行的主键 id
     */
    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    /**
     * 批量插入分类列表。
     */
    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    /**
     * 删除指定分类。
     */
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    /**
     * 查询当前分类总数。
     *
     * @return 分类记录条数
     */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
