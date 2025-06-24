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
) {
    companion object {
        fun fromCursor(cursor: android.database.Cursor): Task {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val creationTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow("creationTime")))
            val dueTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow("dueTime")))
            val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")) == 1
            val notificationEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("notificationEnabled")) == 1
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))

            return Task(
                id = id,
                title = title,
                description = description,
                creationTime = creationTime,
                dueTime = dueTime,
                isCompleted = isCompleted,
                notificationEnabled = notificationEnabled,
                category = category,
                attachments = emptyList()
            )
        }
    }
}

data class TaskAttachment(
    val uri: Uri,
    val name: String
)