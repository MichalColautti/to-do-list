package com.example.todolist

import android.net.Uri
import java.util.Date

data class Task(
    var id: Int = 0,
    val title: String,
    val description: String,
    val creationTime: Date,
    val dueTime: Date,
    val isCompleted: Boolean,
    val notificationEnabled: Boolean,
    val category: String,
    val attachments: List<TaskAttachment> = emptyList()
)

data class TaskAttachment(
    val uri: Uri,
    val name: String
)