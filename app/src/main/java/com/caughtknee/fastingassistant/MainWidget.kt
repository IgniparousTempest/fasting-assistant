package com.caughtknee.fastingassistant

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.widget.RemoteViews
import org.joda.time.LocalTime
import android.app.PendingIntent
import android.content.Intent



/**
 * Implementation of App Widget functionality.
 */
class MainWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE) ?: return
            val hourStart = sharedPref.getInt(context.getString(R.string.saved_time_picker_start_hour), 7)
            val minuteStart = sharedPref.getInt(context.getString(R.string.saved_time_picker_start_minute), 0)
            val hourEnd = sharedPref.getInt(context.getString(R.string.saved_time_picker_end_hour), 19)
            val minuteEnd = sharedPref.getInt(context.getString(R.string.saved_time_picker_end_minute), 0)

            val views = RemoteViews(context.packageName, R.layout.main_widget)
            val statusText = when (canEat(LocalTime.now(), hourStart, minuteStart, hourEnd, minuteEnd)) {
                true -> {
                    val colour = ResourcesCompat.getColor(context.resources, R.color.colourEating, null)
                    views.setInt(R.id.appwidget_layout, "setBackgroundColor", colour)
                    context.getString(R.string.status_eating)
                }
                false -> {
                    val colour = ResourcesCompat.getColor(context.resources, R.color.colourFasting, null)
                    views.setInt(R.id.appwidget_layout, "setBackgroundColor", colour)
                    context.getString(R.string.status_fasting)
                }
            }
            val timeLeft = timeLeftForCurrentStatus(LocalTime.now(), hourStart, minuteStart, hourEnd, minuteEnd)
            val timeLeftText = if (timeLeft == "0") context.getString(R.string.widget_time_left_less_than_30) else context.getString(R.string.widget_time_left, timeLeft)
            val windowText = context.getString(R.string.widget_window, timeFormat(hourStart, minuteStart), timeFormat(hourEnd, minuteEnd))
            // Construct the RemoteViews object
            views.setTextViewText(R.id.appwidget_status, statusText)
            views.setTextViewText(R.id.appwidget_timeLeft, timeLeftText)
            views.setTextViewText(R.id.appwidget_window, windowText)

            // Create an Intent to launch MainActivity
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            views.setOnClickPendingIntent(R.id.appwidget_layout, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

