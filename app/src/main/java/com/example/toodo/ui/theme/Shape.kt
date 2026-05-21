package com.example.toodo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════
//  Slightly irregular rounded corners — hand-drawn imperfection
//  Each corner has a subtly different radius so nothing
//  feels perfectly machine-made.
// ══════════════════════════════════════════════════════════

val ToodoShapes = Shapes(
    extraSmall = RoundedCornerShape(
        topStart = 5.dp,
        topEnd = 3.dp,
        bottomStart = 4.dp,
        bottomEnd = 6.dp
    ),
    small = RoundedCornerShape(
        topStart = 9.dp,
        topEnd = 7.dp,
        bottomStart = 8.dp,
        bottomEnd = 10.dp
    ),
    medium = RoundedCornerShape(
        topStart = 13.dp,
        topEnd = 11.dp,
        bottomStart = 12.dp,
        bottomEnd = 15.dp
    ),
    large = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 14.dp,
        bottomStart = 16.dp,
        bottomEnd = 20.dp
    ),
    extraLarge = RoundedCornerShape(
        topStart = 26.dp,
        topEnd = 22.dp,
        bottomStart = 24.dp,
        bottomEnd = 28.dp
    )
)
