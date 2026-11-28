package com.example.loginactivity.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.loginactivity.R

/**
 * 闹钟到点后系统调这里，只负责把通知弹出来。
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: context.getString(R.string.app_name)
        val content = intent.getStringExtra(EXTRA_CONTENT)
            ?: context.getString(R.string.notify_todo_default_content)

        NotificationHelper.showTodoReminder(context, todoId, title, content)
    }

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
    }
}
