package com.example.toodo.ui.component

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated checkbox that smoothly transitions between checked/unchecked states.
 * The color shifts to primary when checked and provides click feedback.
 */
@Composable
fun AnimatedCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isChecked) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = if (isChecked) "已完成" else "未完成",
        tint = color,
        modifier = modifier
            .size(24.dp)
            .clickable { onCheckedChange(!isChecked) }
    )
}

/**
 * Confetti burst animation triggered when a task is completed.
 * Renders 12 colored particles that radiate outward and fade.
 * Particles clean up automatically after the animation duration.
 */
@Composable
fun CompletionConfetti(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    var particles by remember { mutableStateOf(listOf<ParticleData>()) }

    // Generate particles when trigger fires
    LaunchedEffect(trigger) {
        if (!trigger) return@LaunchedEffect
        particles = (1..12).map { i ->
            ParticleData(
                angle = (i * 30).toFloat(),
                distance = 0f,
                maxDistance = 60f + Random.nextFloat() * 40f,
                color = listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFF6B6B),
                    Color(0xFF4ECDC4),
                    Color(0xFF45B7D1),
                    Color(0xFFFFA07A),
                )[i % 5],
                size = 4f + Random.nextFloat() * 4f
            )
        }
    }

    val animProgress by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        finishedListener = { particles = emptyList() }
    )

    if (particles.isNotEmpty()) {
        Canvas(modifier = modifier.size(120.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            particles.forEach { particle ->
                val currentDist = particle.maxDistance * animProgress
                val rad = Math.toRadians(particle.angle.toDouble())
                val x = center.x + cos(rad).toFloat() * currentDist
                val y = center.y + sin(rad).toFloat() * currentDist
                drawCircle(
                    color = particle.color,
                    radius = particle.size * (1f - animProgress * 0.5f),
                    center = Offset(x, y),
                    alpha = 1f - animProgress
                )
            }
        }
    }
}

private data class ParticleData(
    val angle: Float,
    val distance: Float,
    val maxDistance: Float,
    val color: Color,
    val size: Float
)
