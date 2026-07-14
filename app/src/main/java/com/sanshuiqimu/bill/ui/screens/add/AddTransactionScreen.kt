package com.sanshuiqimu.bill.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanshuiqimu.bill.data.entity.CategoryEntity
import com.sanshuiqimu.bill.ui.components.CategoryChip
import com.sanshuiqimu.bill.ui.theme.ExpenseRed
import com.sanshuiqimu.bill.ui.theme.IncomeGreen
import com.sanshuiqimu.bill.util.TransactionType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 新增 / 编辑交易页面
 *
 * - [transactionId] 为 null 时为新增模式，标题为「记一笔」；
 * - [transactionId] 非 null 时为编辑模式，标题为「编辑记录」，进入时自动加载该记录。
 *
 * 页面包含：类型切换（收入 / 支出）、大字体金额输入（¥ 前缀）、
 * 分类选择（水平滚动芯片）、日期选择（[DatePickerDialog]）、备注输入，
 * 底部为保存按钮，颜色随交易类型变化（收入绿色 / 支出红色）。
 * 保存成功后通过 [onNavigateBack] 返回。
 *
 * @param transactionId 编辑模式下的交易 id，新增模式传 null
 * @param onNavigateBack 返回上一页回调
 * @param viewModel 新增 / 编辑交易 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel(factory = AddTransactionViewModel.Factory)
) {
    val transactionType by viewModel.transactionType.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val date by viewModel.date.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())

    // 编辑模式下加载已有交易
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransaction(transactionId)
        }
    }

    val isEditing = transactionId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "编辑记录" else "记一笔") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 类型切换：收入 / 支出
            TransactionTypeSelector(
                selectedType = transactionType,
                onTypeSelected = viewModel::setTransactionType
            )

            // 金额输入（大字体，¥ 前缀）
            AmountInputField(
                amount = amount,
                type = transactionType,
                onAmountChange = viewModel::setAmount
            )

            // 分类标题
            Text(
                text = "选择分类",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // 分类芯片（水平滚动）
            CategoryChipsRow(
                categories = categories,
                selectedCategory = category,
                onCategorySelected = viewModel::setCategory
            )

            // 日期选择
            DateSelectorRow(
                dateMillis = date,
                onDateSelected = viewModel::setDate
            )

            // 备注输入
            OutlinedTextField(
                value = description,
                onValueChange = viewModel::setDescription,
                label = { Text(text = "备注") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮（颜色随类型变化）
            val buttonColor = if (transactionType == TransactionType.INCOME) IncomeGreen else ExpenseRed
            Button(
                onClick = {
                    scope.launch {
                        val success = viewModel.saveTransaction()
                        if (success) {
                            onNavigateBack()
                        } else {
                            snackbarHostState.showSnackbar("请填写有效金额并选择分类")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isEditing) "保存" else "记一笔",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 交易类型切换器（收入 / 支出）。
 *
 * 自定义的分段控件样式：选中项使用对应类型颜色填充，未选中为透明。
 *
 * @param selectedType   当前选中的类型
 * @param onTypeSelected 类型选中回调
 */
@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TransactionType.entries.forEach { type ->
            val selected = type == selectedType
            val typeColor = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) typeColor else Color.Transparent)
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.displayName,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 金额输入框（大字体，¥ 前缀，仅允许数字与小数点）。
 *
 * @param amount        当前金额文本
 * @param type          当前交易类型（决定 ¥ 符号颜色）
 * @param onAmountChange 金额变更回调
 */
@Composable
private fun AmountInputField(
    amount: String,
    type: TransactionType,
    onAmountChange: (String) -> Unit
) {
    val prefixColor = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 20.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "¥",
                style = MaterialTheme.typography.headlineMedium,
                color = prefixColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (amount.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * 分类芯片行（水平滚动）。
 *
 * @param categories        当前类型下的分类列表
 * @param selectedCategory  已选分类名称
 * @param onCategorySelected 分类选中回调
 */
@Composable
private fun CategoryChipsRow(
    categories: List<CategoryEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = categories,
            key = { it.id }
        ) { category ->
            val label = category.icon?.let { icon -> "$icon ${category.name}" } ?: category.name
            CategoryChip(
                label = label,
                selected = category.name == selectedCategory,
                onClick = { onCategorySelected(category.name) }
            )
        }
    }
}

/**
 * 日期选择行，点击弹出 [DatePickerDialog]。
 *
 * @param dateMillis      当前日期时间戳（毫秒）
 * @param onDateSelected  日期选择回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectorRow(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showDatePicker = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "日期",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = formatDate(dateMillis),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let(onDateSelected)
                    showDatePicker = false
                }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = "取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 将时间戳（毫秒）格式化为「yyyy年MM月dd日」。
 */
private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    return format.format(Date(timestamp))
}
