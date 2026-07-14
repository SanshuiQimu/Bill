package com.sanshuiqimu.bill.ui.screens.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.sanshuiqimu.bill.data.BillDatabase
import com.sanshuiqimu.bill.data.entity.CategoryEntity
import com.sanshuiqimu.bill.data.repository.BillRepository
import com.sanshuiqimu.bill.util.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置页 ViewModel
 */
class SettingsViewModel(
    private val repository: BillRepository
) : ViewModel() {

    /** 所有分类 */
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /** 交易总数 */
    val transactionCount: StateFlow<Int> = repository.allTransactions
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    /** 支出分类列表 */
    val expenseCategories: StateFlow<List<CategoryEntity>> = categories
        .map { list -> list.filter { it.type == TransactionType.EXPENSE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /** 收入分类列表 */
    val incomeCategories: StateFlow<List<CategoryEntity>> = categories
        .map { list -> list.filter { it.type == TransactionType.INCOME } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /** 添加分类 */
    fun addCategory(name: String, type: TransactionType, icon: String?) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name = name, type = type, icon = icon))
        }
    }

    /** 删除分类 */
    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    /** 清空所有交易记录（保留分类） */
    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllTransactions()
            onComplete()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = BillDatabase.getInstance(application)
                val repository = BillRepository(
                    transactionDao = database.transactionDao(),
                    categoryDao = database.categoryDao()
                )
                SettingsViewModel(repository)
            }
        }
    }
}
