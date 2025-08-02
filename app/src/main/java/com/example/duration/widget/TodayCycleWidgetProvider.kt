package com.example.duration.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.duration.R
import com.example.duration.model.DurationData
import com.example.duration.model.DurationUtils
import java.time.LocalDate
import java.time.LocalDateTime

class TodayCycleWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_WIDGET_ADD_RECORD = "com.example.duration.ACTION_WIDGET_ADD_RECORD"

        // Helper function to update all instances of this widget
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TodayCycleWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TodayCycleWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            context.sendBroadcast(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent) // Important to call super first

        if (ACTION_WIDGET_ADD_RECORD == intent.action) {
            performCheckIn(context)
            // After check-in, update all widgets to reflect the new data
            updateAllWidgets(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val durationData = DurationData.fromPreferences(context)
        val todayCycles = durationData.todayCycles

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.today_cycle_widget_layout)
        views.setTextViewText(R.id.tv_widget_today_cycles, "Today's Cycles: $todayCycles")

        // Setup pending intent for the button click
        val intent = Intent(context, TodayCycleWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_ADD_RECORD
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0, // requestCode, can be 0 if not distinguishing between multiple intents
            intent,
            pendingIntentFlags
        )
        views.setOnClickPendingIntent(R.id.btn_widget_add_record, pendingIntent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performCheckIn(context: Context) {
        // This function replicates the core logic of DurationViewModel.checkIn()
        // and DurationViewModel.resetIfNeeded()
        var data = DurationData.fromPreferences(context)
        val today = LocalDate.now()
        val now = LocalDateTime.now()

        // --- Start: Replicate resetIfNeeded logic ---
        var needsSaveAfterReset = false
        var updatedDataForReset = data.copy()

        if (DurationUtils.shouldResetDaily(today, data.lastResetDate)) {
            updatedDataForReset = updatedDataForReset.copy(
                todayCycles = 0,
                lastRecordedTodayCycles = 0,
                lastResetDate = today
            )
            needsSaveAfterReset = true
        }
        if (DurationUtils.shouldResetWeekly(today, data.lastResetDate ?: today )) { // Use today if lastResetDate is null
            updatedDataForReset = updatedDataForReset.copy(weekCycles = 0)
            if (updatedDataForReset.lastResetDate == null || !updatedDataForReset.lastResetDate!!.isEqual(today)) {
                updatedDataForReset = updatedDataForReset.copy(lastResetDate = today)
            }
            needsSaveAfterReset = true
        }
        if (DurationUtils.shouldResetMonthly(today, data.lastResetDate ?: today)) { // Use today if lastResetDate is null
            updatedDataForReset = updatedDataForReset.copy(monthCycles = 0)
            if (updatedDataForReset.lastResetDate == null || !updatedDataForReset.lastResetDate!!.isEqual(today)) {
                updatedDataForReset = updatedDataForReset.copy(lastResetDate = today)
            }
            needsSaveAfterReset = true
        }

        if (needsSaveAfterReset) {
            data = updatedDataForReset // Use the updated data for subsequent check-in logic
            // No need to save here, will be saved after check-in calculation
        }
        // --- End: Replicate resetIfNeeded logic ---


        val currentTodayCycles = DurationUtils.calculateCycles(today, now)
        val previousRecordedTodayCycles = data.lastRecordedTodayCycles


        // Calculate newly added cycles
        // If lastResetDate from 'data' (which might have been updated by reset logic) is today,
        // it means a reset just occurred for this day OR it's a subsequent check-in on the same day.
        val cyclesToAdd = if (data.lastResetDate != null && today.isEqual(data.lastResetDate)) {
            (currentTodayCycles - previousRecordedTodayCycles).coerceAtLeast(0)
        } else {
            // This case implies it's the very first record for a new day where lastResetDate was older,
            // or data.lastResetDate was null (e.g. fresh install or after clear).
            // In this scenario, resetIfNeeded logic should have set lastResetDate to today,
            // and lastRecordedTodayCycles to 0. So currentTodayCycles are all new.
            // However, to be absolutely safe, especially if resetIfNeeded didn't run or
            // lastResetDate was somehow still not today, we treat all currentTodayCycles as new.
            currentTodayCycles
        }


        data = data.copy(
            lastCheckIn = now,
            todayCycles = currentTodayCycles,
            weekCycles = data.weekCycles + cyclesToAdd,
            monthCycles = data.monthCycles + cyclesToAdd,
            lastRecordedTodayCycles = currentTodayCycles,
            // Ensure lastResetDate is set if it was null and a reset didn't explicitly set it.
            // This typically shouldn't be needed if reset logic is comprehensive but acts as a safeguard.
            lastResetDate = data.lastResetDate ?: today
        )

        DurationData.saveToPreferences(context, data)
    }
}
