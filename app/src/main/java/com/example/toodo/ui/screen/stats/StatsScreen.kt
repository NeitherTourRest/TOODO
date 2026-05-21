package com.example.toodo.ui.screen.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.toodo.ui.component.BalanceChart
import com.example.toodo.ui.component.HeatmapCalendar
import com.example.toodo.ui.theme.StreakActive
import com.example.toodo.ui.theme.StreakFrozen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val viewModel: StatsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "统计",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("加载中…", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── 1. Streak Section ──
                StreakSection(
                    streakDays = uiState.streakDays,
                    longestStreak = uiState.longestStreak,
                    freezesRemaining = uiState.freezesRemaining,
                    onUseFreeze = { viewModel.useFreeze() }
                )

                // ── 2. Daily Completion Ring ──
                DailyCompletionRing(
                    completedCount = uiState.todayCompletedCount,
                    totalCount = uiState.todayTotalCount,
                    focusCompletedCount = uiState.todayFocusCompletedCount
                )

                // ── 3. Heatmap ──
                HeatmapSection(heatmapData = uiState.heatmapData)

                // ── 4. Weekly Overview ──
                WeeklyOverview(weeklyData = uiState.weeklyData)

                // ── 5. Life Balance ──
                BalanceSection(
                    categoryData = uiState.categoryData,
                    categoryColors = uiState.categoryColors
                )

                // Bottom spacer
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════
//  Streak Section
// ═══════════════════════════════════════════════════

@Composable
private fun StreakSection(
    streakDays: Int,
    longestStreak: Int,
    freezesRemaining: Int,
    onUseFreeze: () -> Unit
) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: fire icon + streak count + longest
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = StreakActive,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "连续打卡 $streakDays 天",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "最长: $longestStreak 天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Row 2: freeze info + use button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AcUnit,
                        contentDescription = null,
                        tint = StreakFrozen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "冻结卡 ×$freezesRemaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (freezesRemaining > 0) {
                    Button(
                        onClick = onUseFreeze,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StreakFrozen.copy(alpha = 0.15f),
                            contentColor = StreakActive
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AcUnit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "使用冻结卡",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
//  Daily Completion Ring
// ═══════════════════════════════════════════════════

@Composable
private fun DailyCompletionRing(
    completedCount: Int,
    totalCount: Int,
    focusCompletedCount: Int
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val percentage = (progress * 100).toInt()

    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "今日完成",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Ring + center text
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            val progressColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Canvas ring drawn behind the center text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val strokeWidth = 12.dp.toPx()
                            val arcSize = Size(
                                size.width - strokeWidth,
                                size.height - strokeWidth
                            )
                            val arcOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            // Track (background ring)
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Progress arc
                            drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                )

                // Center text overlay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedCount/$totalCount 完成",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Focus task completion hint
            if (focusCompletedCount > 0) {
                Text(
                    text = "其中 ⭐ $focusCompletedCount 项聚焦任务已完成",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
//  Heatmap Section
// ═══════════════════════════════════════════════════

@Composable
private fun HeatmapSection(heatmapData: Map<Long, Int>) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "完成热力图",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HeatmapCalendar(completionData = heatmapData)
            }
        }
    }
}

// ═══════════════════════════════════════════════════
//  Weekly Overview
// ═══════════════════════════════════════════════════

private val weekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
private fun WeeklyOverview(weeklyData: Map<Int, Int>) {
    val maxCount = weeklyData.values.maxOrNull() ?: return

    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "周概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // dayOfWeek.value: 1=Mon .. 7=Sun
                for (dayIndex in 1..7) {
                    val count = weeklyData[dayIndex] ?: 0
                    val fraction = if (maxCount > 0) count.toFloat() / maxCount else 0f

                    WeeklyBar(
                        label = weekdayLabels[dayIndex - 1],
                        count = count,
                        fraction = fraction
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyBar(
    label: String,
    count: Int,
    fraction: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Weekday label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center
        )

        // Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }

        // Count
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.End
        )
    }
}

// ═══════════════════════════════════════════════════
//  Life Balance Section
// ═══════════════════════════════════════════════════

@Composable
private fun BalanceSection(
    categoryData: Map<String, Int>,
    categoryColors: Map<String, Long>
) {
    if (categoryData.isEmpty()) return

    val colorMap = categoryColors.mapValues { (_, longColor) ->
        Color(longColor)
    }

    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "生活平衡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            BalanceChart(
                data = categoryData,
                colors = colorMap
            )
        }
    }
}

// ═══════════════════════════════════════════════════
//  Shared: Section card wrapper
// ═══════════════════════════════════════════════════

@Composable
private fun SectionCard(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        content()
    }
}
