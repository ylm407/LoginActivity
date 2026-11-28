package com.example.loginactivity.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.loginactivity.R
import com.example.loginactivity.data.Todo

/**
 * 用 AlarmManager 在「截止时间 - 提前量」触发一条广播，广播里再发通知。
 *
 * 这里用非精确的 set()：Android 12+ 用精确闹钟需要额外申请特殊权限，
 * 课程演示用非精确版本已经够用，代价是可能晚几十秒到几分钟。
 */
object AlarmScheduler {

    fun schedule(context: Context, todo: Todo) {
        val triggerAt = todo.deadline - todo.remindBeforeMinutes * 60_000L
        if (triggerAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            buildPendingIntent(context, todo, PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    fun cancel(context: Context, todoId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoId.toInt(),
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun buildPendingIntent(context: Context, todo: Todo, flags: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TODO_ID, todo.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, todo.title)
            putExtra(
                ReminderReceiver.EXTRA_CONTENT,
                todo.description.ifBlank { context.getString(R.string.notify_todo_default_content) }
            )
        }
        return PendingIntent.getBroadcast(
            context,
            todo.id.toInt(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
