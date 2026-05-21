package com.example.toodo.ui.screen.addtask

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.toodo.domain.model.Frequency
import com.example.toodo.domain.model.Priority
import com.example.toodo.ui.theme.PriorityHigh
import com.example.toodo.ui.theme.PriorityLow
import com.example.toodo.ui.theme.PriorityMedium
import com.example.toodo.ui.util.toChineseShort
import com.example.toodo.ui.util.toDisplayString
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ══════════════════════════════════════════════════════════
//  Constants
// ══════════════════════════════════════════════════════════

private val PRESET_REMINDERS: List<Pair<Long, String>> = listOf(
    1440L to "前1天",
    10080L to "前1周",
    60L to "前1小时",
)

private val WEEKDAY_ORDER = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)

// ══════════════════════════════════════════════════════════
//  Custom TimePickerDialog (not in Material 3 library)
// ══════════════════════════════════════════════════════════

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() }
    )
}

// ══════════════════════════════════════════════════════════
//  Main screen composable
// ══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: AddTaskViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // ── Date picker dialog ──
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onHideDatePicker() },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onDueDateChange(date)
                    }
                    viewModel.onHideDatePicker()
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onHideDatePicker() }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Time picker dialog ──
    if (uiState.showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.dueTime?.hour ?: 9,
            initialMinute = uiState.dueTime?.minute ?: 0,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { viewModel.onHideTimePicker() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDueTimeChange(
                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                    )
                    viewModel.onHideTimePicker()
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onHideTimePicker() }) {
                    Text("取消")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "编辑任务" else "新建任务",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onSave(onNavigateBack)
                        },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text("保存", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── 1. Title ────────────────────────────────────
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("任务名称", style = MaterialTheme.typography.bodyLarge) },
                isError = uiState.titleError,
                supportingText = if (uiState.titleError) {
                    { Text("标题不能为空", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 2. Task type segmented buttons ─────────────
            SectionLabel("任务类型")
            Spacer(modifier = Modifier.height(8.dp))
            TaskTypeRow(
                selectedType = uiState.uiTaskType,
                onTypeChange = viewModel::onUiTaskTypeChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 3. Conditional fields ──────────────────────
            when (uiState.uiTaskType) {
                UiTaskType.ONE_TIME -> {
                    DateField(
                        date = uiState.dueDate,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onShowDatePicker()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TimeField(
                        time = uiState.dueTime,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onShowTimePicker()
                        },
                        onClear = { viewModel.onDueTimeChange(null) }
                    )
                }
                UiTaskType.DAILY -> {
                    Text(
                        text = "每日重复，无需额外设置",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                UiTaskType.RECURRING -> {
                    RecurrenceSection(
                        freq = uiState.recurrenceFreq,
                        onFreqChange = viewModel::onRecurrenceFreqChange,
                        selectedDays = uiState.selectedDays,
                        onDayToggle = viewModel::onDayToggle,
                        viewModel = viewModel
                    )
                }
                UiTaskType.DDL -> {
                    DateField(
                        date = uiState.dueDate,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onShowDatePicker()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TimeField(
                        time = uiState.dueTime,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onShowTimePicker()
                        },
                        onClear = { viewModel.onDueTimeChange(null) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ReminderSection(
                        selectedOffsets = uiState.reminderOffsets,
                        onOffsetToggle = viewModel::onReminderOffsetToggle,
                        onAddCustom = viewModel::onAddCustomReminder
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── 4. Priority ────────────────────────────────
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("优先级")
            Spacer(modifier = Modifier.height(8.dp))
            PriorityRow(
                selected = uiState.priority,
                onSelect = viewModel::onPriorityChange
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── 5. Category dropdown ────────────────────────
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("分类")
            Spacer(modifier = Modifier.height(8.dp))
            CategoryDropdown(
                availableLists = uiState.availableLists,
                selectedListId = uiState.selectedListId,
                onListChange = viewModel::onListChange
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── 6. Description / Notes ──────────────────────
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("备注")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("添加备注…", style = MaterialTheme.typography.bodyMedium) },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── 7. Subtasks ─────────────────────────────────
            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("子任务")
            Spacer(modifier = Modifier.height(8.dp))
            SubTaskSection(
                subTasks = uiState.subTasks,
                onAdd = viewModel::onAddSubTask,
                onUpdate = viewModel::onUpdateSubTask,
                onDelete = viewModel::onDeleteSubTask
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── 8. Focus toggle ─────────────────────────────
            Spacer(modifier = Modifier.height(20.dp))
            FocusToggle(
                isFocus = uiState.isFocusTask,
                onToggle = viewModel::onFocusToggle
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  Reusable section components
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// ═══════════════════════════════════════════════════════════
//  Task type row
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTypeRow(
    selectedType: UiTaskType,
    onTypeChange: (UiTaskType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UiTaskType.entries.forEach { type ->
            val isSelected = type == selectedType
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = isSelected,
                onClick = { onTypeChange(type) },
                label = {
                    Text(
                        text = type.label,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  Date field
// ═══════════════════════════════════════════════════════════

@Composable
private fun DateField(
    date: LocalDate?,
    onClick: () -> Unit
) {
    val displayText = date?.toDisplayString() ?: "选择日期"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (date != null) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  Time field
// ═══════════════════════════════════════════════════════════

@Composable
private fun TimeField(
    time: LocalTime?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val displayText = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "设置时间（可选）"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (time != null) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (time != null) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "清除时间",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  Recurrence section
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceSection(
    freq: Frequency,
    onFreqChange: (Frequency) -> Unit,
    selectedDays: Set<DayOfWeek>,
    onDayToggle: (DayOfWeek) -> Unit,
    viewModel: AddTaskViewModel
) {
    // Frequency toggle: 每周 | 每月
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(Frequency.WEEKLY, Frequency.MONTHLY).forEach { f ->
            val isSelected = f == freq
            val label = when (f) {
                Frequency.WEEKLY -> "每周"
                Frequency.MONTHLY -> "每月"
                else -> f.name
            }
            FilterChip(
                selected = isSelected,
                onClick = { onFreqChange(f) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (freq) {
        Frequency.WEEKLY -> {
            Text(
                text = "选择重复日",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WEEKDAY_ORDER.forEach { day ->
                    val isSelected = day in selectedDays
                    ChipToggle(
                        label = day.toChineseShort(),
                        isSelected = isSelected,
                        onClick = { onDayToggle(day) }
                    )
                }
            }
        }
        Frequency.MONTHLY -> {
            Text(
                text = "选择日期（1-31号）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (1..31).forEach { day ->
                    val isSelected = day in viewModel.uiState.value.selectedMonthDays
                    ChipToggle(
                        label = day.toString(),
                        isSelected = isSelected,
                        onClick = { viewModel.onMonthDayToggle(day) }
                    )
                }
            }
        }
        else -> {}
    }
}

// ═══════════════════════════════════════════════════════════
//  Generic chip toggle
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChipToggle(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

// ═══════════════════════════════════════════════════════════
//  Reminder section
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSection(
    selectedOffsets: List<Long>,
    onOffsetToggle: (Long) -> Unit,
    onAddCustom: (Long) -> Unit
) {
    var customOffsetText by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "提醒设置",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PRESET_REMINDERS.forEach { (offset, label) ->
            val isSelected = offset in selectedOffsets
            FilterChip(
                selected = isSelected,
                onClick = { onOffsetToggle(offset) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = customOffsetText,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                    customOffsetText = newValue
                }
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text("自定义分钟数") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        IconButton(
            onClick = {
                val minutes = customOffsetText.toLongOrNull()
                if (minutes != null && minutes > 0) {
                    onAddCustom(minutes)
                    customOffsetText = ""
                }
            },
            enabled = customOffsetText.toLongOrNull()?.let { it > 0 } == true
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "添加",
                tint = if (customOffsetText.toLongOrNull()?.let { it > 0 } == true)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  Priority row
// ═══════════════════════════════════════════════════════════

@Composable
private fun PriorityRow(
    selected: Priority,
    onSelect: (Priority) -> Unit
) {
    val items = listOf(
        Triple(Priority.HIGH, "高", PriorityHigh),
        Triple(Priority.MEDIUM, "中", PriorityMedium),
        Triple(Priority.LOW, "低", PriorityLow)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { (priority, label, color) ->
            val isSelected = priority == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) color.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .clickable { onSelect(priority) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  Category dropdown
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    availableLists: List<com.example.toodo.domain.model.TaskList>,
    selectedListId: Long?,
    onListChange: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedList = availableLists.find { it.id == selectedListId }
    val selectedText = selectedList?.name ?: "无分类"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("无分类", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = {
                    onListChange(null)
                    expanded = false
                }
            )
            availableLists.forEach { list ->
                DropdownMenuItem(
                    text = {
                        Text(
                            list.name,
                            color = if (list.id == selectedListId)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onListChange(list.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  Subtask section
// ═══════════════════════════════════════════════════════════

@Composable
private fun SubTaskSection(
    subTasks: List<SubTaskDraft>,
    onAdd: () -> Unit,
    onUpdate: (Int, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    subTasks.forEach { draft ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draft.title,
                onValueChange = { onUpdate(draft.tempId, it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("子任务名称") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            IconButton(
                onClick = { onDelete(draft.tempId) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "删除子任务",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onAdd)
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "添加子任务",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  Focus toggle row
// ═══════════════════════════════════════════════════════════

@Composable
private fun FocusToggle(
    isFocus: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isFocus) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = if (isFocus) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "聚焦任务",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "⭐ 最多3个",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isFocus,
            onCheckedChange = { onToggle() }
        )
    }
}
