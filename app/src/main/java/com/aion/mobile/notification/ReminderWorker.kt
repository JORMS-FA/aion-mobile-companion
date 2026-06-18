package com.aion.mobile.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Recordatorio"
        val message = inputData.getString("message") ?: "Tienes un recordatorio pendiente"
        val reminderId = inputData.getString("reminderId") ?: ""

        NotificationHelper.showReminderNotification(
            applicationContext,
            reminderId.hashCode(),
            title,
            message
        )

        return Result.success()
    }

    companion object {
        private const val WORK_NAME_PREFIX = "reminder_"

        fun scheduleOneTime(
            context: Context,
            reminder: Reminder
        ) {
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(
                    reminder.triggerAtMillis - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
                .setInputData(
                    androidx.work.Data.Builder()
                        .putString("title", reminder.title)
                        .putString("message", reminder.message)
                        .putString("reminderId", reminder.id)
                        .build()
                )
                .addTag("reminder_${reminder.id}")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "$WORK_NAME_PREFIX${reminder.id}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }

        fun scheduleRecurring(
            context: Context,
            reminder: Reminder
        ) {
            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                reminder.recurringIntervalMinutes,
                TimeUnit.MINUTES
            )
                .setInputData(
                    androidx.work.Data.Builder()
                        .putString("title", reminder.title)
                        .putString("message", reminder.message)
                        .putString("reminderId", reminder.id)
                        .build()
                )
                .addTag("reminder_${reminder.id}")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "$WORK_NAME_PREFIX${reminder.id}",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
        }

        fun schedule(context: Context, reminder: Reminder) {
            if (reminder.isRecurring && reminder.recurringIntervalMinutes > 0) {
                scheduleRecurring(context, reminder)
            } else {
                scheduleOneTime(context, reminder)
            }
        }

        fun cancel(context: Context, reminderId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("$WORK_NAME_PREFIX$reminderId")
        }

        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag("reminder")
        }
    }
}
