package com.example.loginactivity.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                nickname TEXT NOT NULL DEFAULT '',
                email TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
//建立一个用户表，包含用户名，密码，昵称，邮箱

        db.execSQL(
            """
            CREATE TABLE $TABLE_TODOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                category TEXT NOT NULL DEFAULT '',
                deadline INTEGER NOT NULL,
                remindBeforeMinutes INTEGER NOT NULL DEFAULT 0,
                isDone INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
//建立待办表。userId 指向 users.id，用来区分“这条待办属于谁”
//deadline 存毫秒时间戳（Long 类型），isDone 用 0/1 代替 true/false
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TODOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")//接受一个参数db，然后执行一条不返回数据的 SQL 语句
        onCreate(db)
    }
//当 App 的数据库版本号变了时，这段代码会把旧表全部删掉，然后按新代码重新建表
    companion object {
        const val TABLE_USERS = "users"
        const val TABLE_TODOS = "todos"

        private const val DB_NAME = "login_app.db"
        private const val DB_VERSION = 2
//定义数据库名称和版本号
        @Volatile
        private var instance: DbHelper? = null

        fun get(context: Context): DbHelper =
            instance ?: synchronized(this) {
                instance ?: DbHelper(context.applicationContext).also { instance = it }
            }
    }
}
