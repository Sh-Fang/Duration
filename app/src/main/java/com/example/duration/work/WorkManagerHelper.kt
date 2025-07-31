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
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    private const val DAILY_RESET_WORK = "daily_reset_work"
    private const val WEEKLY_RESET_WORK = "weekly_reset_work"
    private const val MONTHLY_RESET_WORK = "monthly_reset_work"

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleResetWorks(context: Context) {
        scheduleDailyReset(context)
        scheduleWeeklyReset(context)
        scheduleMonthlyReset(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleDailyReset(context: Context) {
        val data = Data.Builder()
            .putString(ResetWorker.KEY_RESET_TYPE, "daily")
            .build()

        // Calculate initial delay to make it run at 00:00
        val now = java.time.Instant.now()
        val zoneId = ZoneId.systemDefault()
        val todayMidnight = java.time.LocalDate.now().atStartOfDay(zoneId).toInstant()
        val tomorrowMidnight = todayMidnight.plusSeconds(24 * 60 * 60) // Add 24 hours

        val initialDelay = if (now.isAfter(todayMidnight)) {
            // If it's past midnight today, schedule for tomorrow
            ChronoUnit.MILLIS.between(now, tomorrowMidnight)
        } else {
            // If it's before midnight today, schedule for today
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

        // Calculate initial delay to make it run at midnight on Monday
        val now = java.time.Instant.now()
        val zoneId = ZoneId.systemDefault()
        val today = java.time.LocalDate.now()
        val daysUntilMonday = (7 - today.dayOfWeek.value + 1) % 7
        val nextMonday = if (daysUntilMonday == 0) today else today.plusDays(daysUntilMonday.toLong())
        val nextMondayMidnight = nextMonday.atStartOfDay(zoneId).toInstant()

        val initialDelay = if (now.isAfter(nextMondayMidnight)) {
            // If it's past Monday midnight, schedule for next Monday
            ChronoUnit.MILLIS.between(now, nextMondayMidnight.plusSeconds(7 * 24 * 60 * 60))
        } else {
            // If it's before Monday midnight, schedule for this Monday
            ChronoUnit.MILLIS.between(now, nextMondayMidnight)
        }

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

        // Calculate initial delay to make it run at midnight on the first day of next month
        val now = java.time.Instant.now()
        val zoneId = ZoneId.systemDefault()
        val today = java.time.LocalDate.now()
        val firstDayOfNextMonth = today.plusMonths(1).withDayOfMonth(1)
        val firstDayOfNextMonthMidnight = firstDayOfNextMonth.atStartOfDay(zoneId).toInstant()

        val initialDelay = ChronoUnit.MILLIS.between(now, firstDayOfNextMonthMidnight)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

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
}