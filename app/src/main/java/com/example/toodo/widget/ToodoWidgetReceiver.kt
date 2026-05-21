package com.example.toodo.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ToodoWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var toodoWidget: ToodoWidget

    override val glanceAppWidget: GlanceAppWidget
        get() = toodoWidget
}
