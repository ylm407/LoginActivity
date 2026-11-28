package com.example.loginactivity

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 时间格式化工具：长日期、短日期、mm:ss 倒计时 */
object TimeUtils {

    private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val shortFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun formatDateTime(millis: Long): String = fullFormat.format(Date(millis))

    fun formatShort(millis: Long): String = shortFormat.format(Date(millis))

    /** 转成 mm:ss 格式 */
    fun formatClock(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        return String.format(Locale.getDefault(), "%02d:%02d", safe / 60, safe % 60)
    }
}
