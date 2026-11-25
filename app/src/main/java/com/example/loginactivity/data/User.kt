package com.example.loginactivity.data

data class User(
    val id: Long,
    val username: String,
    val password: String,
    val nickname: String,
    val email: String
)

/**
 * 对应 DbHelper 里 users 表的一行数据。
 * 字段名和表的列名一一对应，顺序也一致：
 * id / username / password / nickname / email这样的话读取时读取的是这5个数据
 */
