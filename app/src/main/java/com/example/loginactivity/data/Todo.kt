package com.example.loginactivity.data

data class Todo(
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String,
    val category: String,
    val deadline: Long,
    val remindBeforeMinutes: Int,
    val isDone: Boolean = false
)
//为待办领域实体，字段与 todos 表列一一对应，用于承载待办的全部信息