package com.example.loginactivity

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeUtil {object TimeUtils {

    private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())//定义两个日期格式化器，长日期，短日期，系统默认格式
    private val shortFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun formatDateTime(millis: Long): String = fullFormat.format(Date(millis))//格式化长完整日期时间，接收一个毫秒时间戳，先转成Data，在转成字符串

    fun formatShort(millis: Long): String = shortFormat.format(Date(millis))

    fun formatClock(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)//确保总秒数不小于零
        return String.format(Locale.getDefault(), "%02d:%02d", safe / 60, safe % 60)//格式化字符串，算出分秒，转成 mm:ss 格式
    }
}
}
/**转成 mm:ss 格式*/