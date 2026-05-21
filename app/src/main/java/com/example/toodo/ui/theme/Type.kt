package com.example.toodo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ══════════════════════════════════════════════════════════
//  Handwriting Typography — system cursive font for
//  the pencil-sketch personality, serif for body text.
// ══════════════════════════════════════════════════════════

/** System cursive font — maps to handwriting fonts like
 *  "Dancing Script" or "Coming Soon" on most Android devices. */
private val HandFont = FontFamily.Cursive

/** Serif for body text — more organic than sans-serif, like
 *  typed notes on sketch paper. */
private val BodyFont = FontFamily.Serif

val ToodoTypography = Typography(
    // Page titles like "今日待办" — big and hand-written
    headlineLarge = TextStyle(
        fontFamily = HandFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HandFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    // Section headers like "聚焦任务"
    titleLarge = TextStyle(
        fontFamily = HandFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    // Card titles — smaller handwriting
    titleMedium = TextStyle(
        fontFamily = HandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    // Body text — serif for the "typed notes" feel
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Secondary text
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // Button text — handwriting
    labelLarge = TextStyle(
        fontFamily = HandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    // Captions, chip text
    labelSmall = TextStyle(
        fontFamily = HandFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
