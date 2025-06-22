package com.example.todolist

import java.util.Date

data class Task(
    var id: Int = 0,
    val title: String,
    val description: String,
    val creationTime: Date,
    val dueTime: Date,
    val isCompleted: Boolean,
    val notificationEnabled: Boolean,
    val category: String
)

