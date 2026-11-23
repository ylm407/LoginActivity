package com.example.loginactivity

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLogin(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun isLoggedIn(): Boolean = currentUserId() != -1L

    fun currentUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun logout() {
        // 只清登录状态，记住的账号密码保留，方便下次登录
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    fun saveRemembered(username: String, password: String) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER, true)
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun clearRemembered() {
        prefs.edit()
            .putBoolean(KEY_REMEMBER, false)
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }

    fun isRemembered(): Boolean = prefs.getBoolean(KEY_REMEMBER, false)

    fun rememberedUsername(): String = prefs.getString(KEY_USERNAME, "").orEmpty()

    fun rememberedPassword(): String = prefs.getString(KEY_PASSWORD, "").orEmpty()

    companion object {
        private const val PREFS_NAME = "session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_REMEMBER = "remember_password"
        private const val KEY_USERNAME = "remembered_username"
        private const val KEY_PASSWORD = "remembered_password"
    }
}