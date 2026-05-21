package com.example.toodo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.Alignment
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.toodo.MainActivity
import com.example.toodo.domain.model.Priority
import com.example.toodo.domain.model.Task
import com.example.toodo.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class ToodoWidget @Inject constructor(
    private val taskRepository: TaskRepository
) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = taskRepository
            .getTodayTasks(LocalDate.now().toEpochDay())
            .first()

        provideContent {
            WidgetLayout(tasks)
        }
    }

    @Composable
    private fun WidgetLayout(tasks: List<Task>) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(Color(0xFF1C1B1F)))
                .cornerRadius(16.dp)
                .padding(16.dp)
        ) {
            HeaderRow(taskCount = tasks.size)

            Spacer(modifier = GlanceModifier.height(12.dp))

            if (tasks.isEmpty()) {
                EmptyMessage()
            } else {
                TaskList(tasks)
            }
        }
    }

    @Composable
    private fun HeaderRow(taskCount: Int) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "TOODO 今日",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            CountBadge(count = taskCount)
        }
    }

    @Composable
    private fun CountBadge(count: Int) {
        Text(
            text = "$count",
            style = TextStyle(
                color = GlanceTheme.colors.onPrimary,
                fontSize = 14.sp
            ),
            modifier = GlanceModifier
                .background(GlanceTheme.colors.primary)
                .cornerRadius(12.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }

    @Composable
    private fun EmptyMessage() {
        Text(
            text = "今天没有待办 \u2713",
            style = TextStyle(
                color = ColorProvider(Color(0xFFAAAAAA)),
                fontSize = 14.sp
            )
        )
    }

    @Composable
    private fun TaskList(tasks: List<Task>) {
        val displayTasks = tasks.take(5)
        displayTasks.forEachIndexed { index, task ->
            TaskRow(task)
            if (index < displayTasks.lastIndex) {
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
        }
        if (tasks.size > 5) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "还有 ${tasks.size - 5} 项...",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFAAAAAA)),
                    fontSize = 13.sp
                )
            )
        }
    }

    @Composable
    private fun TaskRow(task: Task) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Checkbox icon
            Text(
                text = if (task.isCompleted) "\u2611" else "\u2610",
                style = TextStyle(
                    color = ColorProvider(
                        if (task.isCompleted) Color(0xFF4CAF50) else Color(0xFFAAAAAA)
                    ),
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Task title — strikethrough if completed
            Text(
                text = task.title,
                style = TextStyle(
                    color = if (task.isCompleted) {
                        ColorProvider(Color(0xFF888888))
                    } else {
                        GlanceTheme.colors.onSurface
                    },
                    fontSize = 14.sp,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    }
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Priority dot
            Text(
                text = "\u25CF",
                style = TextStyle(
                    color = ColorProvider(priorityColor(task.priority)),
                    fontSize = 12.sp
                )
            )
        }
    }

    private fun priorityColor(priority: Priority): Color = when (priority) {
        Priority.HIGH -> Color.Red
        Priority.MEDIUM -> Color(0xFFFFA500)
        Priority.LOW -> Color(0xFF2196F3)
    }
}
