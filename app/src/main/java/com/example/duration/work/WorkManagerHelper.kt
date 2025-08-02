// work/WorkManagerHelper.kt
package com.example.duration.work

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    private const val DAILY_RESET_WORK = "daily_reset_work"
    private const val WEEKLY_RESET_WORK = "weekly_reset_work"
    private const val MONTHLY_RESET_WORK = "monthly_reset_work"
    private const val NOTIFICATION_WORK = "notification_work" // Unique name for the new worker

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleWorks(context: Context) { // Renamed for clarity as it schedules more than resets
        scheduleDailyReset(context)
        scheduleWeeklyReset(context)
        scheduleMonthlyReset(context)
        scheduleNotificationWorker(context) // Call the new scheduling function
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleDailyReset(context: Context) {
        val data = Data.Builder()
            .putString(ResetWorker.KEY_RESET_TYPE, "daily")
            .build()

        val now = java.time.Instant.now()
        val zoneId = ZoneId.systemDefault()
        // Correctly get today's midnight
        val todayMidnight = java.time.LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()
        val tomorrowMidnight = todayMidnight.plus(1, ChronoUnit.DAYS)


        val initialDelay = if (now.isAfter(todayMidnight)) {
            ChronoUnit.MILLIS.between(now, tomorrowMidnight)
        } else {
            ChronoUnit.MILLIS.between(now, todayMidnight)
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ResetWorker>(24, TimeUnit.HOURS)
            .setInputData(data)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_RESET_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleWeeklyReset(context: Context) {
        val data = Data.Builder()
            .putString(ResetWorker.KEY_RESET_TYPE, "weekly")
            .build()

        val now = java.time.Instant.now()
        val zoneId = ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zoneId)
        // Calculate days until next Monday (1 = Monday, 7 = Sunday)
        val daysUntilMonday = (java.time.DayOfWeek.MONDAY.value - today.dayOfWeek.value + 7) % 7
        val nextMondayDate = if (daysUntilMonday == 0 && now.isAfter(today.atStartOfDay(zoneId).toInstant())) {
            today.plusWeeks(1) // If it's Monday but past midnight, schedule for next week's Monday
        } else if (daysUntilMonday == 0) {
            today // If it's Monday and before midnight
        }
        else {
            today.plusDays(daysUntilMonday.toLong())
        }
        val nextMondayMidnight = nextMondayDate.atStartOfDay(zoneId).toInstant()

        val initialDelay = ChronoUnit.MILLIS.between(now, nextMondayMidnight)


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ResetWorker>(7, TimeUnit.DAYS)
            .setInputData(data)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_RESET_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleMonthlyReset(context: Context) {
        val data = Data.Builder()
            .putString(ResetWorker.KEY_RESET_TYPE, "monthly")
            .build()

        val now = java.time.Instant.now()
        val zoneId = ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zoneId)
        var firstDayOfNextMonth = today.withDayOfMonth(1).plusMonths(1)
        // If today is already past the first day of this month's midnight, schedule for next month
        if (now.isAfter(today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant()) && today.dayOfMonth != 1) {
             // Already past first day, schedule for next month.
        } else if (today.dayOfMonth == 1 && now.isBefore(today.atStartOfDay(zoneId).toInstant())) {
            firstDayOfNextMonth = today.withDayOfMonth(1) // it's the first but before midnight
        }


        val firstDayOfNextMonthMidnight = firstDayOfNextMonth.atStartOfDay(zoneId).toInstant()
        val initialDelay = ChronoUnit.MILLIS.between(now, firstDayOfNextMonthMidnight)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Using an approximate 30 days for periodic interval. Exactness handled by initial delay.
        val workRequest = PeriodicWorkRequestBuilder<ResetWorker>(30, TimeUnit.DAYS)
            .setInputData(data)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MONTHLY_RESET_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotificationWorker(context: Context) {
        val now = java.time.LocalDateTime.now()
        val zoneId = ZoneId.systemDefault()
        var notificationTimeToday = now.with(LocalTime.of(20, 0)) // 8 PM today

        if (now.isAfter(notificationTimeToday)) {
            // If current time is past 8 PM today, schedule for 8 PM tomorrow
            notificationTimeToday = notificationTimeToday.plusDays(1)
        }

        val initialDelay = ChronoUnit.MILLIS.between(now.atZone(zoneId).toInstant(), notificationTimeToday.atZone(zoneId).toInstant())

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOTIFICATION_WORK,
            ExistingPeriodicWorkPolicy.REPLACE, // REPLACE if you want new scheduling to override old
            workRequest
        )
    }
}
