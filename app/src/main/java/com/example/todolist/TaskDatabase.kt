package com.example.todolist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date

class TaskDatabase(context: Context) :
    SQLiteOpenHelper(context, "tasks.db", null, 3) {

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        onCreate(db)
    }

    fun insertTask(task: Task) {
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("creationTime", task.creationTime.time)
            put("dueTime", task.dueTime.time)
            put("isCompleted", 0)
            put("notificationEnabled", if (task.notificationEnabled) 1 else 0)
            put("category", task.category)
        }
        writableDatabase.insert("tasks", null, values)
    }

    fun deleteTask(taskId: Int) {
        writableDatabase.delete("tasks", "id = ?", arrayOf(taskId.toString()))
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        val values = ContentValues().apply {
            put("isCompleted", if (isCompleted) 1 else 0)
        }
        writableDatabase.update("tasks", values, "id = ?", arrayOf(taskId.toString()))
    }

    fun getAllTasks(): List<Task> {
        val cursor = readableDatabase.rawQuery("SELECT * FROM tasks", null)
        val tasks = mutableListOf<Task>()

        if (cursor.moveToFirst()) {
            do {
                tasks.add(
                    Task(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        creationTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow("creationTime"))),
                        dueTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow("dueTime"))),
                        isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")) == 1,
                        notificationEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("notificationEnabled")) == 1,
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return tasks
    }
}
