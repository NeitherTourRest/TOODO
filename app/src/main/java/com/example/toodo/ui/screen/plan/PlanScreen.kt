package com.example.toodo.ui.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.toodo.domain.model.Task
import com.example.toodo.ui.component.EmptyState
import com.example.toodo.ui.component.TaskCard
import com.example.toodo.ui.util.toChineseShort
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit
) {
    val viewModel: PlanViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Search bar ────────────────────────────────────────────────
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClear = viewModel::clearSearch
            )

            // ── Month Calendar ─────────────────────────────────────────
            MonthCalendar(
                currentMonth = uiState.currentMonth,
                taskCounts = uiState.monthTaskCounts,
                selectedDate = uiState.selectedDate,
                onDateSelected = viewModel::selectDate,
                onNextMonth = viewModel::nextMonth,
                onPreviousMonth = viewModel::previousMonth
            )

            // ── Date strip ────────────────────────────────────────────────
            DateStrip(
                weekDates = uiState.weekDates,
                selectedDate = uiState.selectedDate,
                onDateSelected = viewModel::selectDate,
                onJumpToToday = viewModel::jumpToToday
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Task list ─────────────────────────────────────────────────
            if (uiState.isSearching) {
                SearchResultsList(
                    searchResults = uiState.searchResults,
                    query = uiState.searchQuery,
                    onTaskClick = onTaskClick
                )
            } else {
                DateTaskList(
                    tasks = uiState.tasksForSelectedDate,
                    selectedDate = uiState.selectedDate,
                    onTaskClick = onTaskClick
                )
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = onAddTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加任务"
            )
        }
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                text = "搜索任务…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "清除搜索",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

// ─── Date strip ───────────────────────────────────────────────────────────────

@Composable
private fun DateStrip(
    weekDates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onJumpToToday: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Today" quick-jump chip
        item {
            TodayChip(
                isTodaySelected = selectedDate == LocalDate.now(),
                onClick = onJumpToToday
            )
        }

        // 7-day date items
        items(weekDates, key = { it.toEpochDay() }) { date ->
            DateItem(
                date = date,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayChip(
    isTodaySelected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (isTodaySelected) {
        FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    FilterChip(
        selected = isTodaySelected,
        onClick = onClick,
        label = { Text("今天", style = MaterialTheme.typography.labelLarge) },
        colors = colors,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dayOfWeek = date.dayOfWeek.toChineseShort()
    val dayNumber = date.dayOfMonth.toString()

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

// ─── Date-filtered task list ─────────────────────────────────────────────────

@Composable
private fun DateTaskList(
    tasks: List<Task>,
    selectedDate: LocalDate,
    onTaskClick: (Long) -> Unit
) {
    if (tasks.isEmpty()) {
        EmptyState(
            message = "这一天没有待办任务",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Section header
        Text(
            text = selectedDate.toSectionHeader(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task.id) },
                    categoryColor = task.list?.color?.let { Color(it) }
                )
            }
        }
    }
}

// Section header date formatter (includes weekday)
private fun LocalDate.toSectionHeader(): String {
    val today = LocalDate.now()
    return when {
        this == today -> "今天"
        this == today.plusDays(1) -> "明天"
        this == today.minusDays(1) -> "昨天"
        else -> {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("M月d日 EEEE", java.util.Locale.CHINESE)
            this.format(formatter)
        }
    }
}

// ─── Search results list with highlighting ───────────────────────────────────

@Composable
private fun SearchResultsList(
    searchResults: List<Task>,
    query: String,
    onTaskClick: (Long) -> Unit
) {
    if (searchResults.isEmpty()) {
        EmptyState(
            message = "没有找到匹配的任务",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Text(
            text = "找到 ${searchResults.size} 个结果",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(searchResults, key = { it.id }) { task ->
                HighlightedTaskItem(
                    task = task,
                    query = query,
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}

@Composable
private fun HighlightedTaskItem(
    task: Task,
    query: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color stripe
            task.list?.color?.let { color ->
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(Color(color), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

           Column(modifier = Modifier.weight(1f)) {
                HighlightedTitle(
                    title = task.title,
                    query = query,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Search result metadata
            if (task.dueDate != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.dueDate!!.toDisplayStringInline(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HighlightedTitle(
    title: String,
    query: String,
    color: androidx.compose.ui.graphics.Color
) {
    if (query.isBlank()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        return
    }

    val annotated = buildAnnotatedString {
        var startIndex = 0
        val lowerTitle = title.lowercase()
        val lowerQuery = query.lowercase()

        while (startIndex < title.length) {
            val matchIndex = lowerTitle.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                // No more matches, append rest
                append(title.substring(startIndex))
                break
            }
            // Text before match
            append(title.substring(startIndex, matchIndex))
            // Highlighted match
            withStyle(SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )) {
                append(title.substring(matchIndex, matchIndex + query.length))
            }
            startIndex = matchIndex + query.length
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

private fun LocalDate.toDisplayStringInline(): String {
    val today = LocalDate.now()
    return when {
        this == today -> "今天"
        this == today.plusDays(1) -> "明天"
        this == today.minusDays(1) -> "昨天"
        else -> {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("M月d日", java.util.Locale.CHINESE)
            this.format(formatter)
        }
    }
}

// ─── Month Calendar ────────────────────────────────────────────────────────────

private val CHINESE_DAY_HEADERS = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)

@Composable
private fun MonthCalendar(
    currentMonth: YearMonth,
    taskCounts: Map<LocalDate, Int>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // ── Month header ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上个月",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${currentMonth.year}年${currentMonth.month.value}月",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "下个月",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Day-of-week headers ───────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dow in CHINESE_DAY_HEADERS) {
                Text(
                    text = dow.toChineseShort(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // ── Calendar grid ─────────────────────────────────────────────
        val firstOfMonth = currentMonth.atDay(1)
        val lastOfMonth = currentMonth.atEndOfMonth()
        val startDate = firstOfMonth.minusDays(
            (firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
        )
        val endDate = lastOfMonth.plusDays(
            (DayOfWeek.SUNDAY.value - lastOfMonth.dayOfWeek.value).toLong()
        )
        val totalDays = endDate.toEpochDay() - startDate.toEpochDay() + 1
        val weeks = ((totalDays + 6) / 7).toInt()

        val today = LocalDate.now()

        for (week in 0 until weeks) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    val date = startDate.plusDays((week * 7 + dayOfWeek).toLong())
                    CalendarCell(
                        date = date,
                        isCurrentMonth = date.month == currentMonth.month,
                        taskCount = taskCounts[date] ?: 0,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        modifier = Modifier.weight(1f),
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    taskCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        isSelected || isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(enabled = isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
            if (taskCount > 0) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
