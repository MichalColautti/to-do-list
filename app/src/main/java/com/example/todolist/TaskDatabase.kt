package com.example.todolist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date
import androidx.core.database.sqlite.transaction

class TaskDatabase(context: Context) :
    SQLiteOpenHelper(context, "tasks.db", null, 8) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                creationTime INTEGER,
                dueTime INTEGER,
                isCompleted INTEGER,
                notificationEnabled INTEGER,
                category TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                taskId INTEGER NOT NULL,
                name TEXT NOT NULL,
                uri TEXT NOT NULL,
                FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS attachments")
        db.execSQL("DROP TABLE IF EXISTS tasks")
        onCreate(db)
    }

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        db.beginTransaction()

        try {
            val values = ContentValues().apply {
                put("title", task.title)
                put("description", task.description)
                put("creationTime", task.creationTime.time)
                put("dueTime", task.dueTime.time)
                put("isCompleted", if (task.isCompleted) 1 else 0)
                put("notificationEnabled", if (task.notificationEnabled) 1 else 0)
                put("category", task.category)
            }

            val taskId = db.insert("tasks", null, values)

            task.attachments.forEach { attachment ->
                val attachmentValues = ContentValues().apply {
                    put("taskId", taskId)
                    put("name", attachment.name)
                    put("uri", attachment.uri.toString())
                }
                db.insert("attachments", null, attachmentValues)
            }

            db.setTransactionSuccessful()
            return taskId
        } finally {
            db.endTransaction()
        }
    }

    fun deleteTask(taskId: Int) {
        writableDatabase.delete("attachments", "taskId = ?", arrayOf(taskId.toString()))
        writableDatabase.delete("tasks", "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        val values = ContentValues().apply {
            put("isCompleted", if (isCompleted) 1 else 0)
        }
        writableDatabase.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM tasks", null)
        if (cursor.moveToFirst()) {
            do {
                val taskId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val attachments = getAttachmentsForTask(taskId)

                tasks.add(
                    Task(
                        id = taskId,
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        creationTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow("creationTime"))),
                        dueTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow("dueTime"))),
                        isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")) == 1,
                        notificationEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("notificationEnabled")) == 1,
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        attachments = attachments
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return tasks
    }

    private fun getAttachmentsForTask(taskId: Int): List<TaskAttachment> {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM attachments WHERE taskId = ?",
            arrayOf(taskId.toString())
        )

        val attachments = mutableListOf<TaskAttachment>()

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val uriString = cursor.getString(cursor.getColumnIndexOrThrow("uri"))
                attachments.add(TaskAttachment(name = name, uri = android.net.Uri.parse(uriString)))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return attachments
    }

    fun updateTask(task: Task) {
        val db = writableDatabase
        db.transaction() {
            try {
                val values = ContentValues().apply {
                    put("title", task.title)
                    put("description", task.description)
                    put("dueTime", task.dueTime.time)
                    put("isCompleted", if (task.isCompleted) 1 else 0)
                    put("notificationEnabled", if (task.notificationEnabled) 1 else 0)
                    put("category", task.category)
                }
                update("tasks", values, "id = ?", arrayOf(task.id.toString()))

                delete("attachments", "taskId = ?", arrayOf(task.id.toString()))

                task.attachments.forEach { attachment ->
                    val attachmentValues = ContentValues().apply {
                        put("taskId", task.id)
                        put("uri", attachment.uri.toString())
                        put("name", attachment.name)
                    }
                    insert("attachments", null, attachmentValues)
                }

            } finally {
            }
        }
    }

    fun getTaskById(id: Int): Task? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tasks WHERE id = ?", arrayOf(id.toString()))
        return if (cursor.moveToFirst()) {
            val task = Task.fromCursor(cursor).copy(
                attachments = getAttachmentsForTask(id)
            )
            cursor.close()
            task
        } else {
            cursor.close()
            null
        }
    }
}