package com.example.loginactivity.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.loginactivity.data.TodoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 手机重启后，系统里排队的所有闹钟都会被清空，这里重新排一遍。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = TodoRepository(context)
                repository.listAllFuture(System.currentTimeMillis()).forEach { todo ->
                    AlarmScheduler.schedule(context, todo)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
