package com.sanshuiqimu.bill.ui.screens.group

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: GroupViewModel = viewModel(factory = GroupViewModel.Factory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 错误提示
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(state.errorMessage)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.status == GroupStatus.JOINED) "共享记账本" else "小组共享记账") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (state.status == GroupStatus.JOINED) {
                        IconButton(onClick = { viewModel.pullSync() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "同步")
                        }
                        IconButton(onClick = {
                            viewModel.leaveGroup()
                            Toast.makeText(context, "已退出小组", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.Logout, contentDescription = "退出小组")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        if (state.status == GroupStatus.NOT_JOINED) {
            JoinGroupContent(
                viewModel = viewModel,
                isLoading = state.isLoading,
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = 100.dp)
            )
        } else {
            SharedGroupContent(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = 100.dp)
            )
        }
    }
}

// === 未加入小组: 选择创建或加入 ===

@Composable
private fun JoinGroupContent(
    viewModel: GroupViewModel,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(0) } // 0=none, 1=join, 2=create

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 标题
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "小组共享记账",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "输入邀请码加入小组，多人实时同步记账",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when (mode) {
            0 -> {
                item {
                    Button(
                        onClick = { mode = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("加入小组")
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { mode = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("创建新小组")
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("试试体验", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "输入邀请码 FAMILY2026 或 TRIP2026 体验共享记账",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            1 -> {
                item { JoinForm(viewModel, isLoading) { mode = 0 } }
            }

            2 -> {
                item { CreateForm(viewModel, isLoading) { mode = 0 } }
            }
        }
    }
}

@Composable
private fun JoinForm(
    viewModel: GroupViewModel,
    isLoading: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var inviteCode by remember { mutableStateOf("") }
    var myName by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("🧑") }

    val avatars = listOf("🧑", "👩", "👨", "👵", "👴", "🧒", "👧", "👦")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("加入小组", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase().take(10) },
                label = { Text("邀请码") },
                placeholder = { Text("输入6位邀请码") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = myName,
                onValueChange = { myName = it.take(12) },
                label = { Text("你的昵称") },
                placeholder = { Text("在小组中显示的名字") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("选择头像", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                avatars.forEach { a ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (avatar == a) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            a,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("返回") }
                Button(
                    onClick = { viewModel.joinGroup(inviteCode, myName, avatar) },
                    enabled = inviteCode.isNotBlank() && myName.isNotBlank() && !isLoading,
                    modifier = Modifier.weight(1f)
                ) { Text("加入") }
            }
        }
    }
}

@Composable
private fun CreateForm(
    viewModel: GroupViewModel,
    isLoading: Boolean,
    onBack: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var myName by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("🧑") }

    val avatars = listOf("🧑", "👩", "👨", "👵", "👴", "🧒", "👧", "👦")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("创建新小组", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it.take(20) },
                label = { Text("小组名称") },
                placeholder = { Text("如：家庭记账、旅行AA") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = myName,
                onValueChange = { myName = it.take(12) },
                label = { Text("你的昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("选择头像", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                avatars.forEach { a ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (avatar == a) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(a, fontSize = 22.sp)
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("返回") }
                Button(
                    onClick = { viewModel.createGroup(groupName, myName, avatar) },
                    enabled = groupName.isNotBlank() && myName.isNotBlank() && !isLoading,
                    modifier = Modifier.weight(1f)
                ) { Text("创建") }
            }
        }
    }
}

// === 已加入小组: 共享记账界面 ===

@Composable
private fun SharedGroupContent(
    state: GroupState,
    viewModel: GroupViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    val totalExpense = state.sharedTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalIncome = state.sharedTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 小组信息 + 邀请码
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Group, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(state.groupName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("成员 ${state.members.size} 人", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 邀请码展示 (可复制)
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("邀请码", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    state.inviteCode,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 4.sp
                                )
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("邀请码", state.inviteCode))
                                Toast.makeText(context, "邀请码已复制", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "复制邀请码")
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 成员列表
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        state.members.forEach { member ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) { Text(member.avatar, fontSize = 24.sp) }
                                Spacer(Modifier.height(4.dp))
                                Text(member.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                                Text("¥${"%.0f".format(member.contributedAmount)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 实时同步状态
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isSyncing) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CloudSync, null,
                        tint = if (state.isSyncing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (state.isSyncing) "正在同步..." else "实时同步",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (state.lastSyncTime.isNotEmpty()) "上次同步: ${state.lastSyncTime}" else "等待同步...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        Modifier.size(10.dp).clip(CircleShape)
                            .background(if (state.isSyncing) Color(0xFFFF9F0A) else Color(0xFF04B285))
                    )
                }
            }
        }

        // 收支汇总
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFFF375F).copy(alpha = 0.1f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("总支出", style = MaterialTheme.typography.labelMedium)
                        Text("¥${"%.2f".format(totalExpense)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFFF375F))
                    }
                }
                Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF04B285).copy(alpha = 0.1f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("总收入", style = MaterialTheme.typography.labelMedium)
                        Text("¥${"%.2f".format(totalIncome)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF04B285))
                    }
                }
            }
        }

        // 添加记账按钮
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("记一笔到共享账本")
            }
        }

        // 交易记录标题
        item {
            Text("共享交易记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        }

        // 交易记录列表
        if (state.sharedTransactions.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("暂无交易记录\n点击上方按钮开始记账", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(state.sharedTransactions) { txn ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text(txn.memberAvatar, fontSize = 20.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(txn.note, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("${txn.memberName} · ${txn.category} · ${txn.time}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "${if (txn.type == "EXPENSE") "-" else "+"}¥${"%.2f".format(txn.amount)}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                            color = if (txn.type == "EXPENSE") Color(0xFFFF375F) else Color(0xFF04B285)
                        )
                    }
                }
            }
        }
    }

    // 添加交易对话框
    if (showAddDialog) {
        AddSharedTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, category, note, type ->
                viewModel.addTransaction(amount, category, note, type)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddSharedTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("餐饮") }
    var note by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }

    val categories = if (isIncome) listOf("工资", "奖金", "退款", "其他")
    else listOf("餐饮", "购物", "交通", "住宿", "娱乐", "医疗", "其他")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记一笔到共享账本") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("支出")
                    androidx.compose.material3.Switch(checked = isIncome, onCheckedChange = { isIncome = it })
                    Text("收入")
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() || c == '.' }.take(10) },
                    label = { Text("金额") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilledTonalButton(
                            onClick = { category = cat },
                            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (category == cat) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                            )
                        ) { Text(cat, fontSize = 12.sp) }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(30) },
                    label = { Text("备注") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, category, note.ifBlank { category }, if (isIncome) "INCOME" else "EXPENSE")
                    }
                },
                enabled = amountStr.isNotBlank()
            ) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
