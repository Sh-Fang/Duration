// work/NotificationWorker.kt
package com.example.duration.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log // Added for logging
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.duration.MainActivity
import com.example.duration.R
import com.example.duration.model.DurationData
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "NotificationWorker"
        private const val CHANNEL_ID = "duration_reminder_channel"
        private const val NOTIFICATION_ID = 101
        private const val TAG = "NotificationWorker" // Tag for logs
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork started.")
        val context = applicationContext
        val today = LocalDate.now()
        // We assume this worker is scheduled to run *after* 19:30.
        // For example, at 20:00 or 21:00.
        // So, we only need to check if a record was made today.

        val durationData = DurationData.fromPreferences(context)
        val lastCheckInDate = durationData.lastCheckIn?.toLocalDate()
        Log.d(TAG, "Today: $today, LastCheckInDate: $lastCheckInDate")

        val recordMadeToday = lastCheckInDate != null && lastCheckInDate.isEqual(today)
        Log.d(TAG, "Record made today: $recordMadeToday")

        // Check if current time is indeed after 19:30, as an extra safeguard
        // although scheduling should handle this.
        val reminderTriggerTime = LocalTime.of(13, 10) // Example: 1:04 PM for testing
        val currentTime = LocalTime.now()
        Log.d(TAG, "Current time: $currentTime, Reminder trigger time: $reminderTriggerTime")

        if (!recordMadeToday && currentTime.isAfter(reminderTriggerTime)) {
            Log.d(TAG, "Conditions met, sending notification.")
            sendNotification(context)
        } else {
            Log.d(TAG, "Conditions not met for notification.")
            if (recordMadeToday) Log.d(TAG, "Reason: Record was made today.")
            if (!currentTime.isAfter(reminderTriggerTime)) Log.d(TAG, "Reason: Current time is not after reminder trigger time.")
        }

        Log.d(TAG, "doWork finished.")
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(context: Context) {
        Log.d(TAG, "sendNotification called.")
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Replace with your app's notification icon
            .setContentTitle("打卡提醒")
            .setContentText("今天 19:30 之后还没有打卡记录哦！")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Notification might not be shown on API 33+.")
            // Permission is not granted by the worker.
            // The worker itself cannot request permission.
            // This check is more of a safeguard; permission should be handled in the Activity.
            // If permission is not granted, the notification might not appear on API 33+.
            // For older versions, this check is not strictly necessary for the notification to be posted,
            // but the permission should still be in the manifest.
            // Consider logging this or handling it if a background task absolutely needs to know
            // it can't post a notification. For this case, we attempt to post anyway,
            // and the system will block it if permissions are missing on API 33+.
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        Log.d(TAG, "Notification sent with ID: $NOTIFICATION_ID")
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // This check is redundant due to class-level @RequiresApi(O)
        Log.d(TAG, "Creating notification channel.")
        val name = "每日打卡提醒"
        val descriptionText = "提醒您记录每日打卡周期"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created/updated.")
        // }
    }
}

