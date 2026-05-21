package com.example.toodo.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtil {

    /**
     * Strong confirmation haptic (50ms) — task completion, focus toggle, save.
     */
    fun performConfirmationHaptic(context: Context) {
        vibrate(context, 50)
    }

    /**
     * Light tap haptic (15ms) — button taps, navigation, minor interactions.
     */
    fun performLightHaptic(context: Context) {
        vibrate(context, 15)
    }

    private fun vibrate(context: Context, durationMs: Long) {
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}
