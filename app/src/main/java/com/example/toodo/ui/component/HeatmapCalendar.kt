package com.example.toodo.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.toodo.ui.theme.HeatmapLevel0
import com.example.toodo.ui.theme.HeatmapLevel1
import com.example.toodo.ui.theme.HeatmapLevel2
import com.example.toodo.ui.theme.HeatmapLevel3
import com.example.toodo.ui.theme.HeatmapLevel4
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Interactive GitHub-style contribution heatmap showing 365 days of task
 * completion data with month labels, day-of-week labels, a color legend,
 * today indicator, tap-to-inspect, and tap-flash animation.
 *
 * @param completionData Map of epochDay (Long) → completion count (Int).
 * @param onCellTapped Callback when the user taps a cell, receiving the
 *   epochDay and the completion count for that day.
 */
@Composable
fun HeatmapCalendar(
    completionData: Map<Long, Int>,
    modifier: Modifier = Modifier,
    onCellTapped: (Long, Int) -> Unit = { _, _ -> }
) {
    // ── Stable date anchors (recomputed once per composition) ──
    val today = remember { LocalDate.now() }
    val todayEpochDay = remember { today.toEpochDay() }
    val startDate = remember { todayEpochDay - 364 }

    val weeks = 53
    val daysPerWeek = 7

    // ── Heatmap color palette ──
    val heatmapColors = listOf(HeatmapLevel0, HeatmapLevel1, HeatmapLevel2, HeatmapLevel3, HeatmapLevel4)

    // ── Tap animation state ──
    var tappedCell by remember { mutableStateOf<Long?>(null) }
    val flashAlpha = remember { Animatable(0f) }

    // Flash highlight → fade out over ~300 ms
    LaunchedEffect(tappedCell) {
        if (tappedCell != null) {
            flashAlpha.snapTo(0.3f)
            flashAlpha.animateTo(0f, animationSpec = tween(300))
            tappedCell = null
        }
    }

    // ── Capture theme colors outside Canvas draw scope ──
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current

        // ── Layout constants ──
        val dayLabelWidthDp = 16.dp
        val monthLabelHeightDp = 18.dp
        val legendHeightDp = 28.dp

        // ── Dynamic cell sizing ──
        val cellSizeDp = with(density) {
            val availPx = maxWidth.toPx() - dayLabelWidthDp.toPx()
            val rawCellPx = availPx / weeks.toFloat()
            max(rawCellPx, 8.dp.toPx()).toDp()
        }
        val cellSpacingDp = cellSizeDp * 0.2f

        // Pixel helpers
        val cellSizePx = with(density) { cellSizeDp.toPx() }
        val cellSpacingPx = with(density) { cellSpacingDp.toPx() }
        val stepPx = cellSizePx + cellSpacingPx

        val heatmapWidthDp = with(density) {
            (weeks * stepPx - cellSpacingPx).toDp()
        }
        val heatmapHeightDp = with(density) {
            (daysPerWeek * stepPx - cellSpacingPx).toDp()
        }

        // ── Compute month-label positions (which column each month starts) ──
        val monthLabels = remember(startDate) {
            val labels = mutableListOf<Pair<Int, String>>()
            var lastMonth = -1
            for (week in 0 until weeks) {
                val epochDay = startDate + week * 7
                val date = LocalDate.ofEpochDay(epochDay)
                if (date.monthValue != lastMonth) {
                    labels.add(week to "${date.monthValue}月")
                    lastMonth = date.monthValue
                }
            }
            labels
        }

        // ── Pre-compute grid cell data (avoids recomputation in draw loop) ──
        data class CellInfo(val epochDay: Long, val count: Int, val colorIndex: Int)

        val gridCells = remember(completionData, todayEpochDay) {
            (0 until weeks).flatMap { week ->
                (0 until daysPerWeek).mapNotNull { day ->
                    val epochDay = startDate + (week * 7L + day)
                    if (epochDay > todayEpochDay) return@mapNotNull null
                    val count = completionData[epochDay] ?: 0
                    val colorIdx = when {
                        count == 0 -> 0
                        count <= 2 -> 1
                        count <= 5 -> 2
                        count <= 8 -> 3
                        else -> 4
                    }
                    CellInfo(epochDay, count, colorIdx)
                }
            }
        }

        // ── Today's cell position ──
        val todayWeek = ((todayEpochDay - startDate) / 7).toInt()
        val todayDay = ((todayEpochDay - startDate) % 7).toInt()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "过去365天的任务完成热力图，共${gridCells.size}个格子"
                }
        ) {
            // ══════════════════════════════════════════════════
            //  Month Labels
            // ══════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(monthLabelHeightDp)
            ) {
                monthLabels.forEach { (week, label) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = dayLabelWidthDp)
                            .offset(x = with(density) { (week * stepPx).toDp() })
                            .align(Alignment.TopStart)
                    )
                }
            }

            // ══════════════════════════════════════════════════
            //  Day-of-Week Labels + Canvas Grid
            // ══════════════════════════════════════════════════
            Row(modifier = Modifier.fillMaxWidth()) {
                // ── Left-axis day labels ──
                Box(
                    modifier = Modifier
                        .width(dayLabelWidthDp)
                        .height(heatmapHeightDp)
                ) {
                    data class DayLabel(val day: Int, val text: String)
                    listOf(
                        DayLabel(0, "一"),
                        DayLabel(2, "三"),
                        DayLabel(4, "五"),
                        DayLabel(6, "日")
                    ).forEach { (day, text) ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant,
                            modifier = Modifier
                                .offset(
                                    y = with(density) {
                                        // Vertically center on the cell row
                                        (day * stepPx + cellSizePx * 0.5f).toDp() - 7.dp
                                    }
                                )
                                .align(Alignment.TopStart)
                        )
                    }
                }

                // ── Heatmap Canvas ──
                Canvas(
                    modifier = Modifier
                        .size(width = heatmapWidthDp, height = heatmapHeightDp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val week = (offset.x / stepPx)
                                    .roundToInt()
                                    .coerceIn(0, weeks - 1)
                                val day = (offset.y / stepPx)
                                    .roundToInt()
                                    .coerceIn(0, daysPerWeek - 1)
                                val epochDay = startDate + (week * 7L + day)
                                if (epochDay <= todayEpochDay) {
                                    val count = completionData[epochDay] ?: 0
                                    tappedCell = epochDay
                                    onCellTapped(epochDay, count)
                                }
                            }
                        }
                ) {
                    val oneDpPx = 1.dp.toPx()

                    // Draw all cells
                    gridCells.forEach { cell ->
                        val w = ((cell.epochDay - startDate) / 7).toInt()
                        val d = ((cell.epochDay - startDate) % 7).toInt()
                        val x = w * stepPx
                        val y = d * stepPx

                        drawRoundRect(
                            color = heatmapColors[cell.colorIndex],
                            topLeft = Offset(x, y),
                            size = Size(cellSizePx, cellSizePx),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }

                    // Draw tap-flash overlay
                    tappedCell?.let { tappedEpochDay ->
                        val w = ((tappedEpochDay - startDate) / 7).toInt()
                        val d = ((tappedEpochDay - startDate) % 7).toInt()
                        drawRoundRect(
                            color = primaryColor.copy(alpha = flashAlpha.value),
                            topLeft = Offset(w * stepPx, d * stepPx),
                            size = Size(cellSizePx, cellSizePx),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }

                    // Draw today border
                    if (todayWeek in 0 until weeks && todayDay in 0 until daysPerWeek) {
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(todayWeek * stepPx, todayDay * stepPx),
                            size = Size(cellSizePx, cellSizePx),
                            cornerRadius = CornerRadius(2f, 2f),
                            style = Stroke(width = oneDpPx)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ══════════════════════════════════════════════════
            //  Color Legend
            // ══════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dayLabelWidthDp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val legendLabels = listOf("0", "1-2", "3-5", "6-8", "9+")
                val legendSquareSize = 12.dp

                heatmapColors.forEachIndexed { index, color ->
                    Canvas(modifier = Modifier.size(legendSquareSize)) {
                        drawRoundRect(
                            color = color,
                            size = size,
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = legendLabels[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                    if (index < heatmapColors.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}
