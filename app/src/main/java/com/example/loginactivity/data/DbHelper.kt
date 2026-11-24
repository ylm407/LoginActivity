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
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
//当 App 的数据库版本号变了时，这段代码会把旧表全部删掉，然后按新代码重新建表
    companion object {
        const val TABLE_USERS = "users"
        const val TABLE_TODOS = "todos"

        private const val DB_NAME = "login_app.db"
        private const val DB_VERSION = 1

        @Volatile
        private var instance: DbHelper? = null

        fun get(context: Context): DbHelper =
            instance ?: synchronized(this) {
                instance ?: DbHelper(context.applicationContext).also { instance = it }
            }
    }
}
