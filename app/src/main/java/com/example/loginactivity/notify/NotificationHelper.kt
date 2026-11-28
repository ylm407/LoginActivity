package com.example.loginactivity.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.loginactivity.MainActivity
import com.example.loginactivity.R

object NotificationHelper {

    const val CHANNEL_REMINDER = "reminder"
    const val CHANNEL_FOCUS = "focus"

    private const val FOCUS_NOTIFICATION_ID = 10086

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                context.getString(R.string.channel_reminder),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_FOCUS,
                context.getString(R.string.channel_focus),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showTodoReminder(context: Context, todoId: Long, title: String, content: String) {
        if (!canNotify(context)) return
        createChannels(context)

        val contentIntent = PendingIntent.getActivity(
            context,
            todoId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_todo)
            .setContentTitle(context.getString(R.string.notify_todo_title, title))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(todoId.toInt(), notification)
    }

    fun showFocusDone(context: Context, target: String) {
        if (!canNotify(context)) return
        createChannels(context)

        val contentIntent = PendingIntent.getActivity(
            context,
            FOCUS_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(R.drawable.ic_focus)
            .setContentTitle(context.getString(R.string.notify_focus_title))
            .setContentText(context.getString(R.string.notify_focus_text, target))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(FOCUS_NOTIFICATION_ID, notification)
    }
}
