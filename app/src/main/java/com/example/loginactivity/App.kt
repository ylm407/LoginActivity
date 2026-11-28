package com.example.loginactivity

import android.app.Application
import com.example.loginactivity.notify.NotificationHelper

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 通知渠道只需建一次，放在 App 启动时最稳
        NotificationHelper.createChannels(this)
    }
}
