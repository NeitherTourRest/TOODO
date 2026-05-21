package com.example.toodo.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.toodo.ui.theme.HeatmapLevel0
import com.example.toodo.ui.theme.HeatmapLevel1
import com.example.toodo.ui.theme.HeatmapLevel2
import com.example.toodo.ui.theme.HeatmapLevel3
import com.example.toodo.ui.theme.HeatmapLevel4
import java.time.LocalDate

/**
 * GitHub-style contribution heatmap showing up to 365 days of task completion data.
 *
 * @param completionData Map of epochDay (Long) → completion count (Int).
 *   Days with 0 completions use the lightest color; higher counts use progressively
 *   deeper greens through the 5-level heatmap palette.
 */
@Composable
fun HeatmapCalendar(
    completionData: Map<Long, Int>,
    modifier: Modifier = Modifier
) {
    val cellSize = 10.dp
    val cellSpacing = 2.dp
    val cellSizePx = cellSize.value
    val cellSpacingPx = cellSpacing.value
    val weeks = 53
    val daysPerWeek = 7

    val heatmapColors = listOf(
        HeatmapLevel0,
        HeatmapLevel1,
        HeatmapLevel2,
        HeatmapLevel3,
        HeatmapLevel4
    )

    Canvas(
        modifier = modifier
            .width((weeks * (cellSizePx + cellSpacingPx)).dp)
            .height((daysPerWeek * (cellSizePx + cellSpacingPx)).dp)
    ) {
        val todayEpochDay = LocalDate.now().toEpochDay()
        val startDay = todayEpochDay - 364

        for (week in 0 until weeks) {
            for (day in 0 until daysPerWeek) {
                val epochDay = startDay + (week * 7L + day)
                if (epochDay > todayEpochDay) break

                val count = completionData[epochDay] ?: 0
                val color = when {
                    count == 0 -> heatmapColors[0]
                    count <= 2 -> heatmapColors[1]
                    count <= 5 -> heatmapColors[2]
                    count <= 8 -> heatmapColors[3]
                    else -> heatmapColors[4]
                }

                val x = week * (cellSizePx + cellSpacingPx)
                val y = day * (cellSizePx + cellSpacingPx)

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSizePx, cellSizePx),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
    }
}
