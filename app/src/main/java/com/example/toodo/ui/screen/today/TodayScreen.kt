package com.example.toodo.ui.screen.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.toodo.domain.model.Task
import com.example.toodo.ui.component.CompletionConfetti
import com.example.toodo.ui.component.EmptyState
import com.example.toodo.ui.component.StreakBanner
import com.example.toodo.ui.component.TaskCard
import com.example.toodo.ui.util.toFullDateString
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onAddTask: () -> Unit = {},
    onTaskClick: (Long) -> Unit = {}
) {
    val viewModel: TodayViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val showFocusLimit by viewModel.showFocusLimitReached.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showCompletedSection by remember { mutableStateOf(true) }

    // Show snackbar when focus limit is reached
    LaunchedEffect(showFocusLimit) {
        if (showFocusLimit) {
            snackbarHostState.showSnackbar("最多聚焦3个任务，请先取消一个")
            viewModel.dismissFocusLimitSnackbar()
        }
    }

    val selectedTask = uiState.selectedTask

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = LocalDate.now().toFullDateString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: open search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("设置") },
                                onClick = { showOverflowMenu = false },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        }
    ) { innerPadding ->
        // Confetti overlay
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中…", style = MaterialTheme.typography.bodyLarge)
                }
            } else if (uiState.todayTotalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(message = "今天还没有待办任务\n点击 + 开始添加吧")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ── Streak banner ──
                    item {
                        StreakBanner(
                            streakDays = uiState.streakDays,
                            freezesRemaining = uiState.freezesRemaining
                        )
                    }

                    // ── Focus tasks section ──
                    if (uiState.focusedTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "⭐ 聚焦任务",
                                subtitle = "${uiState.focusedTasks.size} 项"
                            )
                        }
                        items(
                            items = uiState.focusedTasks.take(3),
                            key = { it.id }
                        ) { task ->
                            SwipeableTaskItem(
                                task = task,
                                onComplete = { viewModel.completeTask(task) },
                                onDefer = { viewModel.deferTask(task) },
                                onClick = { viewModel.selectTask(task) }
                            )
                        }
                    }

                    // ── Today tasks section ──
                    if (uiState.todayTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📋 今日任务",
                                subtitle = "${uiState.todayCompletedCount}/${uiState.todayTotalCount}"
                            )
                        }
                        items(
                            items = uiState.todayTasks,
                            key = { it.id }
                        ) { task ->
                            SwipeableTaskItem(
                                task = task,
                                onComplete = { viewModel.completeTask(task) },
                                onDefer = { viewModel.deferTask(task) },
                                onClick = { viewModel.selectTask(task) }
                            )
                        }
                    }

                    // ── Completed tasks section ──
                    if (uiState.completedTasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCompletedSection = !showCompletedSection }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "✓ 已完成 (${uiState.completedTasks.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = if (showCompletedSection) "收起" else "展开",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        item {
                            AnimatedVisibility(
                                visible = showCompletedSection,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.completedTasks.forEach { task ->
                                        TaskCard(
                                            task = task,
                                            onClick = { viewModel.selectTask(task) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom spacer for FAB clearance
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            // ── Confetti overlay ──
            CompletionConfetti(
                trigger = uiState.showConfetti,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // ── Bottom sheet ──
    if (uiState.showBottomSheet && selectedTask != null) {
        TaskBottomSheet(
            task = selectedTask,
            onDismiss = { viewModel.dismissBottomSheet() },
            onCompleteToggle = {
                if (selectedTask.isCompleted) viewModel.uncompleteTask(selectedTask)
                else viewModel.completeTask(selectedTask)
                viewModel.dismissBottomSheet()
            },
            onDefer = {
                viewModel.deferTask(selectedTask)
                viewModel.dismissBottomSheet()
            },
            onEdit = {
                viewModel.dismissBottomSheet()
                onTaskClick(selectedTask.id)
            },
            onDelete = {
                viewModel.deleteTask(selectedTask)
            },
            onToggleFocus = {
                viewModel.toggleFocus(selectedTask)
                viewModel.dismissBottomSheet()
            }
        )
    }
}

// ═══════════════════════════════════════════════════
//  Section header: title + subtitle on one row
// ═══════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════════════
//  Swipeable task item: left swipe → complete,
//  right swipe → defer to tomorrow
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskItem(
    task: Task,
    onComplete: () -> Unit,
    onDefer: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { // left swipe → complete
                    onComplete()
                    false // keep item in list (Room reloads)
                }
                SwipeToDismissBoxValue.EndToStart -> { // right swipe → defer
                    onDefer()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Background behind the card during swipe
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd ->
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            SwipeToDismissBoxValue.EndToStart ->
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd)
                    Arrangement.Start
                else
                    Arrangement.End
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "完成",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("完成", color = MaterialTheme.colorScheme.primary)
                } else if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Text("推迟", color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "推迟",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        TaskCard(
            task = task,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ═══════════════════════════════════════════════════
//  Modal bottom sheet — task quick actions
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskBottomSheet(
    task: Task,
    onDismiss: () -> Unit,
    onCompleteToggle: () -> Unit,
    onDefer: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFocus: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Task title preview
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider()

            // Toggle complete / uncomplete
            BottomSheetAction(
                icon = if (task.isCompleted) Icons.Default.RadioButtonUnchecked
                else Icons.Default.CheckCircle,
                label = if (task.isCompleted) "取消完成" else "完成",
                onClick = onCompleteToggle
            )

            // Defer to tomorrow
            BottomSheetAction(
                icon = Icons.Default.SkipNext,
                label = "推迟到明天",
                onClick = onDefer
            )

            // Edit
            BottomSheetAction(
                icon = Icons.Default.Edit,
                label = "编辑",
                onClick = onEdit
            )

            // Toggle focus
            BottomSheetAction(
                icon = if (task.isFocusTask) Icons.Default.Star else Icons.Default.StarBorder,
                label = if (task.isFocusTask) "取消聚焦" else "标记聚焦",
                onClick = onToggleFocus
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

            // Delete
            BottomSheetAction(
                icon = Icons.Default.Delete,
                label = "删除",
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun BottomSheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}
