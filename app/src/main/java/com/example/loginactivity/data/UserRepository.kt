package com.example.loginactivity.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {
    // DbHelper 本身是单例，这里拿到的和其它地方是同一个连接
    private val helper = DbHelper.get(context.applicationContext)//它声明了一个私有只读属性 helper，类型是 DbHelper，值是那个唯一的数据库管家

    suspend fun register(username: String, password: String): Boolean = withContext(Dispatchers.IO)// 切换到 IO 调度器（后台线程）去执行，并把执行结果作为函数返回值。
    {
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)//调用 ContentValues.put(列名, 值) 方法,存入DbHELPER表中
        }
        // insert 返回新行的 id，失败时返回 -1
        helper.writableDatabase.insert(DbHelper.TABLE_USERS, null, values) != -1L
    }
    //在后台线程里，把用户名和密码装进 ContentValues，插入数据库，返回是否注册成功。
    suspend fun login(username: String, password: String): User? = withContext(Dispatchers.IO) {
        helper.readableDatabase.query(
            DbHelper.TABLE_USERS,
            null,
            "username = ? AND password = ?",
            arrayOf(username, password),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toUser() else null
        }
    }
    /**
     * 按 id 查用户。登录态恢复后需要它来拿昵称、邮箱等信息。
     */
    suspend fun findById(id: Long): User? = withContext(Dispatchers.IO) {
        helper.readableDatabase.query(
            DbHelper.TABLE_USERS,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toUser() else null
        }
    }/**
     * 更新昵称和邮箱。返回受影响的行数，0 表示没找到这个 id。
     */
    suspend fun updateProfile(id: Long, nickname: String, email: String): Int =
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("nickname", nickname)
                put("email", email)
            }
            helper.writableDatabase.update(
                DbHelper.TABLE_USERS,
                values,
                "id = ?",
                arrayOf(id.toString())
            )
        }
    /**
     * 把游标当前指向的这一行，装成一个 User 对象。
     * 这是 Cursor 和 User 之间唯一的转换点。
     */
    private fun Cursor.toUser(): User = User(
        id = getLong(getColumnIndexOrThrow("id")),
        username = getString(getColumnIndexOrThrow("username")),
        password = getString(getColumnIndexOrThrow("password")),
        nickname = getString(getColumnIndexOrThrow("nickname")),
        email = getString(getColumnIndexOrThrow("email"))
    )
}
