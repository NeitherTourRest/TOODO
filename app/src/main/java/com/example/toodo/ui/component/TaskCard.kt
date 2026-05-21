package com.example.toodo.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.toodo.domain.model.Priority
import com.example.toodo.domain.model.Task
import com.example.toodo.domain.model.TaskType
import com.example.toodo.ui.theme.PriorityHigh
import com.example.toodo.ui.theme.PriorityLow
import com.example.toodo.ui.theme.PriorityMedium

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier,
    categoryColor: Color? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(durationMillis = 300)
    )
    val contentAlpha by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color stripe on the left edge
            if (categoryColor != null) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(categoryColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Priority indicator dot
            val priorityColor = when (task.priority) {
                Priority.HIGH -> PriorityHigh
                Priority.MEDIUM -> PriorityMedium
                Priority.LOW -> PriorityLow
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(priorityColor, RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title + metadata column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = contentAlpha,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Recurrence badge
                    if (task.taskType != TaskType.ONE_TIME) {
                        RecurrenceChip(taskType = task.taskType)
                    }

                    // Due time
                    if (task.dueTime != null && !task.isCompleted) {
                        Text(
                            text = "${task.dueTime!!.hour.toString().padStart(2, '0')}:${task.dueTime!!.minute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Subtask progress e.g. "2/5"
                    if (task.subTasks.isNotEmpty()) {
                        val completed = task.subTasks.count { it.isCompleted }
                        val total = task.subTasks.size
                        Text(
                            text = "$completed/$total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Focus star (hidden when completed)
            if (task.isFocusTask && !task.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "聚焦任务",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
