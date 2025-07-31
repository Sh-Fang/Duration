// work/ResetWorker.kt
package com.example.duration.work

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.duration.model.DurationData
import com.example.duration.model.DurationUtils
import java.time.LocalDate

class ResetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val KEY_RESET_TYPE = "reset_type" // "daily", "weekly", "monthly"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            val resetType = inputData.getString(KEY_RESET_TYPE) ?: return Result.failure()
            val context = applicationContext
            val today = LocalDate.now()
            
            // Load current data
            var data = DurationData.fromPreferences(context)
            
            // Perform reset based on type
            when (resetType) {
                "daily" -> {
                    data = data.copy(todayCycles = 0, lastResetDate = today)
                }
                "weekly" -> {
                    data = data.copy(weekCycles = 0)
                }
                "monthly" -> {
                    data = data.copy(monthCycles = 0)
                }
            }
            
            // Save updated data
            DurationData.saveToPreferences(context, data)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}