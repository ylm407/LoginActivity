package com.example.loginactivity.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TodoRepository(context: Context) {

    // 和 UserRepository 拿的是同一个 DbHelper 单例
    private val helper = DbHelper.get(context.applicationContext)
    suspend fun listByUser(userId: Long): List<Todo> = withContext(Dispatchers.IO) {
        helper.readableDatabase.query(
            DbHelper.TABLE_TODOS,
            null,
            "userId = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "isDone ASC, deadline ASC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toTodo())
            }
        }
    }

    suspend fun getById(id: Long): Todo? = withContext(Dispatchers.IO) {
        helper.readableDatabase.query(
            DbHelper.TABLE_TODOS,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toTodo() else null
        }
    }

    suspend fun insert(todo: Todo): Long = withContext(Dispatchers.IO) {
        helper.writableDatabase.insert(DbHelper.TABLE_TODOS, null, todo.toValues())
    }

    suspend fun update(todo: Todo): Int = withContext(Dispatchers.IO) {
        helper.writableDatabase.update(
            DbHelper.TABLE_TODOS,
            todo.toValues(),
            "id = ?",
            arrayOf(todo.id.toString())
        )
    }


    suspend fun delete(id: Long): Int = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete(
            DbHelper.TABLE_TODOS,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun setDone(id: Long, done: Boolean): Int = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("isDone", if (done) 1 else 0)
        }
        helper.writableDatabase.update(
            DbHelper.TABLE_TODOS,
            values,
            "id = ?",
            arrayOf(id.toString())
        )
    }
    private fun Todo.toValues(): ContentValues = ContentValues().apply {
        put("userId", userId)
        put("title", title)
        put("description", description)
        put("category", category)
        put("deadline", deadline)
        put("remindBeforeMinutes", remindBeforeMinutes)
        put("isDone", if (isDone) 1 else 0)
    }

    private fun Cursor.toTodo(): Todo = Todo(
        id = getLong(getColumnIndexOrThrow("id")),
        userId = getLong(getColumnIndexOrThrow("userId")),
        title = getString(getColumnIndexOrThrow("title")),
        description = getString(getColumnIndexOrThrow("description")),
        category = getString(getColumnIndexOrThrow("category")),
        deadline = getLong(getColumnIndexOrThrow("deadline")),
        remindBeforeMinutes = getInt(getColumnIndexOrThrow("remindBeforeMinutes")),
        isDone = getInt(getColumnIndexOrThrow("isDone")) == 1
    )
}
