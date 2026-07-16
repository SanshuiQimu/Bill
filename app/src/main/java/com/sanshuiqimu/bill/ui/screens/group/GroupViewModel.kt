package com.sanshuiqimu.bill.ui.screens.group

import android.content.Context
import android.content.SharedPreferences
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
import kotlin.random.Random

// === 数据模型 ===

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
    val type: String, // "EXPENSE" | "INCOME"
    val time: String
)

enum class GroupStatus { NOT_JOINED, JOINED }

data class GroupState(
    val status: GroupStatus = GroupStatus.NOT_JOINED,
    val groupName: String = "",
    val inviteCode: String = "",
    val myName: String = "",
    val myAvatar: String = "🧑",
    val members: List<GroupMember> = emptyList(),
    val sharedTransactions: List<SharedTransaction> = emptyList(),
    val lastSyncTime: String = "",
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

class GroupViewModel : ViewModel() {

    private val _state = MutableStateFlow(GroupState())
    val state: StateFlow<GroupState> = _state.asStateFlow()

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val shortTimeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    // 模拟小组数据库 (实际项目中替换为 Supabase / Firebase)
    // 邀请码 → 小组数据
    private val mockGroupDatabase = mutableMapOf<String, MockGroupData>()

    data class MockGroupData(
        val groupName: String,
        val members: MutableList<GroupMember>,
        val transactions: MutableList<SharedTransaction>
    )

    init {
        // 预置一个示例小组
        mockGroupDatabase["FAMILY2026"] = MockGroupData(
            groupName = "家庭共享记账本",
            members = mutableListOf(
                GroupMember("爸爸", "👨", 3280.50),
                GroupMember("妈妈", "👩", 2150.00)
            ),
            transactions = mutableListOf(
                SharedTransaction("1", "爸爸", "👨", 68.50, "餐饮", "午餐外卖", "EXPENSE", "07-16 12:30"),
                SharedTransaction("2", "妈妈", "👩", 320.00, "购物", "超市采购", "EXPENSE", "07-16 10:15"),
                SharedTransaction("3", "爸爸", "👨", 200.00, "交通", "加油", "EXPENSE", "07-15 18:45")
            )
        )
        mockGroupDatabase["TRIP2026"] = MockGroupData(
            groupName = "旅行AA记账",
            members = mutableListOf(
                GroupMember("小明", "🧑", 580.00),
                GroupMember("小红", "👩", 430.00),
                GroupMember("小华", "👨", 650.00)
            ),
            transactions = mutableListOf(
                SharedTransaction("4", "小明", "🧑", 280.00, "住宿", "酒店", "EXPENSE", "07-16 14:00"),
                SharedTransaction("5", "小红", "👩", 150.00, "餐饮", "晚餐聚餐", "EXPENSE", "07-16 19:30"),
                SharedTransaction("6", "小华", "👨", 5000.00, "收入", "旅行基金", "INCOME", "07-15 09:00")
            )
        )

        // 模拟实时同步: 每10秒刷新数据
        viewModelScope.launch {
            while (true) {
                delay(10000)
                if (_state.value.status == GroupStatus.JOINED) {
                    pullSync()
                }
            }
        }
    }

    // === 创建小组 ===
    fun createGroup(groupName: String, myName: String, avatar: String) {
        if (groupName.isBlank() || myName.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "请填写小组名称和你的昵称")
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            delay(800) // 模拟网络请求

            // 生成6位邀请码
            val inviteCode = generateInviteCode()
            val member = GroupMember(myName, avatar, 0.0)

            mockGroupDatabase[inviteCode] = MockGroupData(
                groupName = groupName,
                members = mutableListOf(member),
                transactions = mutableListOf()
            )

            _state.value = _state.value.copy(
                status = GroupStatus.JOINED,
                groupName = groupName,
                inviteCode = inviteCode,
                myName = myName,
                myAvatar = avatar,
                members = listOf(member),
                sharedTransactions = emptyList(),
                isLoading = false,
                lastSyncTime = timeFormat.format(Date())
            )
        }
    }

    // === 通过邀请码加入小组 ===
    fun joinGroup(code: String, myName: String, avatar: String) {
        val upperCode = code.trim().uppercase()
        if (upperCode.isBlank() || myName.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "请填写邀请码和你的昵称")
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = "")

        viewModelScope.launch {
            delay(800) // 模拟网络请求

            val groupData = mockGroupDatabase[upperCode]
            if (groupData == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "邀请码无效，请检查后重试"
                )
                return@launch
            }

            // 添加自己到成员列表
            val newMember = GroupMember(myName, avatar, 0.0)
            groupData.members.add(newMember)

            _state.value = _state.value.copy(
                status = GroupStatus.JOINED,
                groupName = groupData.groupName,
                inviteCode = upperCode,
                myName = myName,
                myAvatar = avatar,
                members = groupData.members.toList(),
                sharedTransactions = groupData.transactions.toList(),
                isLoading = false,
                lastSyncTime = timeFormat.format(Date())
            )
        }
    }

    // === 退出小组 ===
    fun leaveGroup() {
        val current = _state.value
        val groupData = mockGroupDatabase[current.inviteCode]
        groupData?.members?.removeAll { it.name == current.myName }

        _state.value = GroupState()
    }

    // === 添加共享交易 ===
    fun addTransaction(amount: Double, category: String, note: String, type: String) {
        val current = _state.value
        if (current.status != GroupStatus.JOINED) return

        val newTxn = SharedTransaction(
            id = System.currentTimeMillis().toString(),
            memberName = current.myName,
            memberAvatar = current.myAvatar,
            amount = amount,
            category = category,
            note = note,
            type = type,
            time = shortTimeFormat.format(Date())
        )

        // 写入"远程"数据库
        val groupData = mockGroupDatabase[current.inviteCode]
        groupData?.transactions?.add(0, newTxn)

        // 更新成员贡献金额
        if (type == "EXPENSE") {
            groupData?.members?.find { it.name == current.myName }?.let { m ->
                val idx = groupData.members.indexOf(m)
                groupData.members[idx] = m.copy(contributedAmount = m.contributedAmount + amount)
            }
        }

        // 本地立即更新
        _state.value = current.copy(
            sharedTransactions = groupData?.transactions?.toList() ?: listOf(newTxn),
            members = groupData?.members?.toList() ?: current.members
        )

        pullSync()
    }

    // === 拉取同步 ===
    fun pullSync() {
        val current = _state.value
        if (current.status != GroupStatus.JOINED) return

        _state.value = current.copy(isSyncing = true)

        viewModelScope.launch {
            delay(500) // 模拟网络延迟

            val groupData = mockGroupDatabase[current.inviteCode]
            if (groupData != null) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    members = groupData.members.toList(),
                    sharedTransactions = groupData.transactions.toList(),
                    lastSyncTime = timeFormat.format(Date())
                )
            } else {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    lastSyncTime = timeFormat.format(Date())
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = "")
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GroupViewModel() as T
        }
    }
}
