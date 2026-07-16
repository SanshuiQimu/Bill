package com.sanshuiqimu.bill.ui.screens.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GroupMember(
    val name: String,
    val avatar: String,
    val contributedAmount: Double
)

data class SharedTransaction(
    val id: String,
    val memberName: String,
    val memberAvatar: String,
    val amount: Double,
    val category: String,
    val note: String,
    val type: String,
    val time: String
)

data class GroupState(
    val groupName: String = "家庭共享记账本",
    val groupId: String = "GRP-2026-0716",
    val members: List<GroupMember> = listOf(
        GroupMember("我", "🧑", 3280.50),
        GroupMember("伴侣", "👩", 2150.00),
        GroupMember("妈妈", "👵", 890.30)
    ),
    val sharedTransactions: List<SharedTransaction> = listOf(
        SharedTransaction("1", "我", "🧑", 68.50, "餐饮", "午餐外卖", "EXPENSE", "今天 12:30"),
        SharedTransaction("2", "伴侣", "👩", 320.00, "购物", "超市采购", "EXPENSE", "今天 10:15"),
        SharedTransaction("3", "妈妈", "👵", 5000.00, "工资", "退休金", "INCOME", "昨天 09:00"),
        SharedTransaction("4", "我", "🧑", 200.00, "交通", "加油", "EXPENSE", "昨天 18:45"),
        SharedTransaction("5", "伴侣", "👩", 128.90, "餐饮", "晚餐", "EXPENSE", "07-14 19:20")
    ),
    val lastSyncTime: String = "",
    val isSyncing: Boolean = false
)

class GroupViewModel : ViewModel() {
    private val _state = MutableStateFlow(GroupState())
    val state: StateFlow<GroupState> = _state.asStateFlow()

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        // 初始同步
        syncNow()
        // 模拟实时同步: 每30秒更新一次同步时间
        viewModelScope.launch {
            while (true) {
                delay(30000)
                syncNow()
            }
        }
    }

    fun syncNow() {
        _state.value = _state.value.copy(
            isSyncing = true
        )
        viewModelScope.launch {
            delay(800) // 模拟网络延迟
            _state.value = _state.value.copy(
                isSyncing = false,
                lastSyncTime = timeFormat.format(Date())
            )
        }
    }

    fun addTransaction(amount: Double, category: String, note: String, type: String) {
        val current = _state.value
        val newTxn = SharedTransaction(
            id = System.currentTimeMillis().toString(),
            memberName = "我",
            memberAvatar = "🧑",
            amount = amount,
            category = category,
            note = note,
            type = type,
            time = SimpleDateFormat("今天 HH:mm", Locale.getDefault()).format(Date())
        )
        _state.value = current.copy(
            sharedTransactions = listOf(newTxn) + current.sharedTransactions
        )
        syncNow()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GroupViewModel() as T
        }
    }
}
